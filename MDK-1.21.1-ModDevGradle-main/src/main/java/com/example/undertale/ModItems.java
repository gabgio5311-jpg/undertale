package com.example.undertale;


import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UndertaleMod.MOD_ID);


    // Registro da Real Knife (espada de diamante comum; é o item de reparo da armadura Sans)
    public static final DeferredItem<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))));

    public static final DeferredItem<Item> CONTADOR_MORTE = ITEMS.register("contador_morte",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> INDICADOR_LV = ITEMS.register("indicador_lv",
            () -> new IndicadorLvItem(new Item.Properties().stacksTo(1))); // stacksTo(1) faz ele não acumular no inventário

    // The Real Knife: hitkill em qualquer coisa. Dano +900 e velocidade de espada (-2.4), Tier Netherite.
    public static final DeferredItem<Item> THE_REAL_KNIFE = ITEMS.register("the_real_knife",
            () -> new TheRealKnifeItem(new Item.Properties().stacksTo(1).fireResistant()
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 900, -2.4F))));

    // Armadura do Sans (set completo). Usa SansArmorItem (material ModArmorMaterials.SANS).
    // Os bônus de set (voo, invencibilidade, etc.) ficam em SansArmorHandler.
    public static final DeferredItem<Item> SANS_HELMET = ITEMS.register("sans_helmet",
            () -> new SansArmorItem(ArmorItem.Type.HELMET));
    public static final DeferredItem<Item> SANS_CHESTPLATE = ITEMS.register("sans_chestplate",
            () -> new SansArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<Item> SANS_LEGGINGS = ITEMS.register("sans_leggings",
            () -> new SansArmorItem(ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<Item> SANS_BOOTS = ITEMS.register("sans_boots",
            () -> new SansArmorItem(ArmorItem.Type.BOOTS));

    // "Blue Bones": ingrediente de craft da armadura (item comum, NÃO é armadura).
    public static final DeferredItem<Item> SANS_BONES = ITEMS.register("sans_bones",
            () -> new Item(new Item.Properties()));
}
