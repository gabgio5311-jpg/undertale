package com.example.undertale;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/**
 * Material da armadura do Sans.
 *
 * No 1.21.1 o {@code ArmorMaterial} deixou de ser uma enum/interface e virou um
 * {@code record} REGISTRADO (Holder) no registro {@link Registries#ARMOR_MATERIAL}.
 *
 * O {@link ArmorMaterial.Layer} com o id "undertale:sans" faz o jogo procurar as
 * texturas NO CORPO do jogador em:
 *   assets/undertale/textures/models/armor/sans_layer_1.png  (capacete + peitoral + botas)
 *   assets/undertale/textures/models/armor/sans_layer_2.png  (calças)
 */
public class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, UndertaleMod.MOD_ID);

    public static final Holder<ArmorMaterial> SANS = ARMOR_MATERIALS.register("sans",
            () -> new ArmorMaterial(
                    // proteção por slot
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 8);
                    }),
                    15,                                  // enchantmentValue
                    SoundEvents.ARMOR_EQUIP_NETHERITE,   // som ao equipar (já é Holder<SoundEvent> no 1.21.1)
                    () -> Ingredient.of(ModItems.REAL_KNIFE.get()), // item de reparo na bigorna
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(UndertaleMod.MOD_ID, "sans"))),
                    3.0F,   // toughness (resistência extra)
                    0.1F    // knockback resistance
            ));
}
