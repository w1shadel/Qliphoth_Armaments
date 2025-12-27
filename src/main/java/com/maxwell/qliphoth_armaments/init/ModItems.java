package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.item.*;
import net.minecraft.network.chat.Component;
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
    public static final Supplier<Item> KNIGHT_SCRAP = ITEMS.register("knight_scrap", () -> new ComponentItem(new Item.Properties(), Component.translatable("item.qliphoth_armaments.knight_scrap.desc")));
    public static final Supplier<Item> FIRE_M_BATTLEAXE = ITEMS.register("fire_malkuth_warriors_battle_axe", () -> new SeismicImpactAxeItem(Tiers.DIAMOND, 2, -2.1f, new Item.Properties(), QAElements.FIRE));
    public static final Supplier<Item> ICE_M_BATTLEAXE = ITEMS.register("ice_malkuth_warriors_battle_axe", () -> new SeismicImpactAxeItem(Tiers.DIAMOND, 2, -2.2f, new Item.Properties(), QAElements.ICE));
    public static final Supplier<Item> SERAPHIM_RAILGUN = ITEMS.register("seraphim_railgun", () -> new LaserRifleItem(Tiers.DIAMOND, 1, -3.9f, new Item.Properties()));
}
