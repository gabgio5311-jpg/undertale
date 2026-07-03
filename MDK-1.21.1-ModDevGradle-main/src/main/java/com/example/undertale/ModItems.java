package com.example.undertale;


import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UndertaleMod.MOD_ID);


    // Real Knife (comum): espada de netherite cujo dano ESCALA com o LV do jogador
    // (bônus aplicado em LvEvents.onRealKnifeHit; tooltip em RealKnifeItem). É a ferramenta
    // de farm de LV e o item de reparo da armadura Sans.
    public static final DeferredItem<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new RealKnifeItem(new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4F))
                    // Indestrutível: o Tier Netherite dá durabilidade no construtor do SwordItem,
                    // então o componente UNBREAKABLE impede que ela seja consumida. false = sem
                    // a linha "Inquebrável" no tooltip.
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(false))));

    // Death Counter: dropa de qualquer mob; o tooltip mostra a chance (DropInfoItem).
    public static final DeferredItem<Item> CONTADOR_MORTE = ITEMS.register("contador_morte",
            () -> new DropInfoItem(new Item.Properties(),
                    ContadorMorteDropHandler.DROP_CHANCE, ContadorMorteDropHandler.LOOTING_BONUS, "any mob"));

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
    // Dropa de esqueletos; o tooltip mostra a chance (DropInfoItem).
    public static final DeferredItem<Item> SANS_BONES = ITEMS.register("sans_bones",
            () -> new DropInfoItem(new Item.Properties(),
                    SansBonesDropHandler.DROP_CHANCE, SansBonesDropHandler.LOOTING_BONUS, "skeletons"));

    // Chara: item de sabor / ícone do advancement do LV 20 (sem comportamento próprio).
    public static final DeferredItem<Item> CHARA = ITEMS.register("chara",
            () -> new Item(new Item.Properties()));
}
