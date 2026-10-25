package net.gruposeis.mullet.block;

import net.gruposeis.mullet.MulletPozo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class Pozo {

    public static void construir(Level mundo, BlockPos centro) {
        construir(mundo, centro, 6);
    }

    public static void construir(Level mundo, BlockPos centro, int profundidadEntierro) {
        if (!(mundo instanceof ServerLevel serverLevel)) {
            return;
        }

        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        Optional<StructureTemplate> templateOpt = templateManager.get(new ResourceLocation(MulletPozo.MOD_ID, "pozo"));

        if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false);

            // 1. Obtener la altura real del suelo sólido (OCEAN_FLOOR ignora copas de árboles y plantas)
            // Evaluamos el centro y los bordes del área de 17x17 para que nunca quede flotando en desniveles
            int hCenter = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, centro.getX(), centro.getZ());
            int h1 = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, centro.getX() - 6, centro.getZ() - 6);
            int h2 = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, centro.getX() + 6, centro.getZ() - 6);
            int h3 = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, centro.getX() - 6, centro.getZ() + 6);
            int h4 = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, centro.getX() + 6, centro.getZ() + 6);

            int groundY = Math.min(hCenter, Math.min(Math.min(h1, h2), Math.min(h3, h4)));

            // 2. Enterramos la base los bloques indicados (por defecto 6) para que toda la parte del foso
            // del pozo (capas Y=0..3) quede 100% sepultada bajo el terreno.
            BlockPos placePos = new BlockPos(centro.getX() - 8, groundY - profundidadEntierro, centro.getZ() - 8);
            template.placeInWorld(serverLevel, placePos, placePos, settings, serverLevel.random, 3);
        }
    }
}
