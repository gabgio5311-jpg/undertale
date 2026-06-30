package com.example.undertale;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(UndertaleMod.MOD_ID)
public class UndertaleMod {
    public static final String MOD_ID = "undertale";

    public UndertaleMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        // Inicializa o GeckoLib (animações da entidade do osso do Sans).
        GeckoLib.initialize();

        ModItems.ITEMS.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);
        ModEntities.ENTITIES.register(bus);

        // Canal de rede (tecla do osso: cliente -> servidor).
        ModNetwork.register();
    }
}
