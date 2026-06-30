package com.example.undertale;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Energia azul "de fogo" saindo do olho do Sans.
 *
 * Roda SÓ no cliente (efeito puramente visual) e solta partículas de
 * {@link ParticleTypes#SOUL_FIRE_FLAME} — a chama azul da soul fire, que já
 * sobe/tremeluz/some como fogo — na posição do olho do jogador, enquanto ele
 * estiver com o capacete do Sans.
 *
 * Como vê o capacete em qualquer jogador na sua tela, o efeito aparece tanto
 * em você quanto nos outros players usando a armadura.
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SansEyeParticles {

    // Quantas partículas por tick (mais = chama mais densa). 2 é um bom meio-termo.
    private static final int PER_TICK = 2;
    // Deslocamento lateral até o olho. Inverta o sinal se sair pelo olho errado.
    private static final double SIDE = -0.14;
    // O quanto à frente do rosto a chama nasce.
    private static final double FRONT = 0.18;

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Level level = player.level();
        if (!level.isClientSide()) return;

        // Não polui a SUA visão em 1ª pessoa: pula só o jogador local quando NÃO está em F5.
        // (Os outros jogadores continuam vendo a sua chama normalmente.)
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) return;

        // Só com o capacete do Sans equipado.
        // (Troque por SansArmorHandler.isWearingFullSet(player) se quiser exigir o set completo.)
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
