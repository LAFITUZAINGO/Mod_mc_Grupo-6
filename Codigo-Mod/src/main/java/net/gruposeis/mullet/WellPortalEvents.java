package net.gruposeis.mullet;

import net.gruposeis.mullet.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MulletPozo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WellPortalEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity item) {
            if (item.getItem().is(ModItems.MONEDA.get())) {
                WellPortalManager.trackCoin(item);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide() && event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            WellPortalManager.tickCoins(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // El portal reutiliza los bloques de End Portal por su textura y animación cósmica,
            // pero cancelamos el viaje al End vainilla y enviamos al jugador a la dimensión Tierras Profundas.
            if (event.getDimension() == Level.END) {
                Double portalY = WellPortalManager.getWellPortalY(player.level(), player.getBoundingBox().inflate(0.2));
                if (portalY != null) {
                    event.setCanceled(true);
                    WellPortalManager.teleportToDungeonDimension(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() == WellPortalManager.TIERRAS_PROFUNDAS && event.getEntity() instanceof ServerPlayer player) {
            ServerLevel dungeonWorld = player.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
            if (dungeonWorld != null) {
                WellPortalManager.ensureDungeonPlaced(dungeonWorld);
            }
        }
    }
}
