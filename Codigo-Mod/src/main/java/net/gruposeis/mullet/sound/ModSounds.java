package net.gruposeis.mullet.sound;

import net.gruposeis.mullet.MulletPozo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MulletPozo.MOD_ID);

    // 1. Sonido al insertar o interactuar con las tarjetas en un bloque o cerradura
    public static final RegistryObject<SoundEvent> TARJETA_PONER = registerSoundEvent("tarjeta_poner");

    // 2. Sonido al entrar a la dimensión de la dungeon (Tierras Profundas)
    public static final RegistryObject<SoundEvent> DIMENSION_ENTRAR = registerSoundEvent("dimension_entrar");

    // 3. Sonido al salir de la dimensión y regresar al Overworld
    public static final RegistryObject<SoundEvent> DIMENSION_SALIR = registerSoundEvent("dimension_salir");

    // 4. Sonido cuando el jefe Mr. Mullet recibe daño / es golpeado
    public static final RegistryObject<SoundEvent> JEFE_GOLPE = registerSoundEvent("jefe_golpe");

    // 5. Sonido cuando el jefe Mr. Mullet es derrotado / muere
    public static final RegistryObject<SoundEvent> JEFE_MUERTE = registerSoundEvent("jefe_muerte");

    // 6. Sonido cuando el jefe Mr. Mullet realiza un ataque o gruñido de combate
    public static final RegistryObject<SoundEvent> JEFE_ATAQUE = registerSoundEvent("jefe_ataque");

    // 7. Música ambiental de la dungeon
    public static final RegistryObject<SoundEvent> DUNGEON_AMBIENTE = registerSoundEvent("dungeon_ambiente");

    // 8. Música de combate intenso contra el jefe de la dungeon
    public static final RegistryObject<SoundEvent> DUNGEON_COMBATE = registerSoundEvent("dungeon_combate");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MulletPozo.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

