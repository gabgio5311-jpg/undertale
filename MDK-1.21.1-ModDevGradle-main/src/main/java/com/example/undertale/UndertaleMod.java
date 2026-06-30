package com.example.undertale;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(UndertaleMod.MOD_ID)
public class UndertaleMod {
    public static final String MOD_ID = "undertale";

    // No NeoForge 1.21.1 o IEventBus do mod é injetado direto no construtor.
    public UndertaleMod(IEventBus bus) {
        ModItems.ITEMS.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModArmorMaterials.ARMOR_MATERIALS.register(bus);

        // GeckoLib 4.x no NeoForge se auto-inicializa — não é preciso chamar GeckoLib.initialize().
        // A rede (payload do osso) é registrada via @EventBusSubscriber em ModNetwork.
    }
}
