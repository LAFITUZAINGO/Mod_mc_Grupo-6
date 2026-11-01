package net.gruposeis.mullet.entity;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.gruposeis.mullet.entity.custom.ProfesorMulletEntity;
import net.gruposeis.mullet.entity.projectile.CardProjectileEntity;
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

    // Jefe de 5 bloques de altura y proporción equilibrada (5.0 / 3 ≈ 1.67 bloques de ancho)
    public static final RegistryObject<EntityType<MrMulletEntity>> MR_MULLET =
            ENTITY_TYPES.register("mr_mullet", () -> EntityType.Builder.of(MrMulletEntity::new, MobCategory.MONSTER)
                    .sized(1.67F, 5.0F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(MulletPozo.MOD_ID, "mr_mullet").toString()));

    // Proyectil estilo wither con textura de tarjeta amarilla / roja
    public static final RegistryObject<EntityType<CardProjectileEntity>> CARD_PROJECTILE =
            ENTITY_TYPES.register("card_projectile", () -> EntityType.Builder.<CardProjectileEntity>of(CardProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(new ResourceLocation(MulletPozo.MOD_ID, "card_projectile").toString()));

    // NPC Profesor Mullet rescatado tras completar la mazmorra
    public static final RegistryObject<EntityType<ProfesorMulletEntity>> PROFESOR_MULLET =
            ENTITY_TYPES.register("profesor_mullet", () -> EntityType.Builder.of(ProfesorMulletEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(MulletPozo.MOD_ID, "profesor_mullet").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

