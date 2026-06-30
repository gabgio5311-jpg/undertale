package com.example.undertale;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Eventos de setup do cliente que rodam no BUS DO MOD (registro de renderers,
 * camadas de modelo e teclas). Distinto do {@link BoneArmorSansClient}, que roda
 * no bus FORGE (eventos de jogo a cada tick/frame).
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // O modelo/animação são carregados pelo GeckoLib via BoneArmorSansModel — não há
        // RegisterLayerDefinitions vanilla.
        event.registerEntityRenderer(ModEntities.BONE_ARMOR_SANS.get(), BoneArmorSansRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.THROW_BONE);
    }
}
