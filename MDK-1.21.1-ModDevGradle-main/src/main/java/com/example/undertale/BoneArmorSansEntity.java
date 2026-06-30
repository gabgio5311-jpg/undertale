package com.example.undertale;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * Osso do Sans (bone_armor_sans) — entidade animada via GeckoLib.
 *
 * Tem dois MODOS (ver {@link Mode}):
 *  - {@code THROWN}: invocado pela tecla. É um PROJÉTIL: voa em linha reta na
 *    direção em que o jogador olhava, causa dano fixo ao acertar e some.
 *  - {@code FOLLOW}: invocado enquanto o jogador VOA com o set do Sans. Fica
 *    flutuando embaixo dele (o jogador "voa montado num osso").
 *
 * Estende {@link Entity} (não LivingEntity): sem vida/IA. O movimento e a colisão
 * do projétil são feitos na mão. As animações ({@code idle} / {@code throw}) ficam no
 * Blockbench em assets/undertale/animations/bone_armor_sans.animation.json.
 */
public class BoneArmorSansEntity extends Entity implements GeoEntity {

    public enum Mode { THROWN, FOLLOW }

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(BoneArmorSansEntity.class, EntityDataSerializers.INT);

    // Animações (nomes batem com o .animation.json). Padrão GeckoLib "<nome>.animation.<anim>".
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("bone_armor_sans.animation.idle");
    private static final RawAnimation THROW_ANIM = RawAnimation.begin().thenLoop("bone_armor_sans.animation.throw");

    private static final float DAMAGE = 100.0F;        // dano fixo ao acertar (não ignora invulnerabilidade)
    private static final double THROW_SPEED = 1.5;     // blocos por tick do osso arremessado
    private static final int THROW_LIFETIME = 60;      // some depois de ~3s se não acertar nada

    private static final double FOLLOW_OFFSET_Y = -0.15; // logo abaixo dos pés, encostando (modo FOLLOW)

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID ownerUUID;

    public BoneArmorSansEntity(EntityType<? extends BoneArmorSansEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;     // movimento/colisão tratados manualmente (projétil) ou fixo (follow)
        this.noCulling = true;     // sempre renderiza
    }

    /* ===================== Setup pelos invocadores ===================== */

    /** Configura como OSSO ARREMESSADO: voa na direção em que o dono olha. */
    public void setupThrow(LivingEntity owner) {
        setMode(Mode.THROWN);
        this.ownerUUID = owner.getUUID();
        Vec3 look = owner.getLookAngle();
        this.setDeltaMovement(look.scale(THROW_SPEED));
        // orienta o osso na direção do arremesso (a animação pode girar por cima)
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    /** Configura como osso de VOO (flutua embaixo do jogador). */
    public void setupFollow(LivingEntity owner) {
        setMode(Mode.FOLLOW);
        this.ownerUUID = owner.getUUID();
        repositionBelow(owner);
    }

    public Mode getMode() {
        return Mode.values()[this.entityData.get(DATA_MODE)];
    }

    private void setMode(Mode mode) {
        this.entityData.set(DATA_MODE, mode.ordinal());
    }

    @Nullable
    private LivingEntity getOwner() {
        if (this.ownerUUID != null && this.level() instanceof ServerLevel server) {
            Entity entity = server.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    /* ===================== Tick ===================== */

    @Override
    public void tick() {
        super.tick();

        if (this.level() instanceof ServerLevel server) {
            if (getMode() == Mode.FOLLOW) {
                LivingEntity owner = getOwner();
                if (owner == null) {
                    discard();   // dono sumiu — o handler normalmente já tira, isto é a garantia
                    return;
                }
                repositionBelow(owner);
            } else { // THROWN
                tickProjectile(server);
            }
        }
    }

    /** Mantém o osso flutuando logo abaixo do jogador, virado pra mesma direção. */
    private void repositionBelow(LivingEntity owner) {
        Vec3 p = owner.position();
        this.setPos(p.x, p.y + FOLLOW_OFFSET_Y, p.z);
        this.setYRot(owner.yBodyRot);
    }

    /* ===================== Projétil (modo THROWN) ===================== */

    private void tickProjectile(ServerLevel server) {
        Vec3 from = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 to = from.add(motion);

        // 1) Colisão com bloco no caminho deste tick.
        BlockHitResult blockHit = server.clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            onHit(server, null, blockHit.getLocation());
            return;
        }

        // 2) Colisão com entidade viva (menos o dono) no caminho.
        LivingEntity owner = getOwner();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(server, this, from, to,
                this.getBoundingBox().expandTowards(motion).inflate(1.0),
                e -> e != owner && e instanceof LivingEntity && e.isAlive());
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity victim) {
            onHit(server, victim, entityHit.getLocation());
            return;
        }

        // 3) Sem colisão: avança e solta um rastro de partículas.
        this.setPos(to.x, to.y, to.z);
        server.sendParticles(ParticleTypes.CRIT, to.x, to.y, to.z, 1, 0.0, 0.0, 0.0, 0.0);

        if (this.tickCount >= THROW_LIFETIME) {
            discard();
        }
    }

    /** Acerto do osso: aplica dano (se for entidade), estoura partículas e some. */
    private void onHit(ServerLevel server, @Nullable LivingEntity victim, Vec3 where) {
        if (victim != null) {
            LivingEntity owner = getOwner();
            DamageSource source = (owner instanceof Player player)
                    ? this.damageSources().playerAttack(player)
                    : this.damageSources().magic();
            victim.hurt(source, DAMAGE);
        }
        server.sendParticles(ParticleTypes.POOF, where.x, where.y, where.z, 8, 0.2, 0.2, 0.2, 0.02);
        discard();
    }

    /* ===================== GeckoLib ===================== */

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (getMode() == Mode.FOLLOW) {
                return state.setAndContinue(IDLE_ANIM);
            }
            return state.setAndContinue(THROW_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /* ===================== Plumbing de Entity ===================== */

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MODE, Mode.THROWN.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.entityData.set(DATA_MODE, tag.getInt("Mode"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("Mode", this.entityData.get(DATA_MODE));
    }
}
