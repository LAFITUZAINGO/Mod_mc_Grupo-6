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

                        // Comando para teletransportarse a la dungeon (sala de la cruz de amatista)
                        .then(Commands.literal("dungeon")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    WellPortalManager.teleportToDungeonDimension(jugador);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Teletransportado a la sala de la cruz de amatista en la dungeon."),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        // Comando para regresar al Overworld
                        .then(Commands.literal("overworld")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    WellPortalManager.teleportBackToOverworld(jugador);
                                    return Command.SINGLE_SUCCESS;
                                })

                        // Comando para spawnear o resetear al boss Mr Mullet en la sala circular
                        .then(Commands.literal("boss")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    net.minecraft.server.level.ServerLevel dungeonLevel = jugador.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                                    if (dungeonLevel != null) {
                                        DungeonProtectionData data = DungeonProtectionData.get(dungeonLevel);
                                        data.setMrMulletDefeated(false);
                                        data.setMrMulletSpawned(false);
                                        WellPortalManager.ensureMrMulletSpawned(dungeonLevel);
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aBoss Mr Mullet posicionado en la sala circular."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendFailure(Component.literal("§cNo se pudo acceder a la dimensión Tierras Profundas."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        // Comando para spawnear o resetear todos los mobs de la dungeon segun Dungeon.md
                        .then(Commands.literal("mobs")
                                .executes(context -> {
                                    ServerPlayer jugador = context.getSource().getPlayerOrException();
                                    net.minecraft.server.level.ServerLevel dungeonLevel = jugador.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                                    if (dungeonLevel != null) {
                                        WellPortalManager.spawnDungeonMobs(dungeonLevel);
                                        DungeonProtectionData.get(dungeonLevel).setDungeonMobsSpawned(true);
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aMobs de la dungeon generados exitosamente en Fase 1 y Fase 2."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendFailure(Component.literal("§cNo se pudo acceder a la dimension Tierras Profundas."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        // Comando para inspeccionar o restablecer las barreras invisibles de la mazmorra
                        .then(Commands.literal("barreras")
                                .then(Commands.literal("colocar")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            net.minecraft.server.level.ServerLevel dungeonLevel = jugador.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                                            if (dungeonLevel != null) {
                                                WellPortalManager.placeDungeonBarriers(dungeonLevel);
                                                context.getSource().sendSuccess(() -> Component.literal("§aBarreras colocadas en todas las salas no despejadas."), false);
                                            } else {
                                                context.getSource().sendFailure(Component.literal("§cNo se pudo acceder a la dimension Tierras Profundas."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            net.minecraft.server.level.ServerLevel dungeonLevel = jugador.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                                            if (dungeonLevel != null) {
                                                WellPortalManager.resetDungeonBarriers(dungeonLevel);
                                                context.getSource().sendSuccess(() -> Component.literal("§aTodas las barreras han sido reiniciadas y colocadas."), false);
                                            } else {
                                                context.getSource().sendFailure(Component.literal("§cNo se pudo acceder a la dimension Tierras Profundas."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("info")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            net.minecraft.server.level.ServerLevel dungeonLevel = jugador.server.getLevel(WellPortalManager.TIERRAS_PROFUNDAS);
                                            if (dungeonLevel != null) {
                                                DungeonProtectionData data = DungeonProtectionData.get(dungeonLevel);
                                                int f1n1 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f1_n1");
                                                int f1n2 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f1_n2");
                                                int f1n3 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f1_n3");
                                                int f2n1 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f2_n1");
                                                int f2n2 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f2_n2");
                                                int f2n3 = WellPortalManager.getRemainingMobs(dungeonLevel, "dungeon_room_f2_n3");

                                                jugador.sendSystemMessage(Component.literal("§6§l=== ESTADO DE SALAS Y BARRERAS ==="));
                                                jugador.sendSystemMessage(Component.literal("§eFase 1 - Nivel 1: §f" + f1n1 + " mobs §7| Puerta abierta: " + data.isGateCleared(WellPortalManager.GATE_F1_N1)));
                                                jugador.sendSystemMessage(Component.literal("§eFase 1 - Nivel 2: §f" + f1n2 + " mobs §7| Puerta abierta: " + data.isGateCleared(WellPortalManager.GATE_F1_N2)));
                                                jugador.sendSystemMessage(Component.literal("§eFase 1 - Nivel 3: §f" + f1n3 + " mobs §7| Cofre 1/Hub abierto: " + data.isGateCleared(WellPortalManager.GATE_F1_N3)));
                                                jugador.sendSystemMessage(Component.literal("§eFase 2 - Nivel 1: §f" + f2n1 + " mobs §7| Puerta abierta: " + data.isGateCleared(WellPortalManager.GATE_F2_N1)));
                                                jugador.sendSystemMessage(Component.literal("§eFase 2 - Nivel 2: §f" + f2n2 + " mobs §7| Puerta abierta: " + data.isGateCleared(WellPortalManager.GATE_F2_N2)));
                                                jugador.sendSystemMessage(Component.literal("§eFase 2 - Nivel 3: §f" + f2n3 + " mobs §7| Cofre 2 abierto: " + data.isGateCleared(WellPortalManager.GATE_F2_N3)));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        )

                        // Comando para reproducir y probar cada sonido del mod
                        .then(Commands.literal("sonido")
                                .then(Commands.literal("tarjeta")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.TARJETA_PONER.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§e[Sonido] Reproduciendo: tarjeta_poner"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("entrar")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.DIMENSION_ENTRAR.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§5[Sonido] Reproduciendo: dimension_entrar"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("salir")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.DIMENSION_SALIR.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§a[Sonido] Reproduciendo: dimension_salir"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("golpe")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.JEFE_GOLPE.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§c[Sonido] Reproduciendo: jefe_golpe"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("muerte")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.JEFE_MUERTE.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§4[Sonido] Reproduciendo: jefe_muerte"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("ataque")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.JEFE_ATAQUE.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§6[Sonido] Reproduciendo: jefe_ataque"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("ambiente")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.DUNGEON_AMBIENTE.get(), net.minecraft.sounds.SoundSource.RECORDS, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§9[Música] Reproduciendo: dungeon_ambiente"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("combate")
                                        .executes(context -> {
                                            ServerPlayer jugador = context.getSource().getPlayerOrException();
                                            jugador.level().playSound(null, jugador.blockPosition(), net.gruposeis.mullet.sound.ModSounds.DUNGEON_COMBATE.get(), net.minecraft.sounds.SoundSource.RECORDS, 1.0F, 1.0F);
                                            context.getSource().sendSuccess(() -> Component.literal("§d[Música] Reproduciendo: dungeon_combate"), false);
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
