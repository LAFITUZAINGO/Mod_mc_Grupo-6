package net.gruposeis.mullet.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MrMulletRenderer extends HumanoidMobRenderer<MrMulletEntity, MrMulletModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MulletPozo.MOD_ID, "textures/entity/jefe.png");

    // Factor de escala: altura objetivo 5.0 bloques / altura humana estándar 1.8 bloques
    public static final float BOSS_SCALE = 5.0F / 1.8F;

    public MrMulletRenderer(EntityRendererProvider.Context context) {
        super(context, new MrMulletModel(context.bakeLayer(MrMulletModel.LAYER_LOCATION)), 0.5F * BOSS_SCALE);
    }

    @Override
    protected void scale(MrMulletEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(BOSS_SCALE, BOSS_SCALE, BOSS_SCALE);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(MrMulletEntity entity) {
        return TEXTURE;
    }
}

