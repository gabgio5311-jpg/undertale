package com.example.undertale;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UndertaleMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> UMA_TAB = CREATIVE_TABS.register("uma_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.undertale"))
                    // Lembre-se: agora usamos os nomes das constantes que atualizamos no ModItems (todas em MAIÚSCULAS)
                    .icon(() -> ModItems.REAL_KNIFE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.REAL_KNIFE.get());
                        output.accept(ModItems.CONTADOR_MORTE.get());
                        output.accept(ModItems.INDICADOR_LV.get());
                        output.accept(ModItems.THE_REAL_KNIFE.get());
                        output.accept(ModItems.SANS_HELMET.get());
                        output.accept(ModItems.SANS_CHESTPLATE.get());
                        output.accept(ModItems.SANS_LEGGINGS.get());
                        output.accept(ModItems.SANS_BOOTS.get());
                        output.accept(ModItems.SANS_BONES.get());

    }).build());
}
