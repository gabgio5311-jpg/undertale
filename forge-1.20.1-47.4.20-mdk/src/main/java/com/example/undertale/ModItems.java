package com.example.undertale;


import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UndertaleMod.MOD_ID);


    // Registro da Real Knife
    public static final RegistryObject<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new RealKnifeItem(new Item.Properties()));
    public static final RegistryObject<Item> CONTADOR_MORTE = ITEMS.register("contador_morte",
            () -> new DropInfoItem(new Item.Properties(),
                    ContadorMorteDropHandler.DROP_CHANCE, ContadorMorteDropHandler.LOOTING_BONUS, "any mob"));
    public static final RegistryObject<Item> INDICADOR_LV = ITEMS.register("indicador_lv",
            () -> new IndicadorLvItem(new Item.Properties().stacksTo(1))); // stacksTo(1) faz ele não acumular no inventário
    public static final RegistryObject<Item> THE_REAL_KNIFE = ITEMS.register("the_real_knife",
            () -> new TheRealKnifeItem(new Item.Properties().stacksTo(1)));
    // Armadura do Sans (set completo). Usa SansArmorItem (material ModArmorMaterials.SANS).
    // Os bônus de set (voo, invencibilidade, etc.) ficam em SansArmorHandler.
    public static final RegistryObject<Item> SANS_HELMET = ITEMS.register("sans_helmet",
            () -> new SansArmorItem(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> SANS_CHESTPLATE = ITEMS.register("sans_chestplate",
            () -> new SansArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> SANS_LEGGINGS = ITEMS.register("sans_leggings",
            () -> new SansArmorItem(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> SANS_BOOTS = ITEMS.register("sans_boots",
            () -> new SansArmorItem(ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> SANS_BONES = ITEMS.register("sans_bones",
            () -> new DropInfoItem(new Item.Properties(),
                    SansBonesDropHandler.DROP_CHANCE, SansBonesDropHandler.LOOTING_BONUS, "skeletons"));
    public static final RegistryObject<Item> CHARA = ITEMS.register("chara",
            () -> new Item(new Item.Properties()));
}
