package net.gruposeis.mullet.entity;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MulletPozo.MOD_ID);

    public static final RegistryObject<EntityType<MrMulletEntity>> MR_MULLET =
            ENTITY_TYPES.register("mr_mullet", () -> EntityType.Builder.of(MrMulletEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(MulletPozo.MOD_ID, "mr_mullet").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

