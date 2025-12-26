package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.item.ConductorRequiemItem;
import com.maxwell.qliphoth_armaments.common.item.MaximumQuietusItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, QA.MOD_ID);
    public static final Supplier<Item> CONDUCTORS_REQUIEM = ITEMS.register("conductors_requiem", () -> new ConductorRequiemItem(Tiers.DIAMOND, 2, -2.3f, new Item.Properties()));
    public static final Supplier<Item> MAXIMUM_QUIETUS = ITEMS.register("maximum_quietus", () -> new MaximumQuietusItem(Tiers.DIAMOND, 7, -3.1f, new Item.Properties()));
    public static final Supplier<Item> TEMPERED_NETHERITE = ITEMS.register("tempered_netherite", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> HEATED_NETHERITE = ITEMS.register("heated_netherite", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> COPPER_STICK = ITEMS.register("copper_stick", () -> new Item(new Item.Properties()));
}
