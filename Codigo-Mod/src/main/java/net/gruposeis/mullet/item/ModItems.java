package net.gruposeis.mullet.item;

import net.gruposeis.mullet.MulletPozo;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // Registro diferido de Forge para todos los ítems de este mod
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MulletPozo.MOD_ID);

    // Registro de los ítems individuales
    public static final RegistryObject<Item> MONEDA = ITEMS.register("moneda",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TARJETA_ROJA = ITEMS.register("tarjeta_roja",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TARJETA_AMARILLA = ITEMS.register("tarjeta_amarilla",
            () -> new Item(new Item.Properties()));

    // Método para conectar el registro con el bus de eventos de Forge
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
