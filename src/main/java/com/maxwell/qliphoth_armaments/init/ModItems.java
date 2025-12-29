package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // ForgeRegistries.ITEMS の代わりに DeferredRegister.createItems を使用します
    // これにより、登録メソッドが DeferredItem<T> を返すようになります
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QA.MOD_ID);
    // Supplier<Item> ではなく DeferredItem<クラス名> を使うと、後でコード内で特定のメソッドにアクセスしやすくなります
    // (DeferredItem は Supplier を継承しているので、そのまま get() できます)

    public static final DeferredItem<ConductorRequiemItem> CONDUCTORS_REQUIEM =
            ITEMS.register("conductors_requiem", () -> new ConductorRequiemItem(Tiers.DIAMOND, 2, -2.3f, new Item.Properties()));

    public static final DeferredItem<MaximumQuietusItem> MAXIMUM_QUIETUS =
            ITEMS.register("maximum_quietus", () -> new MaximumQuietusItem(Tiers.DIAMOND, 7, -3.1f, new Item.Properties()));

    public static final DeferredItem<Item> TEMPERED_NETHERITE =
            ITEMS.register("tempered_netherite", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> HEATED_NETHERITE =
            ITEMS.register("heated_netherite", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COPPER_STICK =
            ITEMS.register("copper_stick", () -> new Item(new Item.Properties()));

    public static final DeferredItem<ComponentItem> KNIGHT_SCRAP =
            ITEMS.register("knight_scrap", () -> new ComponentItem(new Item.Properties(), Component.translatable("item.qliphoth_armaments.knight_scrap.desc")));

    public static final DeferredItem<SeismicImpactAxeItem> FIRE_M_BATTLEAXE =
            ITEMS.register("fire_malkuth_warriors_battle_axe", () -> new SeismicImpactAxeItem(Tiers.DIAMOND, 2, -2.1f, new Item.Properties(), QAElements.FIRE));

    public static final DeferredItem<SeismicImpactAxeItem> ICE_M_BATTLEAXE =
            ITEMS.register("ice_malkuth_warriors_battle_axe", () -> new SeismicImpactAxeItem(Tiers.DIAMOND, 2, -2.2f, new Item.Properties(), QAElements.ICE));

    public static final DeferredItem<SeraphimRailGunItem> SERAPHIM_RAILGUN =
            ITEMS.register("seraphim_railgun", () -> new SeraphimRailGunItem(Tiers.DIAMOND, 1, -3.1f, new Item.Properties()));

    public static final DeferredItem<TheSovereigntyItem> THE_SOVEREIGNTY =
            ITEMS.register("the_sovereignty", () -> new TheSovereigntyItem(Tiers.NETHERITE, 10, -2.8f, new Item.Properties()));

}