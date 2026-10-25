package net.gruposeis.mullet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MulletPozo.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class LibroInicio {

    @SubscribeEvent
    public static void alEntrarAlMundo(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer jugador)) {
            return;
        }

        // Comprobar si el jugador ya recibió el libro
        if (jugador.getPersistentData().getBoolean("mullet_libro_recibido")) {
            return;
        }

        // Crear el libro
        ItemStack libro = new ItemStack(Items.WRITTEN_BOOK);

        CompoundTag tag = libro.getOrCreateTag();

        // Nombre del libro
        tag.putString("title", "Mr Mullet");

        // Autor
        tag.putString("author", "Mr Mullet");

        // Páginas
        ListTag paginas = new ListTag();

        paginas.add(StringTag.valueOf(
                "{\"text\":\"Bienvenido a la aventura de Mr Mullet.\\n\\nTu misión es encontrar y rescatar al profesor MR_MULLET, quien quedó atrapado en una dimensión desconocida.\"}"
        ));

        paginas.add(StringTag.valueOf(
                "{\"text\":\"El profesor fue corrompido por un malware creado por Pintaluba. Para salvarlo tendrás que explorar el mundo y encontrar una estructura especial conocida como El Pozo.\"}"
        ));

        paginas.add(StringTag.valueOf(
                "{\"text\":\"Para acceder a la dimensión necesitarás una Llave Moneda. Una vez que encuentres El Pozo, tendrás que utilizarla para activar el acceso a las Tierras Profundas.\"}"
        ));

        paginas.add(StringTag.valueOf(
                "{\"text\":\"Dentro de las Tierras Profundas encontrarás diferentes desafíos. Supera las salas, consigue las Tarjetas Amarillas y fabrica la Tarjeta Roja para poder avanzar.\"}"
        ));

        paginas.add(StringTag.valueOf(
                "{\"text\":\"Tu objetivo final será llegar hasta MR_MULLET y liberarlo de la corrupción. Prepárate, explora y presta atención a las pistas que encuentres durante tu aventura.\"}"
        ));

        tag.put("pages", paginas);

        // Entregar el libro
        jugador.getInventory().add(libro);

        // Marcar que ya lo recibió
        jugador.getPersistentData().putBoolean(
                "mullet_libro_recibido",
                true
        );
    }
}