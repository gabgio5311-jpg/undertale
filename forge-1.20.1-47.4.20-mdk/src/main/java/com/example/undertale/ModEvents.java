package com.example.undertale;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedItem = event.getCrafting();
        Player player = event.getEntity();
        Level level = player.level(); // Pega o mundo atual

        // Garante que o código só rode no lado do Servidor para não duplicar
        if (!level.isClientSide()) {

            // Verifica se o item craftado é o Indicador de LV
            if (craftedItem.is(ModItems.INDICADOR_LV.get())) {

                // Cria a Real Knife de bônus
                ItemStack facaBonus = new ItemStack(ModItems.REAL_KNIFE.get());

                // Coloca no inventário ou dropa no chão
                if (!player.getInventory().add(facaBonus)) {
                    player.drop(facaBonus, false);
                }

                // Envia a mensagem limpa, apenas uma vez
                player.sendSystemMessage(Component.literal("§4Chara: Partner."));
            }
        }
    }
}
