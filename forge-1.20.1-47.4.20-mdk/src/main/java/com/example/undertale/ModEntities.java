package com.example.undertale;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro das entidades do mod. Por enquanto só o osso do Sans (bone_armor_sans).
 *
 * Como tudo no mod, precisa ser registrado no bus de eventos do mod
 * (ver {@link UndertaleMod}).
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UndertaleMod.MOD_ID);

    public static final RegistryObject<EntityType<BoneArmorSansEntity>> BONE_ARMOR_SANS =
            ENTITIES.register("bone_armor_sans",
                    () -> EntityType.Builder.<BoneArmorSansEntity>of(BoneArmorSansEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)            // tamanho da hitbox (puramente visual aqui)
                            .clientTrackingRange(10)       // distância em que o cliente "vê" a entidade
                            .updateInterval(1)             // sincroniza todo tick
                            .build("bone_armor_sans"));
}
