package com.Maxwell.qliphoth_armaments.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class QAConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue conductorRequiemHotbarOnly;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Conductor Requiem Settings");
            conductorRequiemHotbarOnly = builder
                    .comment("If true, minions will only spawn when the item is in the hotbar or offhand.")
                    .comment("trueの場合、アイテムがホットバーまたはオフハンドにある時のみミニオンを召喚します。")
                    .define("OnlySpawnInHotbar", true);
            builder.pop();
        }
    }
}