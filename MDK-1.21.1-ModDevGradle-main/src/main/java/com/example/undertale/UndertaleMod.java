package com.example.undertale;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(UndertaleMod.MOD_ID)
public class UndertaleMod {
    public static final String MOD_ID = "undertale";

    public UndertaleMod(IEventBus bus) {
        ModItems.ITEMS.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);
    }
}