package com.maxwell.qliphoth_armaments.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class QAConfig {
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ModConfigSpec.BooleanValue conductorRequiemHotbarOnly;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("Conductor Requiem Settings");

            conductorRequiemHotbarOnly = builder
                    .comment("If true, minions will only spawn when the item is in the hotbar or offhand.")
                    .comment("trueの場合、アイテムがホットバーまたはオフハンドにある時のみミニオンを召喚します。")
                    .define("OnlySpawnInHotbar", true);

            builder.pop();
        }
    }
}