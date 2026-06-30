package com.example.undertale;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler do set bonus da armadura do Sans — replica a Infinity Armor do Avaritia
 * (AbilityHandler + InfinityHandler), porém TODOS os efeitos só valem com o SET COMPLETO
 * (capacete + peitoral + calças + botas Sans).
 *
 * Exceção (igual ao "tipo de dano INFINITY" do Avaritia): a {@code the_real_knife}
 * ainda atravessa a invencibilidade.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SansArmorHandler {

    // Jogadores em que NÓS concedemos voo, para reverter ao tirar a armadura.
    private static final Set<UUID> grantedFlight = ConcurrentHashMap.newKeySet();

    // Step assist via atributo (no 1.21.1 não existe mais setMaxUpStep — é o atributo STEP_HEIGHT).
    // Base do jogador = 0.6; +0.4625 = 1.0625 (sobe blocos de 1).
    private static final ResourceLocation STEP_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(UndertaleMod.MOD_ID, "sans_step_assist");
    private static final AttributeModifier STEP_BOOST =
            new AttributeModifier(STEP_BOOST_ID, 0.4625, AttributeModifier.Operation.ADD_VALUE);

    // Velocidade das botas (valores padrão do Avaritia / ModConfig).
    private static final double BOOT_SPEED_BASE = 0.1;
    private static final double BOOT_SPEED_FLYING_MULT = 1.1;
    private static final double BOOT_SPEED_SWIMMING_MULT = 1.2;
    private static final double BOOT_SPEED_SNEAKING_MULT = 0.1;
    private static final double BOOT_SPEED_BACKWARD_MULT = 0.25;
    private static final double BOOT_SPEED_STRAFING_MULT = 0.45;
    private static final double BOOT_SPEED_SPRINTING_MULT = 0.2;

    /* ===================== Detecção do set ===================== */

    /** True se o jogador estiver com as 4 peças Sans equipadas. */
    public static boolean isWearingFullSet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SansArmorItem
                && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SansArmorItem
                && entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof SansArmorItem
                && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SansArmorItem;
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
    public static void onEntityTick(EntityTickEvent.Post event) {
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
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null && step.getModifier(STEP_BOOST_ID) == null) {
            step.addTransientModifier(STEP_BOOST);
        }
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
        for (MobEffectInstance instance : new ArrayList<>(player.getActiveEffects())) {
            if (!instance.getEffect().value().isBeneficial()) {
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
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null && step.getModifier(STEP_BOOST_ID) != null) {
            step.removeModifier(STEP_BOOST_ID);
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

    // No NeoForge 1.21.1, LivingIncomingDamageEvent substitui LivingAttackEvent/LivingHurtEvent.
    // Cancelar aqui impede dano E knockback.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWearingFullSet(player)) return;
        if (isRealKnifeSource(event.getSource())) return;
        event.setCanceled(true);
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
