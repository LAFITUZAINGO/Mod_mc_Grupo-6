package net.gruposeis.mullet;

import com.mojang.logging.LogUtils;
import net.gruposeis.mullet.block.ModBlocks;
import net.gruposeis.mullet.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// Anotación principal que indica a Forge que esta clase es el punto de entrada del Mod
@Mod(MulletPozo.MOD_ID)
public class MulletPozo {
    // Identificador único del mod (debe coincidir con mod_id en gradle.properties)
    public static final String MOD_ID = "mullet";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MulletPozo(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 1. Registra los ítems y bloques en el bus de eventos de Forge
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // 2. Registra los métodos de inicialización
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // 3. Registra el mod en el bus general de eventos de MinecraftForge
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Configuracion comun de Mullet Pozo cargada");
    }

    // Método que coloca los ítems y bloques en las pestañas del inventario creativo
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Agrega los ítems a la pestaña de Ingredientes
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.MONEDA);
            event.accept(ModItems.TARJETA_ROJA);
            event.accept(ModItems.TARJETA_AMARILLA);
        }

        // Agrega el bloque a la pestaña de Bloques de Construcción
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.MULLET_BLOCK);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando con el mod Mullet Pozo activo");
    }

    // Eventos que solo se ejecutan del lado del cliente (graficos, pantallas, etc.)
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Configuracion de cliente para Mullet Pozo lista");
        }
    }
}
