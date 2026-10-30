package net.gruposeis.mullet;

import com.mojang.logging.LogUtils;
import net.gruposeis.mullet.block.ModBlocks;
import net.gruposeis.mullet.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WellPortalManager {
    // Clave de registro para la dimensión de vacío personalizada "Tierras Profundas"
    public static final ResourceKey<Level> TIERRAS_PROFUNDAS = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(MulletPozo.MOD_ID, "tierras_profundas")
    );

    // Portales registrados en memoria (con respaldo de validación estructural)
    private static final Set<BlockPos> REGISTERED_PORTALS = Collections.synchronizedSet(new HashSet<>());
    private static final Set<ItemEntity> TRACKED_COINS = Collections.synchronizedSet(new HashSet<>());

    public static void trackCoin(ItemEntity entity) {
        TRACKED_COINS.add(entity);
    }

    public static void tickCoins(ServerLevel level) {
        if (TRACKED_COINS.isEmpty()) return;

        TRACKED_COINS.removeIf(item -> {
            if (!item.isAlive() || item.level() != level) {
                return !item.isAlive();
            }

            BlockPos pos = item.blockPosition();
            BlockState state = level.getBlockState(pos);
            BlockPos targetWaterPos = null;

            if (state.is(Blocks.WATER)) {
                targetWaterPos = pos;
            } else if (level.getBlockState(pos.below()).is(Blocks.WATER)) {
                targetWaterPos = pos.below();
            }

            if (targetWaterPos != null && isWellWater(level, targetWaterPos)) {
                activateWell(level, targetWaterPos, item);
                return true;
            }

            return false;
        });
    }

    private static boolean isWellWater(Level level, BlockPos pos) {
        // Encontrar límites de agua contigua horizontalmente en el nivel pos.getY()
        int minX = pos.getX();
        while (level.getBlockState(new BlockPos(minX - 1, pos.getY(), pos.getZ())).is(Blocks.WATER)) minX--;
        int maxX = pos.getX();
        while (level.getBlockState(new BlockPos(maxX + 1, pos.getY(), pos.getZ())).is(Blocks.WATER)) maxX++;

        int minZ = pos.getZ();
        while (level.getBlockState(new BlockPos(pos.getX(), pos.getY(), minZ - 1)).is(Blocks.WATER)) minZ--;
        int maxZ = pos.getZ();
        while (level.getBlockState(new BlockPos(pos.getX(), pos.getY(), maxZ + 1)).is(Blocks.WATER)) maxZ++;

        int widthX = maxX - minX + 1;
        int widthZ = maxZ - minZ + 1;

        // La piscina del pozo es de 3x3 (permitimos entre 2 y 4 por flexibilidad)
        if (widthX >= 2 && widthX <= 4 && widthZ >= 2 && widthZ <= 4) {
            BlockPos checkDown = pos;
            while (level.getBlockState(checkDown.below()).is(Blocks.WATER)) {
                checkDown = checkDown.below();
            }
            BlockState bottomState = level.getBlockState(checkDown.below());
            // En pozo.nbt el fondo es black_stained_glass o bloques de piedra/deepslate
            return bottomState.is(Blocks.BLACK_STAINED_GLASS)
                    || bottomState.is(Blocks.STONE_BRICKS)
                    || bottomState.is(Blocks.DEEPSLATE_BRICKS)
                    || bottomState.is(Blocks.CRACKED_STONE_BRICKS)
                    || bottomState.is(Blocks.MOSSY_STONE_BRICKS);
        }
        return false;
    }

    private static void activateWell(ServerLevel level, BlockPos waterPos, ItemEntity coin) {
        // Encontrar dimensiones completas de la columna de agua
        int minX = waterPos.getX();
        while (level.getBlockState(new BlockPos(minX - 1, waterPos.getY(), waterPos.getZ())).is(Blocks.WATER)) minX--;
        int maxX = waterPos.getX();
        while (level.getBlockState(new BlockPos(maxX + 1, waterPos.getY(), waterPos.getZ())).is(Blocks.WATER)) maxX++;

        int minZ = waterPos.getZ();
        while (level.getBlockState(new BlockPos(waterPos.getX(), waterPos.getY(), minZ - 1)).is(Blocks.WATER)) minZ--;
        int maxZ = waterPos.getZ();
        while (level.getBlockState(new BlockPos(waterPos.getX(), waterPos.getY(), maxZ + 1)).is(Blocks.WATER)) maxZ++;

        int maxY = waterPos.getY();
        while (level.getBlockState(new BlockPos(waterPos.getX(), maxY + 1, waterPos.getZ())).is(Blocks.WATER)) maxY++;

        int minY = waterPos.getY();
        while (level.getBlockState(new BlockPos(waterPos.getX(), minY - 1, waterPos.getZ())).is(Blocks.WATER)) minY--;

        // 1. Eliminar la moneda arrojada
        coin.discard();

        // 2. Reemplazar el agua: la capa superior se convierte en el bloque de portal (reutilizando la textura del End)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos portalPos = new BlockPos(x, maxY, z);
                level.setBlock(portalPos, Blocks.END_PORTAL.defaultBlockState(), 3);
                REGISTERED_PORTALS.add(portalPos);

                // Debajo del portal dejamos aire para crear el pozo oscuro
                for (int y = minY; y < maxY; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // 3. Efectos visuales y sonoros
        double cx = (minX + maxX + 1) / 2.0;
        double cy = maxY + 0.5;
        double cz = (minZ + maxZ + 1) / 2.0;

        level.playSound(null, new BlockPos((int)cx, maxY, (int)cz), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, new BlockPos((int)cx, maxY, (int)cz), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.7F, 1.2F);

        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.5, cz, 120, 1.2, 0.4, 1.2, 0.2);
        level.sendParticles(ParticleTypes.FLASH, cx, cy + 0.5, cz, 3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, cx, cy + 0.2, cz, 40, 1.0, 0.2, 1.0, 0.05);

        // Mensaje a jugadores cercanos
        level.players().forEach(player -> {
            if (player.distanceToSqr(cx, cy, cz) < 64 * 64) {
                player.sendSystemMessage(Component.literal("§5§l[Pozo Mullet]§r §d¡El sello se ha roto! El portal a las Tierras Profundas está activo."));
            }
        });
    }

    public static boolean isWellPortal(Level level, BlockPos pos) {
        BlockPos[] checks = new BlockPos[]{pos, pos.below(), pos.above(), pos.above(2)};
        for (BlockPos p : checks) {
            if (REGISTERED_PORTALS.contains(p)) {
                return true;
            }
            if (level.getBlockState(p).is(Blocks.END_PORTAL)) {
                // Si es un bloque de portal pero NO está rodeado por marcos del End (Stronghold), es nuestro pozo
                if (!hasEndPortalFrameNearby(level, p)) {
                    REGISTERED_PORTALS.add(p);
                    return true;
                }
            }
        }
        return false;
    }

    public static Double getWellPortalY(Level level, net.minecraft.world.phys.AABB box) {
        BlockPos min = new BlockPos((int) Math.floor(box.minX), (int) Math.floor(box.minY), (int) Math.floor(box.minZ));
        BlockPos max = new BlockPos((int) Math.floor(box.maxX), (int) Math.floor(box.maxY), (int) Math.floor(box.maxZ));

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (REGISTERED_PORTALS.contains(p) || (level.getBlockState(p).is(Blocks.END_PORTAL) && !hasEndPortalFrameNearby(level, p))) {
                REGISTERED_PORTALS.add(p.immutable());
                return (double) p.getY();
            }
        }
        return null;
    }

    private static boolean hasEndPortalFrameNearby(Level level, BlockPos pos) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (level.getBlockState(pos.offset(dx, 0, dz)).is(Blocks.END_PORTAL_FRAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Origen de coordenadas donde se coloca la dungeon en la dimensión Tierras Profundas
    public static final BlockPos DUNGEON_ORIGIN = new BlockPos(0, 40, 0);

    // Desplazamiento exacto de la sala con amatistas en forma de cruz dentro de la dungeon
    // La amatista está en Y=28, por lo que el jugador se posiciona sobre ella en Y=29
    public static final BlockPos AMETHYST_CROSS_OFFSET = new BlockPos(261, 29, 35);

    // Posición del bloque para volver al Overworld en la habitación negra con bloques verdes
    public static final BlockPos BLACK_ROOM_RETURN_OFFSET = new BlockPos(101, 28, 180);

    public static void ensureReturnBlock(ServerLevel dungeonWorld) {
        BlockPos returnPos = DUNGEON_ORIGIN.offset(BLACK_ROOM_RETURN_OFFSET);
        if (!dungeonWorld.getBlockState(returnPos).is(ModBlocks.MULLET_BLOCK.get())) {
            dungeonWorld.setBlock(returnPos, ModBlocks.MULLET_BLOCK.get().defaultBlockState(), 3);
            LogUtils.getLogger().info("Bloque de retorno al Overworld asegurado en {}", returnPos);
        }
    }

    public static void ensureDungeonPlaced(ServerLevel dungeonWorld) {
        DungeonProtectionData data = DungeonProtectionData.get(dungeonWorld);
        BlockPos crossPos = DUNGEON_ORIGIN.offset(260, 28, 34);

        if (data.isDungeonPlaced() || dungeonWorld.getBlockState(crossPos).is(Blocks.AMETHYST_BLOCK)) {
            if (!data.isDungeonPlaced()) {
                data.setDungeonPlaced(true);
            }
            ensureReturnBlock(dungeonWorld);
            return;
        }

        StructureTemplateManager templateManager = dungeonWorld.getStructureManager();
        Optional<StructureTemplate> templateOpt = templateManager.get(new ResourceLocation(MulletPozo.MOD_ID, "dungeon"));

        if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false)
                    .setKnownShape(true);

            template.placeInWorld(dungeonWorld, DUNGEON_ORIGIN, DUNGEON_ORIGIN, settings, dungeonWorld.random, 2);
            data.setDungeonPlaced(true);
            ensureReturnBlock(dungeonWorld);
            LogUtils.getLogger().info("Dungeon colocada exitosamente en Tierras Profundas en la posición {}", DUNGEON_ORIGIN);
        } else {
            LogUtils.getLogger().error("No se pudo encontrar la plantilla de estructura mullet:dungeon!");
        }
    }

    public static void teleportToDungeonDimension(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // 1. Guardar la posición de retorno en el Overworld
        int returnX = player.getBlockX() + 3;
        int returnZ = player.getBlockZ();
        int surfaceY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, returnX, returnZ);
        int returnY = Math.max(player.getBlockY() + 2, surfaceY);

        CompoundTag data = player.getPersistentData();
        data.putInt("MulletReturnX", returnX);
        data.putInt("MulletReturnY", returnY);
        data.putInt("MulletReturnZ", returnZ);

        // 2. Obtener la dimensión Tierras Profundas
        ServerLevel dungeonWorld = player.server.getLevel(TIERRAS_PROFUNDAS);
        if (dungeonWorld == null) {
            player.sendSystemMessage(Component.literal("§c[Error] No se pudo cargar la dimensión Tierras Profundas."));
            return;
        }

        // 3. Generar la dungeon si aún no ha sido colocada
        ensureDungeonPlaced(dungeonWorld);

        // 4. Teletransportar al jugador a la sala de amatistas en forma de cruz
        BlockPos spawnPos = DUNGEON_ORIGIN.offset(AMETHYST_CROSS_OFFSET);
        double spawnX = spawnPos.getX() + 0.5;
        double spawnY = spawnPos.getY(); // 69.0
        double spawnZ = spawnPos.getZ() + 0.5;
        float yaw = 90.0F; // Orientado hacia el pasillo oeste de la sala
        float pitch = 0.0F;

        dungeonWorld.setDefaultSpawnPos(spawnPos, yaw);
        player.teleportTo(dungeonWorld, spawnX, spawnY, spawnZ, yaw, pitch);

        // 5. Efectos sonoros y mensajes
        dungeonWorld.playSound(null, spawnPos, ModSounds.DIMENSION_ENTRAR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        dungeonWorld.playSound(null, spawnPos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.4f, 1.0f);
        dungeonWorld.sendParticles(ParticleTypes.PORTAL, spawnX, spawnY + 0.5, spawnZ, 60, 0.6, 0.6, 0.6, 0.1);

        player.sendSystemMessage(Component.literal("§5§l[Tierras Profundas]§r §dHas ingresado a la mazmorra."));
    }

    public static void teleportBackToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // Si el jugador está en la dimensión Tierras Profundas, regresarlo al Overworld
        if (player.level().dimension() == TIERRAS_PROFUNDAS) {
            CompoundTag data = player.getPersistentData();

            if (data.contains("MulletReturnX")) {
                int rx = data.getInt("MulletReturnX");
                int ry = data.getInt("MulletReturnY");
                int rz = data.getInt("MulletReturnZ");

                data.remove("MulletReturnX");
                data.remove("MulletReturnY");
                data.remove("MulletReturnZ");

                player.teleportTo(overworld, rx + 0.5, ry, rz + 0.5, player.getYRot(), player.getXRot());
            } else {
                BlockPos spawn = overworld.getSharedSpawnPos();
                int surfaceY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, spawn.getX(), spawn.getZ());
                player.teleportTo(overworld, spawn.getX() + 0.5, surfaceY, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
            }

            overworld.playSound(null, player.blockPosition(), ModSounds.DIMENSION_SALIR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            overworld.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.5, 0.5, 0.5, 0.2);
            player.sendSystemMessage(Component.literal("§a§l[Pozo Mullet]§r §a¡Has regresado sano y salvo al Overworld!"));
        } else {
            // Ya está en el Overworld
            player.sendSystemMessage(Component.literal("§6§l[Pozo Mullet]§r §eYa te encuentras en el Overworld."));
        }
    }

    public static void ensureMrMulletSpawned(ServerLevel dungeonWorld) {
        net.minecraft.world.phys.AABB checkArea = new net.minecraft.world.phys.AABB(-15, 50, -15, 15, 80, 15);
        if (dungeonWorld.getEntitiesOfClass(net.gruposeis.mullet.entity.custom.MrMulletEntity.class, checkArea).isEmpty()) {
            net.gruposeis.mullet.entity.custom.MrMulletEntity mrMullet =
                    net.gruposeis.mullet.entity.ModEntities.MR_MULLET.get().create(dungeonWorld);
            if (mrMullet != null) {
                mrMullet.moveTo(0.5, 64.0, -1.5, 180.0F, 0.0F);
                dungeonWorld.addFreshEntity(mrMullet);
            }
        }
    }
}
