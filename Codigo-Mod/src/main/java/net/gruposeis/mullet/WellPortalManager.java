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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
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

        // 2. Llenar toda la columna de agua con bloques de portal del End.
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos portalPos = new BlockPos(x, y, z);
                    level.setBlock(portalPos, Blocks.END_PORTAL.defaultBlockState(), 3);
                    REGISTERED_PORTALS.add(portalPos.immutable());
                }
            }
        }

        // 3. Pre-generar la estructura de la dungeon en segundo plano si aún no fue colocada
        ServerLevel dungeonWorld = level.getServer().getLevel(TIERRAS_PROFUNDAS);
        if (dungeonWorld != null) {
            ensureDungeonPlaced(dungeonWorld);
        }

        // 4. Efectos visuales y sonoros épicos
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

    public static boolean isPlayerInWellPortal(ServerPlayer player) {
        if (player.level().dimension() != Level.OVERWORLD) return false;
        long cooldown = player.getPersistentData().getLong("MulletPortalCooldown");
        if (System.currentTimeMillis() < cooldown) return false;
        if (player.getPersistentData().getBoolean("MulletTeleporting")) return false;

        return getWellPortalY(player.level(), player.getBoundingBox().inflate(0.1)) != null;
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

    // Desplazamiento exacto del centro de la sala circular donde se encuentra el boss Mr Mullet
    public static final BlockPos CIRCULAR_ROOM_BOSS_OFFSET = new BlockPos(102, 28, 106);

    // Identificadores de las barreras invisibles entre salas de la mazmorra
    public static final String GATE_F1_N1 = "f1_n1";
    public static final String GATE_F1_N2 = "f1_n2";
    public static final String GATE_F1_N3 = "f1_n3";
    public static final String GATE_F2_N1 = "f2_n1";
    public static final String GATE_F2_N2 = "f2_n2";
    public static final String GATE_F2_N3 = "f2_n3";

    public static void ensureReturnBlock(ServerLevel dungeonWorld) {
        BlockPos returnPos = DUNGEON_ORIGIN.offset(BLACK_ROOM_RETURN_OFFSET);
        if (!dungeonWorld.getBlockState(returnPos).is(ModBlocks.MULLET_BLOCK.get())) {
            dungeonWorld.setBlock(returnPos, ModBlocks.MULLET_BLOCK.get().defaultBlockState(), 3);
            LogUtils.getLogger().info("Bloque de retorno al Overworld asegurado en {}", returnPos);
        }
    }

    public static void ensureDungeonPlaced(ServerLevel dungeonWorld) {
        DungeonProtectionData data = DungeonProtectionData.get(dungeonWorld);

        // 1. Si los datos guardados ya indican que la dungeon fue colocada, salir inmediatamente
        if (data.isDungeonPlaced()) {
            ensureReturnBlock(dungeonWorld);
            ensureMrMulletSpawned(dungeonWorld);
            ensureDungeonMobsSpawned(dungeonWorld);
            placeDungeonBarriers(dungeonWorld);
            return;
        }

        // 2. Validación física de seguridad cargando el chunk de la sala de amatista
        BlockPos crossPos = DUNGEON_ORIGIN.offset(260, 28, 34);
        dungeonWorld.getChunkSource().getChunk(crossPos.getX() >> 4, crossPos.getZ() >> 4, ChunkStatus.FULL, true);
        if (dungeonWorld.getBlockState(crossPos).is(Blocks.AMETHYST_BLOCK)) {
            data.setDungeonPlaced(true);
            ensureReturnBlock(dungeonWorld);
            ensureMrMulletSpawned(dungeonWorld);
            ensureDungeonMobsSpawned(dungeonWorld);
            placeDungeonBarriers(dungeonWorld);
            return;
        }

        // 3. Colocar la dungeon por primera y única vez en la dimensión
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
            ensureMrMulletSpawned(dungeonWorld);
            ensureDungeonMobsSpawned(dungeonWorld);
            placeDungeonBarriers(dungeonWorld);
            LogUtils.getLogger().info("Dungeon colocada exitosamente por primera y única vez en Tierras Profundas en la posición {}", DUNGEON_ORIGIN);
        } else {
            LogUtils.getLogger().error("No se pudo encontrar la plantilla de estructura mullet:dungeon!");
        }
    }

    public static void teleportToDungeonDimension(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("MulletTeleporting")) {
            return;
        }
        player.getPersistentData().putBoolean("MulletTeleporting", true);

        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            player.getPersistentData().remove("MulletTeleporting");
            return;
        }

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
            player.getPersistentData().remove("MulletTeleporting");
            player.sendSystemMessage(Component.literal("§c[Error] No se pudo cargar la dimensión Tierras Profundas."));
            return;
        }

        // 3. Generar la dungeon si aún no ha sido colocada
        ensureDungeonPlaced(dungeonWorld);

        // 4. Efecto de transición inmediata (fundido negro limpio): oculta cualquier frame residual
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 1, false, false, false));

        // 5. Teletransportar al jugador a la sala de amatistas en forma de cruz
        BlockPos spawnPos = DUNGEON_ORIGIN.offset(AMETHYST_CROSS_OFFSET);
        double spawnX = spawnPos.getX() + 0.5;
        double spawnY = spawnPos.getY(); // 69.0
        double spawnZ = spawnPos.getZ() + 0.5;
        float yaw = 90.0F; // Orientado hacia el pasillo oeste de la sala
        float pitch = 0.0F;

        dungeonWorld.setDefaultSpawnPos(spawnPos, yaw);
        player.teleportTo(dungeonWorld, spawnX, spawnY, spawnZ, yaw, pitch);

        // 6. Efectos sonoros y partículas
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

            // Cooldown de 3 segundos para que no se re-teletransporte accidentalmente al reaparecer
            player.getPersistentData().putLong("MulletPortalCooldown", System.currentTimeMillis() + 3000);

            overworld.playSound(null, player.blockPosition(), ModSounds.DIMENSION_SALIR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            overworld.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.5, 0.5, 0.5, 0.2);
            player.sendSystemMessage(Component.literal("§a§l[Pozo Mullet]§r §a¡Has regresado sano y salvo al Overworld!"));
        } else {
            // Ya está en el Overworld
            player.sendSystemMessage(Component.literal("§6§l[Pozo Mullet]§r §eYa te encuentras en el Overworld."));
        }
    }

    public static void ensureMrMulletSpawned(ServerLevel dungeonWorld) {
        DungeonProtectionData data = DungeonProtectionData.get(dungeonWorld);
        // Si el jefe ya fue derrotado, NO volver a spawnear jamás
        if (data.isMrMulletDefeated()) {
            return;
        }

        BlockPos bossPos = DUNGEON_ORIGIN.offset(CIRCULAR_ROOM_BOSS_OFFSET);
        // Asegurar que el chunk de la sala circular esté cargado
        dungeonWorld.getChunkSource().getChunk(bossPos.getX() >> 4, bossPos.getZ() >> 4, ChunkStatus.FULL, true);

        // Buscar si ya existe algún MrMullet en la dimensión (para garantizar que SOLO HAYA UNO)
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                DUNGEON_ORIGIN.getX() - 50, 0, DUNGEON_ORIGIN.getZ() - 50,
                DUNGEON_ORIGIN.getX() + 350, 150, DUNGEON_ORIGIN.getZ() + 250
        );
        List<net.gruposeis.mullet.entity.custom.MrMulletEntity> bosses =
                dungeonWorld.getEntitiesOfClass(net.gruposeis.mullet.entity.custom.MrMulletEntity.class, searchArea);

        if (!bosses.isEmpty()) {
            // Ya existe al menos uno: conservar solo el primero y remover duplicados
            boolean first = true;
            for (net.gruposeis.mullet.entity.custom.MrMulletEntity boss : bosses) {
                if (first) {
                    first = false;
                    // Si el boss existente estaba en la posición antigua de prueba, moverlo al centro de la sala circular
                    if (boss.distanceToSqr(bossPos.getX() + 0.5, bossPos.getY(), bossPos.getZ() + 0.5) > 40 * 40) {
                        boss.teleportTo(bossPos.getX() + 0.5, bossPos.getY(), bossPos.getZ() + 0.5);
                    }
                } else {
                    boss.discard(); // Descartar duplicados si los hubiera
                }
            }
            data.setMrMulletSpawned(true);
            return;
        }

        // Si no existe ninguno y no ha sido derrotado, crear exactamente un Mr Mullet en la sala circular
        net.gruposeis.mullet.entity.custom.MrMulletEntity mrMullet =
                net.gruposeis.mullet.entity.ModEntities.MR_MULLET.get().create(dungeonWorld);
        if (mrMullet != null) {
            mrMullet.moveTo(bossPos.getX() + 0.5, bossPos.getY(), bossPos.getZ() + 0.5, 180.0F, 0.0F);
            dungeonWorld.addFreshEntity(mrMullet);
            data.setMrMulletSpawned(true);
            LogUtils.getLogger().info("Mr Mullet jefe spawneado como boss unico en la sala circular en {}", bossPos);
        }
    }

    public static boolean tryOpenRedWoolDoor(Level level, BlockPos clickedPos, net.minecraft.world.entity.player.Player player) {
        if (!level.getBlockState(clickedPos).is(Blocks.RED_WOOL)) {
            return false;
        }

        // Encontrar todos los bloques de lana roja contiguos que componen la puerta
        Set<BlockPos> doorBlocks = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        doorBlocks.add(clickedPos);
        queue.add(clickedPos);

        final int MAX_DOOR_BLOCKS = 70; // Puertas de la mazmorra tienen 36 y 54 bloques. Paredes del pasillo tienen >400

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!doorBlocks.contains(neighbor) && level.getBlockState(neighbor).is(Blocks.RED_WOOL)) {
                    doorBlocks.add(neighbor);
                    queue.add(neighbor);
                    if (doorBlocks.size() > MAX_DOOR_BLOCKS) {
                        // Excede el tamaño de una puerta: es un muro/pasillo decorativo, no abrir
                        return false;
                    }
                }
            }
        }

        if (doorBlocks.isEmpty()) {
            return false;
        }

        // Determinar si es la segunda puerta (puerta de salida de la sala circular hacia la salida, Z > 100)
        boolean isSecondDoor = false;
        for (BlockPos pos : doorBlocks) {
            if (pos.getZ() > DUNGEON_ORIGIN.getZ() + 100) {
                isSecondDoor = true;
                break;
            }
        }

        // La segunda puerta solo se abre despues de derrotar al jefe
        if (isSecondDoor && level instanceof ServerLevel serverLevel) {
            DungeonProtectionData data = DungeonProtectionData.get(serverLevel);
            boolean bossAlive = false;
            for (net.minecraft.world.entity.Entity e : serverLevel.getAllEntities()) {
                if (e instanceof net.gruposeis.mullet.entity.custom.MrMulletEntity && e.isAlive()) {
                    bossAlive = true;
                    break;
                }
            }

            if (!data.isMrMulletDefeated() || bossAlive) {
                // El jefe aun no fue derrotado: no abrir la puerta y notificar al jugador
                level.playSound(null, clickedPos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, clickedPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.6F);

                for (BlockPos p : doorBlocks) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.01);
                }

                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c§l[Mazmorra]§r §cEsta puerta está sellada. Debes derrotar al jefe de la mazmorra primero."),
                            true
                    );
                }
                return false;
            }
        }

        // Abrir la puerta: sustituir la lana roja por aire
        for (BlockPos pos : doorBlocks) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.05);
            }
        }

        // Efectos de sonido de apertura
        level.playSound(null, clickedPos, ModSounds.TARJETA_PONER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, clickedPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.8F);

        if (player != null) {
            String msg = isSecondDoor ? "§a[Mazmorra] §f¡La puerta de salida se ha abierto!" : "§a[Mazmorra] §f¡La puerta se ha abierto!";
            player.displayClientMessage(Component.literal(msg), true);
        }

        return true;
    }

    public static void ensureDungeonMobsSpawned(ServerLevel dungeonWorld) {
        DungeonProtectionData data = DungeonProtectionData.get(dungeonWorld);
        if (data.isDungeonMobsSpawned()) {
            return;
        }
        spawnDungeonMobs(dungeonWorld);
        data.setDungeonMobsSpawned(true);
    }

    public static void spawnDungeonMobs(ServerLevel level) {
        LogUtils.getLogger().info("Generando mobs de la dungeon segun Dungeon.md...");

        // FASE 1 - Nivel 1 (X=[160..190], Z=[25..55]): 8 Zombies, 5 Esqueletos
        List<BlockPos> spotsF1N1 = findWalkableSpots(level, 160, 190, 25, 55);
        Collections.shuffle(spotsF1N1, new java.util.Random(101));
        int idx = 0;
        for (int i = 0; i < 8 && idx < spotsF1N1.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N1.get(idx++), "dungeon_room_f1_n1", z -> {});
        }
        for (int i = 0; i < 5 && idx < spotsF1N1.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF1N1.get(idx++), "dungeon_room_f1_n1", s -> equipBow(s, null, 0));
        }

        // FASE 1 - Nivel 2 (X=[125..155], Z=[15..45]): 12 Zombies (4 armadura hierro), 7 Esqueletos (3 arco encantado)
        List<BlockPos> spotsF1N2 = findWalkableSpots(level, 125, 155, 15, 45);
        Collections.shuffle(spotsF1N2, new java.util.Random(102));
        idx = 0;
        for (int i = 0; i < 4 && idx < spotsF1N2.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N2.get(idx++), "dungeon_room_f1_n2", z -> equipIronArmor(z));
        }
        for (int i = 0; i < 8 && idx < spotsF1N2.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N2.get(idx++), "dungeon_room_f1_n2", z -> {});
        }
        for (int i = 0; i < 3 && idx < spotsF1N2.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF1N2.get(idx++), "dungeon_room_f1_n2", s -> equipBow(s, net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, 1));
        }
        for (int i = 0; i < 4 && idx < spotsF1N2.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF1N2.get(idx++), "dungeon_room_f1_n2", s -> equipBow(s, null, 0));
        }

        // FASE 1 - Nivel 3 (X=[120..185], Z=[60..76]): 16 Zombies (6 armadura hierro), 2 Zombies (armadura diamante), 10 Esqueletos (4 arco encantado)
        List<BlockPos> spotsF1N3 = findWalkableSpots(level, 120, 185, 60, 76);
        Collections.shuffle(spotsF1N3, new java.util.Random(103));
        idx = 0;
        for (int i = 0; i < 2 && idx < spotsF1N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N3.get(idx++), "dungeon_room_f1_n3", z -> equipDiamondArmor(z));
        }
        for (int i = 0; i < 6 && idx < spotsF1N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N3.get(idx++), "dungeon_room_f1_n3", z -> equipIronArmor(z));
        }
        for (int i = 0; i < 10 && idx < spotsF1N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF1N3.get(idx++), "dungeon_room_f1_n3", z -> {});
        }
        for (int i = 0; i < 4 && idx < spotsF1N3.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF1N3.get(idx++), "dungeon_room_f1_n3", s -> equipBow(s, net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, 2));
        }
        for (int i = 0; i < 6 && idx < spotsF1N3.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF1N3.get(idx++), "dungeon_room_f1_n3", s -> equipBow(s, null, 0));
        }

        // FASE 2 - Nivel 1 (X=[55..80], Z=[15..55]): 12 Zombies (4 armadura hierro), 8 Esqueletos
        List<BlockPos> spotsF2N1 = findWalkableSpots(level, 55, 80, 15, 55);
        Collections.shuffle(spotsF2N1, new java.util.Random(201));
        idx = 0;
        for (int i = 0; i < 4 && idx < spotsF2N1.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N1.get(idx++), "dungeon_room_f2_n1", z -> equipIronArmor(z));
        }
        for (int i = 0; i < 8 && idx < spotsF2N1.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N1.get(idx++), "dungeon_room_f2_n1", z -> {});
        }
        for (int i = 0; i < 8 && idx < spotsF2N1.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF2N1.get(idx++), "dungeon_room_f2_n1", s -> equipBow(s, null, 0));
        }

        // FASE 2 - Nivel 2 (X=[10..55], Z=[15..55]): 16 Zombies (6 armadura hierro), 10 Esqueletos (5 arco con Empuje)
        List<BlockPos> spotsF2N2 = findWalkableSpots(level, 10, 55, 15, 55);
        Collections.shuffle(spotsF2N2, new java.util.Random(202));
        idx = 0;
        for (int i = 0; i < 6 && idx < spotsF2N2.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N2.get(idx++), "dungeon_room_f2_n2", z -> equipIronArmor(z));
        }
        for (int i = 0; i < 10 && idx < spotsF2N2.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N2.get(idx++), "dungeon_room_f2_n2", z -> {});
        }
        for (int i = 0; i < 5 && idx < spotsF2N2.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF2N2.get(idx++), "dungeon_room_f2_n2", s -> equipBow(s, net.minecraft.world.item.enchantment.Enchantments.PUNCH_ARROWS, 2));
        }
        for (int i = 0; i < 5 && idx < spotsF2N2.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF2N2.get(idx++), "dungeon_room_f2_n2", s -> equipBow(s, null, 0));
        }

        // FASE 2 - Nivel 3 (X=[15..60], Z=[60..76]): 20 Zombies (8 armadura hierro, 3 armadura diamante), 12 Esqueletos (6 arco encantado)
        List<BlockPos> spotsF2N3 = findWalkableSpots(level, 15, 60, 60, 76);
        Collections.shuffle(spotsF2N3, new java.util.Random(203));
        idx = 0;
        for (int i = 0; i < 3 && idx < spotsF2N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N3.get(idx++), "dungeon_room_f2_n3", z -> equipDiamondArmor(z));
        }
        for (int i = 0; i < 8 && idx < spotsF2N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N3.get(idx++), "dungeon_room_f2_n3", z -> equipIronArmor(z));
        }
        for (int i = 0; i < 9 && idx < spotsF2N3.size(); i++) {
            spawnMob(level, EntityType.ZOMBIE, spotsF2N3.get(idx++), "dungeon_room_f2_n3", z -> {});
        }
        for (int i = 0; i < 6 && idx < spotsF2N3.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF2N3.get(idx++), "dungeon_room_f2_n3", s -> equipBow(s, net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, 2));
        }
        for (int i = 0; i < 6 && idx < spotsF2N3.size(); i++) {
            spawnMob(level, EntityType.SKELETON, spotsF2N3.get(idx++), "dungeon_room_f2_n3", s -> equipBow(s, null, 0));
        }

        // Colocar las barreras invisibles bloqueando el paso a las siguientes salas
        placeDungeonBarriers(level);

        LogUtils.getLogger().info("Mobs de la dungeon generados exitosamente en Fase 1 y Fase 2.");
    }

    private static List<BlockPos> findWalkableSpots(ServerLevel level, int minX, int maxX, int minZ, int maxZ) {
        List<BlockPos> spots = new java.util.ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos pos = DUNGEON_ORIGIN.offset(x, 28, z);
                level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
                BlockState floor = level.getBlockState(pos.below());
                BlockState at = level.getBlockState(pos);
                BlockState above = level.getBlockState(pos.above());
                if (!floor.isAir() && !floor.is(Blocks.RED_CARPET) && at.isAir() && above.isAir()) {
                    spots.add(pos);
                }
            }
        }
        return spots;
    }

    private static <T extends net.minecraft.world.entity.Mob> void spawnMob(ServerLevel level, EntityType<T> type, BlockPos pos, String roomTag, java.util.function.Consumer<T> setup) {
        T mob = type.create(level);
        if (mob != null) {
            setup.accept(mob);
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            mob.addTag(roomTag);
            mob.addTag("dungeon_mob");
            level.addFreshEntity(mob);
        }
    }

    public static String getGateIdForRoomTag(String tag) {
        return switch (tag) {
            case "dungeon_room_f1_n1" -> GATE_F1_N1;
            case "dungeon_room_f1_n2" -> GATE_F1_N2;
            case "dungeon_room_f1_n3" -> GATE_F1_N3;
            case "dungeon_room_f2_n1" -> GATE_F2_N1;
            case "dungeon_room_f2_n2" -> GATE_F2_N2;
            case "dungeon_room_f2_n3" -> GATE_F2_N3;
            default -> null;
        };
    }

    public static List<BlockPos> getGatePositions(String gateId) {
        List<BlockPos> positions = new java.util.ArrayList<>();
        switch (gateId) {
            case GATE_F1_N1 -> {
                // Muro divisorio entre N1 y N2 en X=160
                for (int z = 15; z <= 55; z++) {
                    for (int y = 28; y <= 31; y++) {
                        positions.add(DUNGEON_ORIGIN.offset(160, y, z));
                    }
                }
            }
            case GATE_F1_N2 -> {
                // Puerta entre N2 y N3 en Z=60, X=152..155
                for (int x = 152; x <= 155; x++) {
                    for (int y = 28; y <= 31; y++) {
                        positions.add(DUNGEON_ORIGIN.offset(x, y, 60));
                    }
                }
            }
            case GATE_F1_N3 -> {
                // Pasaje hacia el pasillo central en X=130, Z=33..36
                for (int z = 33; z <= 36; z++) {
                    for (int y = 28; y <= 33; y++) {
                        positions.add(DUNGEON_ORIGIN.offset(130, y, z));
                    }
                }
                // Jaula protectora alrededor del Cofre 1 en (149, 30, 70)
                for (int x = 148; x <= 150; x++) {
                    for (int y = 30; y <= 32; y++) {
                        for (int z = 69; z <= 71; z++) {
                            if (x == 149 && y == 30 && z == 70) continue;
                            positions.add(DUNGEON_ORIGIN.offset(x, y, z));
                        }
                    }
                }
            }
            case GATE_F2_N1 -> {
                // Muro divisorio entre N1 y N2 en X=55
                for (int z = 15; z <= 55; z++) {
                    for (int y = 28; y <= 31; y++) {
                        positions.add(DUNGEON_ORIGIN.offset(55, y, z));
                    }
                }
            }
            case GATE_F2_N2 -> {
                // Puerta entre N2 y N3 en Z=60, X=33..34
                for (int x = 33; x <= 34; x++) {
                    for (int y = 28; y <= 31; y++) {
                        positions.add(DUNGEON_ORIGIN.offset(x, y, 60));
                    }
                }
            }
            case GATE_F2_N3 -> {
                // Jaula protectora alrededor del Cofre 2 en (40, 30, 71)
                for (int x = 39; x <= 41; x++) {
                    for (int y = 30; y <= 32; y++) {
                        for (int z = 70; z <= 72; z++) {
                            if (x == 40 && y == 30 && z == 71) continue;
                            positions.add(DUNGEON_ORIGIN.offset(x, y, z));
                        }
                    }
                }
            }
        }
        return positions;
    }

    public static void placeDungeonBarriers(ServerLevel level) {
        DungeonProtectionData data = DungeonProtectionData.get(level);
        String[] allGates = new String[]{GATE_F1_N1, GATE_F1_N2, GATE_F1_N3, GATE_F2_N1, GATE_F2_N2, GATE_F2_N3};
        for (String gateId : allGates) {
            if (!data.isGateCleared(gateId)) {
                for (BlockPos pos : getGatePositions(gateId)) {
                    level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.RED_CARPET)) {
                        level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    public static void clearGate(ServerLevel level, String gateId) {
        DungeonProtectionData data = DungeonProtectionData.get(level);
        if (data.isGateCleared(gateId)) {
            return;
        }
        data.setGateCleared(gateId, true);

        BlockPos soundCenter = null;
        for (BlockPos pos : getGatePositions(gateId)) {
            level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
            if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.05);
                if (soundCenter == null) {
                    soundCenter = pos;
                }
            }
        }

        if (soundCenter != null) {
            level.playSound(null, soundCenter, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.2F);
            level.playSound(null, soundCenter, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.8F, 1.0F);
        }

        Component announcement = getGateClearMessage(gateId);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(announcement);
            player.displayClientMessage(Component.literal("§a§l[Mazmorra]§r §f¡Barrera desbloqueada!"), true);
        }
        LogUtils.getLogger().info("Barrera de la dungeon despejada: {}", gateId);
    }

    private static Component getGateClearMessage(String gateId) {
        return switch (gateId) {
            case GATE_F1_N1 -> Component.literal("§a§l[Mazmorra]§r §a¡Fase 1 - Nivel 1 despejado! La barrera hacia la sala del Nivel 2 ha desaparecido.");
            case GATE_F1_N2 -> Component.literal("§a§l[Mazmorra]§r §a¡Fase 1 - Nivel 2 despejado! La barrera hacia la sala del Nivel 3 ha desaparecido.");
            case GATE_F1_N3 -> Component.literal("§6§l[Mazmorra]§r §6¡Fase 1 completada con éxito!§a El Cofre 1 y el pasillo central han sido desbloqueados.");
            case GATE_F2_N1 -> Component.literal("§a§l[Mazmorra]§r §a¡Fase 2 - Nivel 1 despejado! La barrera hacia la sala del Nivel 2 ha desaparecido.");
            case GATE_F2_N2 -> Component.literal("§a§l[Mazmorra]§r §a¡Fase 2 - Nivel 2 despejado! La barrera hacia la sala del Nivel 3 ha desaparecido.");
            case GATE_F2_N3 -> Component.literal("§6§l[Mazmorra]§r §6¡Fase 2 completada con éxito!§a El Cofre 2 ha sido desbloqueado. ¡Ya puedes fabricar la Tarjeta Roja!");
            default -> Component.literal("§a§l[Mazmorra]§r §a¡Zona despejada! La barrera ha desaparecido.");
        };
    }

    public static void checkDungeonRoomClear(ServerLevel level, net.minecraft.world.entity.LivingEntity dyingEntity) {
        for (String tag : dyingEntity.getTags()) {
            if (!tag.startsWith("dungeon_room_")) continue;

            String gateId = getGateIdForRoomTag(tag);
            if (gateId == null) continue;

            DungeonProtectionData data = DungeonProtectionData.get(level);
            if (data.isGateCleared(gateId)) continue;

            int remaining = 0;
            for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
                if (e != dyingEntity && e.isAlive() && !e.isRemoved() && e.getTags().contains(tag)) {
                    remaining++;
                }
            }

            if (remaining == 0) {
                clearGate(level, gateId);
            } else {
                int finalRemaining = remaining;
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(dyingEntity) < 40 * 40) {
                        player.displayClientMessage(
                                Component.literal("§e[Mazmorra] §7Enemigos restantes en la sala: §f" + finalRemaining),
                                true
                        );
                    }
                }
            }
        }
    }

    public static int getRemainingMobs(ServerLevel level, String roomTag) {
        int count = 0;
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e.isAlive() && !e.isRemoved() && e.getTags().contains(roomTag)) {
                count++;
            }
        }
        return count;
    }

    public static void resetDungeonBarriers(ServerLevel level) {
        DungeonProtectionData data = DungeonProtectionData.get(level);
        data.resetClearedGates();
        placeDungeonBarriers(level);
    }

    private static void equipIronArmor(Zombie zombie) {
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_LEGGINGS));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_BOOTS));
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.05F);
    }

    private static void equipDiamondArmor(Zombie zombie) {
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_LEGGINGS));
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BOOTS));
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.05F);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.05F);
    }

    private static void equipBow(Skeleton skeleton, @org.jetbrains.annotations.Nullable net.minecraft.world.item.enchantment.Enchantment enchantment, int encLevel) {
        net.minecraft.world.item.ItemStack bow = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW);
        if (enchantment != null && encLevel > 0) {
            bow.enchant(enchantment, encLevel);
        }
        skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, bow);
    }
}
