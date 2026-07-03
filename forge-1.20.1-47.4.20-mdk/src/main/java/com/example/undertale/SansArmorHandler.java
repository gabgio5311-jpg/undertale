package com.example.undertale;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler do set bonus da armadura do Sans — replica a Infinity Armor do Avaritia
 * (AbilityHandler + InfinityHandler), porém TODOS os efeitos só valem com o SET COMPLETO
 * (capacete + peitoral + calças + botas Sans).
 *
 * Efeitos:
 *  - Voo criativo
 *  - Invencibilidade total (cancela hurt/attack/death)
 *  - Fome e saturação sempre cheias
 *  - Respiração na água (ar sempre cheio)
 *  - Visão Noturna
 *  - Imunidade ao fogo (apaga chamas todo tick)
 *  - Remove efeitos de poção negativos
 *  - Step assist (sobe blocos de 1) + boost de pulo ao correr
 *
 * Exceção (igual ao "tipo de dano INFINITY" do Avaritia): a {@code the_real_knife}
 * ainda atravessa a invencibilidade — a faca que nerfa a Sword of the Cosmos também
 * vence a armadura. Ajuste isto depois se quiser.
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SansArmorHandler {

    // Jogadores em que NÓS concedemos voo / step assist, para reverter ao tirar a armadura.
    private static final Set<UUID> grantedFlight = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> grantedStep = ConcurrentHashMap.newKeySet();

    private static final float STEP_UP = 1.0625F;
    private static final float STEP_DEFAULT = 0.6F;

    // Velocidade das botas (valores padrão do Avaritia / ModConfig).
    private static final double BOOT_SPEED_BASE = 1.5;
    private static final double BOOT_SPEED_FLYING_MULT = 3.5;
    private static final double BOOT_SPEED_SWIMMING_MULT = 2.5;
    private static final double BOOT_SPEED_SNEAKING_MULT = 0.1;
    private static final double BOOT_SPEED_BACKWARD_MULT = 0.5;
    private static final double BOOT_SPEED_STRAFING_MULT = 0.8;
    private static final double BOOT_SPEED_SPRINTING_MULT = 3.5;

    /* ===================== Detecção do set ===================== */

    /** True se o jogador estiver com as 4 peças Sans equipadas. */
    public static boolean isWearingFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof SansArmorItem)) {
                return false;
            }
        }
        return true;
    }

    /** True se o dano veio de alguém empunhando a the_real_knife (atravessa a invencibilidade). */
    private static boolean isRealKnifeSource(DamageSource source) {
        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity living)) return false;
        return living.getMainHandItem().is(ModItems.THE_REAL_KNIFE.get())
                || living.getOffhandItem().is(ModItems.THE_REAL_KNIFE.get());
    }

    /* ===================== Bônus ativos (tick) ===================== */

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isWearingFullSet(player)) {
            applyBonuses(player);
        } else {
            cleanup(player);
        }
    }

    private static void applyBonuses(Player player) {
        // --- Capacete: respiração + fome + visão noturna ---
        player.setAirSupply(300);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        // Re-aplica a visão noturna a cada tick (renova a duração sem piscar perto do fim).
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));

        // --- Peitoral: voo criativo + remove efeitos negativos ---
        if (!player.isCreative() && !player.isSpectator()) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                syncAbilities(player);
            }
            grantedFlight.add(player.getUUID());
        }
        removeNegativeEffects(player);

        // --- Calças: imunidade ao fogo ---
        if (player.isOnFire()) {
            player.clearFire();
            player.setRemainingFireTicks(0);
        }

        // --- Botas: step assist + boost de velocidade ---
        player.setMaxUpStep(STEP_UP);
        grantedStep.add(player.getUUID());
        applyBootSpeed(player);
    }

    /** Boost de movimento das botas, igual ao AbilityHandler do Avaritia. */
    private static void applyBootSpeed(Player player) {
        boolean flying = player.getAbilities().flying;
        boolean swimming = player.isInWater();
        boolean sneaking = player.isShiftKeyDown();

        if (player.onGround() || flying || swimming) {
            float speed = (float) (BOOT_SPEED_BASE
                    * (flying ? BOOT_SPEED_FLYING_MULT : 1.0)
                    * (swimming ? BOOT_SPEED_SWIMMING_MULT : 1.0)
                    * (sneaking ? BOOT_SPEED_SNEAKING_MULT : 1.0));

            // frente / trás (zza = input para frente)
            if (player.zza > 0.0F) {
                player.moveRelative(speed, new Vec3(0.0, 0.0, 1.0));
            } else if (player.zza < 0.0F) {
                player.moveRelative((float) (-speed * BOOT_SPEED_BACKWARD_MULT), new Vec3(0.0, 0.0, 1.0));
            }
            // strafe (xxa = input lateral)
            if (player.xxa != 0.0F) {
                player.moveRelative((float) (speed * BOOT_SPEED_STRAFING_MULT * Math.signum(player.xxa)),
                        new Vec3(1.0, 0.0, 0.0));
            }
        }

        // corrida: empurrão extra na direção em que está olhando
        if (player.isSprinting()) {
            float f = player.getYRot() * ((float) Math.PI / 180.0F);
            player.setDeltaMovement(player.getDeltaMovement().add(
                    -Mth.sin(f) * BOOT_SPEED_SPRINTING_MULT, 0.0, Mth.cos(f) * BOOT_SPEED_SPRINTING_MULT));
        }
    }

    private static void removeNegativeEffects(Player player) {
        List<MobEffectInstance> active = new ArrayList<>(player.getActiveEffects());
        for (MobEffectInstance instance : active) {
            if (!instance.getEffect().isBeneficial()) {
                player.removeEffect(instance.getEffect());
            }
        }
    }

    /** Reverte voo/step que NÓS concedemos quando o jogador tira a armadura. */
    private static void cleanup(Player player) {
        UUID id = player.getUUID();

        if (grantedFlight.remove(id)) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                syncAbilities(player);
            }
        }
        if (grantedStep.remove(id)) {
            player.setMaxUpStep(STEP_DEFAULT);
        }
    }

    private static void syncAbilities(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    // Boost de pulo ao correr (botas).
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (!player.isSprinting()) return;
        player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.305F, 0.0));
    }

    /* ===================== Invencibilidade ===================== */

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (isRealKnifeSource(event.getSource())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (isRealKnifeSource(event.getSource())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (isRealKnifeSource(event.getSource())) return;
        event.setAmount(0.0F);
        player.hurtTime = 0;
        player.hurtDuration = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (isRealKnifeSource(event.getSource())) return;
        // Cancela a morte e restaura a vida cheia, como a Infinity Armor.
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
    }
}
