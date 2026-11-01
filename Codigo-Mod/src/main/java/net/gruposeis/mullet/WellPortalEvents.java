package net.gruposeis.mullet;

import net.gruposeis.mullet.entity.custom.MrMulletEntity;
import net.gruposeis.mullet.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MulletPozo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WellPortalEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity item) {
            if (item.getItem().is(ModItems.MONEDA.get())) {
                WellPortalManager.trackCoin(item);
            }
        }

        // Asegurar que nunca haya más de un Mr Mullet / jefe en el nivel
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.gruposeis.mullet.entity.custom.MrMulletEntity mrMullet) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                DungeonProtectionData data = DungeonProtectionData.get(serverLevel);
                if (data.isMrMulletDefeated()) {
                    event.setCanceled(true);
                    return;
                }
                for (net.minecraft.world.entity.Entity e : serverLevel.getAllEntities()) {
                    if (e != mrMullet && e instanceof net.gruposeis.mullet.entity.custom.MrMulletEntity && e.isAlive()) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide() && event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            WellPortalManager.tickCoins(serverLevel);

            // En cada fase/sala de la dimensión Tierras Profundas, cuando queden solo tres mobs (o menos), dejarlos con Luminiscencia
            if (serverLevel.dimension() == WellPortalManager.TIERRAS_PROFUNDAS && serverLevel.getGameTime() % 20L == 0L) {
                WellPortalManager.cleanupTrappedMazeMobs(serverLevel);
                WellPortalManager.updateAllPhasesLuminiscence(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != WellPortalManager.TIERRAS_PROFUNDAS) return;

        WellPortalManager.checkDungeonRoomClear(serverLevel, event.getEntity());

        // Al morir un mob en Tierras Profundas, actualizar de inmediato la luminiscencia para las fases donde queden 3 o menos
        WellPortalManager.updateAllPhasesLuminiscence(serverLevel);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            if (player.level().dimension() == Level.OVERWORLD && !player.isSpectator()) {
                // Detección inmediata en cada tick: si el jugador hace contacto con el portal del pozo,
                // se teletransporta en ese mismo instante sin caer en el hueco ni ver el interior del pozo.
                if (WellPortalManager.isPlayerInWellPortal(player)) {
                    WellPortalManager.teleportToDungeonDimension(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // El portal reutiliza la textura y apariencia del portal del End, pero se intercepta el viaje para
            // que NUNCA vaya al End vainilla y siempre conduzca a la dimensión Tierras Profundas.
            if (event.getDimension() == Level.END && player.level().dimension() == Level.OVERWORLD) {
                if (!WellPortalManager.hasEndPortalFrameNearby(player.level(), player.blockPosition())) {
                    event.setCanceled(true);
                    WellPortalManager.teleportToDungeonDimension(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getTo() == WellPortalManager.TIERRAS_PROFUNDAS) {
                ServerLevel dungeonWorld = player.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                if (dungeonWorld != null) {
                    WellPortalManager.ensureDungeonPlaced(dungeonWorld);
                }
            }
            // Al completar la carga de la dimensión, quitar el fundido negro
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.DARKNESS);
            player.getPersistentData().remove("MulletTeleporting");
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        // 1. Mensaje y retroalimentación al interactuar con barreras invisibles en la dungeon
        if (level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS && level.getBlockState(pos).is(Blocks.BARRIER)) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(
                        Component.literal("§c§l[Mazmorra]§r §cEsta sala está bloqueada por una barrera invisible. ¡Elimina a todos los enemigos para avanzar!"),
                        true
                );
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.02);
                }
                player.playSound(SoundEvents.CHEST_LOCKED, 0.7F, 1.2F);
            }
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }

        // 2. Abrir la puerta si el jugador hace click derecho en la lana roja de las puertas
        if (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.RED_WOOL)) {
                if (!level.isClientSide()) {
                    WellPortalManager.tryOpenRedWoolDoor(level, pos, event.getEntity());
                }
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        // 3. Efecto de sonido al interactuar con las tarjetas amarilla o roja
        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.TARJETA_ROJA.get()) || stack.is(ModItems.TARJETA_AMARILLA.get())) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, net.gruposeis.mullet.sound.ModSounds.TARJETA_PONER.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS && level.getBlockState(pos).is(Blocks.BARRIER)) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(
                        Component.literal("§c§l[Mazmorra]§r §cEsta sala está bloqueada por una barrera invisible. ¡Elimina a todos los enemigos para avanzar!"),
                        true
                );
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.02);
                }
                player.playSound(SoundEvents.CHEST_LOCKED, 0.7F, 1.2F);
            }
            event.setCanceled(true);
        }
    }
}
