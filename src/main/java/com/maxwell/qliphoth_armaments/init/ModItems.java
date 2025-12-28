package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.item.ConductorRequiemItem;
import com.maxwell.qliphoth_armaments.common.item.MaximumQuietusItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QA.MOD_ID);

    public static final DeferredItem<Item> CONDUCTORS_REQUIEM = ITEMS.register("conductors_requiem",
            () -> new ConductorRequiemItem(Tiers.DIAMOND, 2, -2.3f, new Item.Properties()));

    public static final DeferredItem<Item> MAXIMUM_QUIETUS = ITEMS.register("maximum_quietus",
            () -> new MaximumQuietusItem(Tiers.DIAMOND, 7, -3.1f, new Item.Properties()));

    public static final DeferredItem<Item> TEMPERED_NETHERITE = ITEMS.register("tempered_netherite",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> HEATED_NETHERITE = ITEMS.register("heated_netherite",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COPPER_STICK = ITEMS.register("copper_stick",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
