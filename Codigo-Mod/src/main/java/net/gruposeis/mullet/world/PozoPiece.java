package net.gruposeis.mullet.world;

import net.gruposeis.mullet.MulletPozo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class PozoPiece extends StructurePiece {
    private final BlockPos origin;

    public PozoPiece(BlockPos origin) {
        super(ModStructures.POZO_PIECE.get(), 0, createBoundingBox(origin));
        this.origin = origin;
    }

    public PozoPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.POZO_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
    }

    private static BoundingBox createBoundingBox(BlockPos origin) {
        return new BoundingBox(
                origin.getX() - 8, origin.getY() - 6, origin.getZ() - 8,
                origin.getX() + 8, origin.getY() - 6 + 22, origin.getZ() + 8
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("ox", this.origin.getX());
        tag.putInt("oy", this.origin.getY());
        tag.putInt("oz", this.origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        StructureTemplateManager templateManager = level.getLevel().getStructureManager();
        Optional<StructureTemplate> templateOpt = templateManager.get(new ResourceLocation(MulletPozo.MOD_ID, "pozo"));

        if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false)
                    .setBoundingBox(box);

            BlockPos placePos = new BlockPos(this.origin.getX() - 8, this.origin.getY() - 6, this.origin.getZ() - 8);
            template.placeInWorld(level, placePos, placePos, settings, random, 2);
        }
    }
}

