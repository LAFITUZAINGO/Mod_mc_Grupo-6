package net.gruposeis.mullet.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class PozoStructure extends Structure {
    public static final Codec<PozoStructure> CODEC = simpleCodec(PozoStructure::new);

    public PozoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        int hCenter = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int h1 = context.chunkGenerator().getBaseHeight(x - 6, z - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int h2 = context.chunkGenerator().getBaseHeight(x + 6, z - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int h3 = context.chunkGenerator().getBaseHeight(x - 6, z + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int h4 = context.chunkGenerator().getBaseHeight(x + 6, z + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int groundY = Math.min(hCenter, Math.min(Math.min(h1, h2), Math.min(h3, h4)));

        if (groundY <= context.chunkGenerator().getSeaLevel()) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(x, groundY, z);

        return Optional.of(new GenerationStub(origin, builder -> {
            builder.addPiece(new PozoPiece(origin));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.POZO_STRUCTURE.get();
    }
}

