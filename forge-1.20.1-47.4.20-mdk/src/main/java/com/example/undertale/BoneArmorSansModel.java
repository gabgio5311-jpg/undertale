package com.example.undertale;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Aponta o GeckoLib para os arquivos do osso (exportados do Blockbench):
 *   - modelo:    assets/undertale/geo/bone_armor_sans.geo.json
 *   - animações: assets/undertale/animations/bone_armor_sans.animation.json
 *   - textura:   depende do MODO (ver {@link #getTextureResource}):
 *       * voando   -> textures/entity/bone_armor_sans_flying.png
 *       * atacando -> textures/entity/bone_armor_sans_attacking.png
 */
public class BoneArmorSansModel extends GeoModel<BoneArmorSansEntity> {

    public static final ResourceLocation TEX_FLYING =
            new ResourceLocation(UndertaleMod.MOD_ID, "textures/entity/bone_armor_sans_flying.png");
    public static final ResourceLocation TEX_ATTACKING =
            new ResourceLocation(UndertaleMod.MOD_ID, "textures/entity/bone_armor_sans_attacking.png");

    @Override
    public ResourceLocation getModelResource(BoneArmorSansEntity entity) {
        return new ResourceLocation(UndertaleMod.MOD_ID, "geo/bone_armor_sans.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BoneArmorSansEntity entity) {
        return entity.getMode() == BoneArmorSansEntity.Mode.FOLLOW ? TEX_FLYING : TEX_ATTACKING;
    }

    @Override
    public ResourceLocation getAnimationResource(BoneArmorSansEntity entity) {
        return new ResourceLocation(UndertaleMod.MOD_ID, "animations/bone_armor_sans.animation.json");
    }
}
