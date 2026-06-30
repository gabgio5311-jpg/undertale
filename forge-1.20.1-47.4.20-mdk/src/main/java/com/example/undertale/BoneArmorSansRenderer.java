package com.example.undertale;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib do osso. A textura muda conforme o modo (voando vs atacando),
 * delegando pro {@link BoneArmorSansModel}.
 *
 * O modelo do osso é VERTICAL (no eixo Y). Aqui a gente DEITA ele (90° no eixo X)
 * pra ele ficar horizontal — "reto" quando arremessado e "deitado" embaixo do
 * jogador. A direção (pra onde aponta) vem do yaw da entidade, então o osso
 * jogado deita ao longo da trajetória. Registrado em {@link ClientModEvents}.
 */
public class BoneArmorSansRenderer extends GeoEntityRenderer<BoneArmorSansEntity> {

    // Quanto deitar o osso. 90° = vertical -> horizontal. Troque o sinal pra inverter a ponta.
    private static final float LAY_FLAT_DEG = 90.0F;

    public BoneArmorSansRenderer(EntityRendererProvider.Context context) {
        super(context, new BoneArmorSansModel());
    }

    @Override
    protected void applyRotations(BoneArmorSansEntity entity, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        // super já aplica o YAW (direção horizontal): trajetória do arremesso (THROWN)
        // ou direção do corpo (FOLLOW, que usa o yBodyRot).
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick);

        // O osso de ATAQUE também acompanha o PITCH da mira (olhar pra cima/baixo).
        // O osso que VOA fica reto na horizontal (só direção do corpo).
        float pitch = (entity.getMode() == BoneArmorSansEntity.Mode.THROWN) ? entity.getXRot() : 0.0F;

        // Deita o osso (vertical -> horizontal) e, no ataque, soma o pitch da mira.
        // Se o pitch sair invertido (olhar pra cima aponta pra baixo), troque "+ pitch" por "- pitch".
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEG + pitch));
    }

    @Override
    public ResourceLocation getTextureLocation(BoneArmorSansEntity entity) {
        return entity.getMode() == BoneArmorSansEntity.Mode.FOLLOW
                ? BoneArmorSansModel.TEX_FLYING
                : BoneArmorSansModel.TEX_ATTACKING;
    }
}
