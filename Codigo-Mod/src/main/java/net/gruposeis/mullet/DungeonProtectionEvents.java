package net.gruposeis.mullet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MulletPozo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonProtectionEvents {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            if (event.getEntity() instanceof Player) {
                DungeonProtectionData data = DungeonProtectionData.get(level);
                if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
                    for (var snapshot : multi.getReplacedBlockSnapshots()) {
                        data.markPlayerPlaced(snapshot.getPos());
                    }
                } else {
                    data.markPlayerPlaced(event.getPos());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            Player player = event.getPlayer();
            // Permitir en modo creativo para pruebas o administradores
            if (player != null && player.isCreative()) {
                return;
            }

            BlockPos pos = event.getPos();

            // Mensaje especial si intenta romper el bloque de retorno al Overworld
            BlockPos returnBlockPos = WellPortalManager.DUNGEON_ORIGIN.offset(WellPortalManager.BLACK_ROOM_RETURN_OFFSET);
            if (pos.equals(returnBlockPos)) {
                event.setCanceled(true);
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§6§l[Pozo Mullet]§r §eEste bloque es el portal de retorno al Overworld. Haz clic derecho para volver."),
                            true
                    );
                }
                return;
            }

            DungeonProtectionData data = DungeonProtectionData.get(level);

            if (!data.isPlayerPlaced(pos)) {
                event.setCanceled(true);
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[Mazmorra] No puedes romper bloques de la dungeon. Solo bloques que hayas puesto tú."),
                            true
                    );
                }
            } else {
                data.removePlayerPlaced(pos);
            }
        }
    }

    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            Player player = event.getPlayer();
            if (player != null && player.isCreative()) {
                return;
            }

            DungeonProtectionData data = DungeonProtectionData.get(level);
            if (!data.isPlayerPlaced(event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            Player player = event.getEntity();
            if (player != null && !player.isCreative()) {
                BlockPos returnBlockPos = WellPortalManager.DUNGEON_ORIGIN.offset(WellPortalManager.BLACK_ROOM_RETURN_OFFSET);
                if (event.getPos().equals(returnBlockPos)) {
                    player.displayClientMessage(
                            Component.literal("§6§l[Pozo Mullet]§r §eHaz clic derecho en este bloque para regresar al Overworld."),
                            true
                    );
                    return;
                }

                DungeonProtectionData data = DungeonProtectionData.get(level);
                if (!data.isPlayerPlaced(event.getPos())) {
                    player.displayClientMessage(
                            Component.literal("§c[Mazmorra] No puedes romper bloques de la dungeon. Solo bloques que hayas puesto tú."),
                            true
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            DungeonProtectionData data = DungeonProtectionData.get(level);
            // Evita que explosiones (creepers, TNT) rompan bloques originales de la dungeon
            event.getAffectedBlocks().removeIf(pos -> !data.isPlayerPlaced(pos));
        }
    }

    @SubscribeEvent
    public static void onPistonPre(PistonEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == WellPortalManager.TIERRAS_PROFUNDAS) {
            DungeonProtectionData data = DungeonProtectionData.get(level);
            PistonStructureResolver resolver = event.getStructureHelper();
            if (resolver != null && resolver.resolve()) {
                for (BlockPos toDestroy : resolver.getToDestroy()) {
                    if (!data.isPlayerPlaced(toDestroy)) {
                        event.setCanceled(true);
                        return;
                    }
                }
                for (BlockPos toPush : resolver.getToPush()) {
                    if (!data.isPlayerPlaced(toPush)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
}
