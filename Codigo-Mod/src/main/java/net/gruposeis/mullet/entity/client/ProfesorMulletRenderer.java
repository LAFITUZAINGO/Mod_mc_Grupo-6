package net.gruposeis.mullet.entity.client;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.ProfesorMulletEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ProfesorMulletRenderer extends HumanoidMobRenderer<ProfesorMulletEntity, PlayerModel<ProfesorMulletEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MulletPozo.MOD_ID, "textures/entity/jefe-profe.png");

    public ProfesorMulletRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ProfesorMulletEntity entity) {
        return TEXTURE;
    }
}

