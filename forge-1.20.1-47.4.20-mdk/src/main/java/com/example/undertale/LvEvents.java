package com.example.undertale;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mantém o cliente sincronizado com o total de almas (LV) guardado no servidor.
 *
 * O total vive no NBT persistente do jogador (ver {@link LvData}) e não é enviado
 * sozinho pro cliente. Aqui reenviamos nos momentos em que o cliente "começa do zero":
 * ao entrar no servidor, ao respawnar (depois de morrer) e ao trocar de dimensão.
 * O incremento por kill já sincroniza na hora, em {@link TheRealKnifeItem}.
 *
 * FORGE bus, auto-registrado (igual a {@code ModEvents}).
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LvEvents {

    /**
     * Conta as almas do LV: quando um <b>Mob</b> morre por um jogador que está
     * segurando o <b>real_knife comum</b> ({@link ModItems#REAL_KNIFE}).
     *
     * Só essa faca conta — a ideia é farmar LV com ela e, ao chegar no LV máximo,
     * desbloquear o craft da armadura do Sans e do the_real_knife.
     */
    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        // Só no servidor (fonte da verdade) e só pra mobs.
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob)) return;

        // Quem matou precisa ser um jogador segurando o real_knife na mão principal.
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && player.getMainHandItem().is(ModItems.REAL_KNIFE.get())) {
            LvData.addAlmas(player, 1);
            ModNetwork.syncLv(player); // atualiza GUI/tooltip na hora
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }
}
