package net.gruposeis.mullet.entity.client;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MrMulletRenderer extends HumanoidMobRenderer<MrMulletEntity, MrMulletModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MulletPozo.MOD_ID, "textures/entity/jefe.png");

    public MrMulletRenderer(EntityRendererProvider.Context context) {
        super(context, new MrMulletModel(context.bakeLayer(MrMulletModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MrMulletEntity entity) {
        return TEXTURE;
    }
}

