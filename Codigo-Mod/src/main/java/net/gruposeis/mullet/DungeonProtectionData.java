package net.gruposeis.mullet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class DungeonProtectionData extends SavedData {
    private static final String DATA_NAME = "mullet_dungeon_protection";

    private boolean dungeonPlaced = false;
    private boolean mrMulletSpawned = false;
    private boolean mrMulletDefeated = false;
    private boolean dungeonMobsSpawned = false;
    private final Set<Long> playerPlacedBlocks = new HashSet<>();
    private final Set<String> clearedGates = new HashSet<>();

    public DungeonProtectionData() {}

    public static DungeonProtectionData load(CompoundTag tag) {
        DungeonProtectionData data = new DungeonProtectionData();
        data.dungeonPlaced = tag.getBoolean("DungeonPlaced");
        data.mrMulletSpawned = tag.getBoolean("MrMulletSpawned");
        data.mrMulletDefeated = tag.getBoolean("MrMulletDefeated");
        data.dungeonMobsSpawned = tag.getBoolean("DungeonMobsSpawned");
        long[] placed = tag.getLongArray("PlayerPlaced");
        for (long pos : placed) {
            data.playerPlacedBlocks.add(pos);
        }
        CompoundTag gatesTag = tag.getCompound("ClearedGates");
        for (String gateKey : gatesTag.getAllKeys()) {
            if (gatesTag.getBoolean(gateKey)) {
                data.clearedGates.add(gateKey);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("DungeonPlaced", this.dungeonPlaced);
        tag.putBoolean("MrMulletSpawned", this.mrMulletSpawned);
        tag.putBoolean("MrMulletDefeated", this.mrMulletDefeated);
        tag.putBoolean("DungeonMobsSpawned", this.dungeonMobsSpawned);
        long[] placed = new long[this.playerPlacedBlocks.size()];
        int i = 0;
        for (long pos : this.playerPlacedBlocks) {
            placed[i++] = pos;
        }
        tag.putLongArray("PlayerPlaced", placed);

        CompoundTag gatesTag = new CompoundTag();
        for (String gateKey : this.clearedGates) {
            gatesTag.putBoolean(gateKey, true);
        }
        tag.put("ClearedGates", gatesTag);
        return tag;
    }

    public static DungeonProtectionData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                DungeonProtectionData::load,
                DungeonProtectionData::new,
                DATA_NAME
        );
    }

    public boolean isDungeonPlaced() {
        return this.dungeonPlaced;
    }

    public void setDungeonPlaced(boolean placed) {
        this.dungeonPlaced = placed;
        this.setDirty();
    }

    public boolean isMrMulletSpawned() {
        return this.mrMulletSpawned;
    }

    public void setMrMulletSpawned(boolean spawned) {
        this.mrMulletSpawned = spawned;
        this.setDirty();
    }

    public boolean isMrMulletDefeated() {
        return this.mrMulletDefeated;
    }

    public void setMrMulletDefeated(boolean defeated) {
        this.mrMulletDefeated = defeated;
        this.setDirty();
    }

    public boolean isDungeonMobsSpawned() {
        return this.dungeonMobsSpawned;
    }

    public void setDungeonMobsSpawned(boolean spawned) {
        this.dungeonMobsSpawned = spawned;
        this.setDirty();
    }

    public boolean isPlayerPlaced(BlockPos pos) {
        return this.playerPlacedBlocks.contains(pos.asLong());
    }

    public void markPlayerPlaced(BlockPos pos) {
        if (this.playerPlacedBlocks.add(pos.asLong())) {
            this.setDirty();
        }
    }

    public void removePlayerPlaced(BlockPos pos) {
        if (this.playerPlacedBlocks.remove(pos.asLong())) {
            this.setDirty();
        }
    }

    public boolean isGateCleared(String gateId) {
        return this.clearedGates.contains(gateId);
    }

    public void setGateCleared(String gateId, boolean cleared) {
        boolean changed = cleared ? this.clearedGates.add(gateId) : this.clearedGates.remove(gateId);
        if (changed) {
            this.setDirty();
        }
    }

    public void resetClearedGates() {
        if (!this.clearedGates.isEmpty()) {
            this.clearedGates.clear();
            this.setDirty();
        }
    }

    public Set<String> getClearedGates() {
        return java.util.Collections.unmodifiableSet(this.clearedGates);
    }
}
