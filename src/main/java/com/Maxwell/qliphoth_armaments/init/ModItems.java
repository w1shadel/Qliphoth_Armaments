package com.Maxwell.qliphoth_armaments.init;

import com.Maxwell.qliphoth_armaments.QA;
import com.Maxwell.qliphoth_armaments.common.item.ChesedOrchestratorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, QA.MOD_ID);
    public static final Supplier<Item> CONDUCTORS_REQUIEM = ITEMS.register("conductors_requiem", () -> new ChesedOrchestratorItem(Tiers.DIAMOND, 2, 3, new Item.Properties()));
}
