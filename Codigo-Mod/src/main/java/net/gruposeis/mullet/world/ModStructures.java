package net.gruposeis.mullet.world;

import net.gruposeis.mullet.MulletPozo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MulletPozo.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MulletPozo.MOD_ID);

    public static final RegistryObject<StructureType<PozoStructure>> POZO_STRUCTURE =
            STRUCTURE_TYPES.register("pozo", () -> () -> PozoStructure.CODEC);

    public static final RegistryObject<StructurePieceType> POZO_PIECE =
            STRUCTURE_PIECES.register("pozo_piece", () -> PozoPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }
}

