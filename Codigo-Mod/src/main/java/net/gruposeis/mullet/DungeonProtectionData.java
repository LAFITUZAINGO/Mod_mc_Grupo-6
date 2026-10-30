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
    private final Set<Long> playerPlacedBlocks = new HashSet<>();

    public DungeonProtectionData() {}

    public static DungeonProtectionData load(CompoundTag tag) {
        DungeonProtectionData data = new DungeonProtectionData();
        data.dungeonPlaced = tag.getBoolean("DungeonPlaced");
        long[] placed = tag.getLongArray("PlayerPlaced");
        for (long pos : placed) {
            data.playerPlacedBlocks.add(pos);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("DungeonPlaced", this.dungeonPlaced);
        long[] placed = new long[this.playerPlacedBlocks.size()];
        int i = 0;
        for (long pos : this.playerPlacedBlocks) {
            placed[i++] = pos;
        }
        tag.putLongArray("PlayerPlaced", placed);
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
}

