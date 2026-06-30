package com.example.undertale;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Energia azul "de fogo" saindo do olho do Sans (efeito puramente visual, só no cliente).
 * Solta partículas de {@link ParticleTypes#SOUL_FIRE_FLAME} no olho enquanto o jogador
 * estiver com o capacete do Sans.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SansEyeParticles {

    // Quantas partículas por tick (mais = chama mais densa).
    private static final int PER_TICK = 2;
    // Deslocamento lateral até o olho. Inverta o sinal se sair pelo olho errado.
    private static final double SIDE = -0.14;
    // O quanto à frente do rosto a chama nasce.
    private static final double FRONT = 0.18;

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Level level = player.level();
        if (!level.isClientSide()) return;

        // Não polui a SUA visão em 1ª pessoa: pula só o jogador local quando NÃO está em F5.
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) return;

        // Só com o capacete do Sans equipado.
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof SansArmorItem)) return;

        RandomSource rng = player.getRandom();

        // Vetores horizontais a partir da rotação do CORPO.
        float yaw = player.yBodyRot * ((float) Math.PI / 180.0F);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw); // frente
        double rx = -fz, rz = fx;                       // direita do jogador

        // Posição-base do olho: à frente do rosto, na altura dos olhos, deslocado pro lado.
        double eyeY = player.getY() + player.getEyeHeight() - 0.10;
        double px = player.getX() + fx * FRONT + rx * SIDE;
        double pz = player.getZ() + fz * FRONT + rz * SIDE;

        for (int i = 0; i < PER_TICK; i++) {
            double jx = (rng.nextDouble() - 0.5) * 0.04;
            double jz = (rng.nextDouble() - 0.5) * 0.04;
            double up = 0.015 + rng.nextDouble() * 0.02; // subida tipo fogo
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    px + jx, eyeY + rng.nextDouble() * 0.05, pz + jz,
                    jx * 0.3, up, jz * 0.3);
        }
    }
}
