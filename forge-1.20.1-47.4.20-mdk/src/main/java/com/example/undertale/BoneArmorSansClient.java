package com.example.undertale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lógica de CLIENTE (bus FORGE) do osso: lê a tecla e, quando apertada (com o set
 * do Sans), manda o pacote pro servidor arremessar o osso.
 *
 * O osso que aparece EMBAIXO ao voar NÃO é desenhado aqui — é uma entidade real
 * em modo FOLLOW, gerenciada por {@link BoneArmorSansFollowHandler}.
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BoneArmorSansClient {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // consumeClick() devolve true uma vez por clique acumulado.
        while (ModKeybinds.THROW_BONE.consumeClick()) {
            // Só tenta com o set (o servidor revalida de qualquer jeito).
            if (SansArmorHandler.isWearingFullSet(player)) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.ThrowBonePacket());
            }
        }
    }
}
