package net.gruposeis.mullet;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.gruposeis.mullet.block.Pozo;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MulletPozo.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ComandosMullet {

    @SubscribeEvent
    public static void registrarComandos(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("mullet")
                        .requires(source -> source.hasPermission(2))

                        // Comando para recuperar el libro
                        .then(Commands.literal("libro")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    ItemStack libro = crearLibro();
                                    boolean entregado = jugador.getInventory().add(libro);
                                    if (!entregado) {
                                        jugador.drop(libro, false);
                                    }
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Has recuperado el libro Mr Mullet."),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        // Comando para construir El Pozo (con profundidad de entierro configurable)
                        .then(Commands.literal("pozo")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    Pozo.construir(jugador.level(), jugador.blockPosition(), 6);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("El Pozo se ha construido (enterrado 6 bloques)."),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("entierro", IntegerArgumentType.integer(0, 15))
                                        .executes(context -> {
                                            int entierro = IntegerArgumentType.getInteger(context, "entierro");
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            Pozo.construir(jugador.level(), jugador.blockPosition(), entierro);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("El Pozo se ha construido (enterrado " + entierro + " bloques)."),
                                                    false
                                            );
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }

    // Crear el libro Mr Mullet
    private static ItemStack crearLibro() {
        ItemStack libro = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = libro.getOrCreateTag();

        tag.putString("title", "Mr Mullet");
        tag.putString("author", "Mr Mullet");

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
        return libro;
    }
}
