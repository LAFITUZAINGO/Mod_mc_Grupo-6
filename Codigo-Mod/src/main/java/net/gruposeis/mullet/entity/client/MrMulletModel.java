package net.gruposeis.mullet.entity.client;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class MrMulletModel extends PlayerModel<MrMulletEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(MulletPozo.MOD_ID, "mr_mullet"), "main");

    public MrMulletModel(ModelPart root) {
        super(root, false);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    }
}

