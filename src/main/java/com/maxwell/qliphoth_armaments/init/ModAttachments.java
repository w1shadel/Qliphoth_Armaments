package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.capabilities.ElementalState;
import com.maxwell.qliphoth_armaments.api.capabilities.IElementalState;
import com.maxwell.qliphoth_armaments.common.util.ModDataControl;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, QA.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IElementalState>> ELEMENTAL_STATE =
            ATTACHMENT_TYPES.register("elemental_state",
                    () -> AttachmentType.<IElementalState>builder(ElementalState::new)
                            .serialize(new ElementalState.Serializer())
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModDataControl>> MINION_CONTROL =
            ATTACHMENT_TYPES.register("minion_control",
                    () -> AttachmentType.builder(ModDataControl::new)
                            .build());

    // 修正: networkSynchronizedを追加してクライアントに同期させる
    public static final Supplier<AttachmentType<Integer>> SIN = ATTACHMENT_TYPES.register("sin",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    // ★追加: 罪のクールダウン（サーバー側でのみ使用するため同期不要）
    public static final Supplier<AttachmentType<Integer>> SIN_COOLDOWN = ATTACHMENT_TYPES.register("sin_cooldown",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    // その他の既存アタッチメント
    public static final Supplier<AttachmentType<Integer>> SIN_MODE = ATTACHMENT_TYPES.register("sin_mode",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build());

    public static final Supplier<AttachmentType<Integer>> SIN_TIMER = ATTACHMENT_TYPES.register("sin_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}