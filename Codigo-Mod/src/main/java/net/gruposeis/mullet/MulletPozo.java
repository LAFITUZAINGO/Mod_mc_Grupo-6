package net.gruposeis.mullet;

import com.mojang.logging.LogUtils;
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

        // Registra los métodos de inicialización
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Registra el mod en el bus de eventos de MinecraftForge
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Configuracion comun de Mullet Pozo cargada");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // En los proximos videos aqui agregaras items a las pestañas del modo creativo
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
