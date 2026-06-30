package com.example.undertale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lógica de CLIENTE (bus GAME) do osso: lê a tecla e, quando apertada (com o set
 * do Sans), manda o pacote pro servidor arremessar o osso.
 *
 * O osso que aparece EMBAIXO ao voar NÃO é desenhado aqui — é uma entidade real
 * em modo FOLLOW, gerenciada por {@link BoneArmorSansFollowHandler}.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class BoneArmorSansClient {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // consumeClick() devolve true uma vez por clique acumulado.
        while (ModKeybinds.THROW_BONE.consumeClick()) {
            // Só tenta com o set (o servidor revalida de qualquer jeito).
            if (SansArmorHandler.isWearingFullSet(player)) {
                PacketDistributor.sendToServer(new ModNetwork.ThrowBonePayload());
            }
        }
    }
}
