package com.example.undertale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro das entidades do mod. Por enquanto só o osso do Sans (bone_armor_sans).
 *
 * Como tudo no mod, é registrado no bus de eventos do mod (ver {@link UndertaleMod}).
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, UndertaleMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BoneArmorSansEntity>> BONE_ARMOR_SANS =
            ENTITY_TYPES.register("bone_armor_sans",
                    () -> EntityType.Builder.of(BoneArmorSansEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)            // tamanho da hitbox (puramente visual aqui)
                            .clientTrackingRange(10)       // distância em que o cliente "vê" a entidade
                            .updateInterval(1)             // sincroniza todo tick
                            .build("bone_armor_sans"));
}
