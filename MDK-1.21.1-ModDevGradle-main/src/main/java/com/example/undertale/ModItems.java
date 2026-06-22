package com.example.undertale;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UndertaleMod.MOD_ID);


    // Registro da Real Knife
    public static final DeferredItem<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))));
    public static final DeferredItem<Item> CONTADOR_MORTE = ITEMS.register("contador_morte",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INDICADOR_LV = ITEMS.register("indicador_lv",
            () -> new IndicadorLvItem(new Item.Properties().stacksTo(1))); // stacksTo(1) faz ele não acumular no inventário
}

