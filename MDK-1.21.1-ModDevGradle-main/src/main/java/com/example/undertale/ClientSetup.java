package com.example.undertale;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Setup do cliente que roda no BUS DO MOD (registro de renderers e teclas).
 * Distinto do {@link BoneArmorSansClient}, que roda no bus GAME (eventos a cada tick).
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // O modelo/animação são carregados pelo GeckoLib via BoneArmorSansModel.
        event.registerEntityRenderer(ModEntities.BONE_ARMOR_SANS.get(), BoneArmorSansRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.THROW_BONE);
    }
}
