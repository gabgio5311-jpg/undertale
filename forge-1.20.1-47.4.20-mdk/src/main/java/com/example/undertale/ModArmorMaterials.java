package com.example.undertale;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    // O nome "undertale:sans" faz o Minecraft procurar as texturas da armadura NO CORPO do jogador em:
    //   assets/undertale/textures/models/armor/sans_layer_1.png  (capacete + peitoral + botas)
    //   assets/undertale/textures/models/armor/sans_layer_2.png  (calças)
    // É SÓ COLOCAR ESSAS 2 IMAGENS NESSE CAMINHO. Não existe JSON pra armadura no corpo.
    SANS("undertale:sans", 37,
            // proteção por slot na ordem: FEET(botas), LEGS(calças), CHEST(peitoral), HEAD(capacete)
            new int[]{3, 6, 8, 3},
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,   // toughness (resistência extra)
            0.1F,   // knockback resistance
            () -> Ingredient.of(ModItems.REAL_KNIFE.get())); // item usado pra reparar na bigorna

    // vida-base por slot, mesma ordem: FEET, LEGS, CHEST, HEAD
    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantmentValue,
                      SoundEvent equipSound, float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    @Override
    public int getDurabilityForType(ArmorItem.@NotNull Type type) {
        return HEALTH_PER_SLOT[type.getSlot().getIndex()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.@NotNull Type type) {
        return this.protectionAmounts[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
