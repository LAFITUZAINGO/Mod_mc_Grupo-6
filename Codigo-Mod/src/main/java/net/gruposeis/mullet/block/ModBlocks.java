package net.gruposeis.mullet.block;

import net.gruposeis.mullet.MulletPozo;
import net.gruposeis.mullet.PozoBlock;
import net.gruposeis.mullet.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    // Registro diferido de Forge para todos los bloques de este mod
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MulletPozo.MOD_ID);

    // Registro del bloque PozoBlock con el id "mullet_block" (debe coincidir con mullet_block.json)
    public static final RegistryObject<Block> MULLET_BLOCK = registerBlock("mullet_block",
            () -> new PozoBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)));

    // Método auxiliar que registra el bloque y automáticamente crea su correspondiente BlockItem
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Registra el ítem del bloque en el registro de ítems para poder tenerlo en mano
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Método para conectar el registro con el bus de eventos de Forge
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
