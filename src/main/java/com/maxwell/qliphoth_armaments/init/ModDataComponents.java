package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, QA.MOD_ID);

    // boolean用のコンポーネント (TAG_RAMPAGE_MODE の代わり)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> RAMPAGE_MODE =
            DATA_COMPONENT_TYPES.register("rampage_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL) // NBTに保存する設定
                    .networkSynchronized(ByteBufCodecs.BOOL) // クライアントと同期する設定
                    .build());

    // int用のコンポーネント (TAG_RAMPAGE_DURATION の代わり)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RAMPAGE_DURATION =
            DATA_COMPONENT_TYPES.register("rampage_duration", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    // int用のコンポーネント (TAG_MODE の代わり)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MODE =
            DATA_COMPONENT_TYPES.register("mode", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());
}