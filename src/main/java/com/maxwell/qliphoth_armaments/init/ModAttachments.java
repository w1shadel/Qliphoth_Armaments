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
    public static final Supplier<AttachmentType<Integer>> STILL_TIMER = ATTACHMENT_TYPES.register("still_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> SPRINT_TIMER = ATTACHMENT_TYPES.register("sprint_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModDataControl>> MINION_CONTROL =
            ATTACHMENT_TYPES.register("minion_control",
                    () -> AttachmentType.builder(ModDataControl::new)
                            .build());
    public static final Supplier<AttachmentType<Long>> LAST_COMBAT_TIME = ATTACHMENT_TYPES.register("last_combat_time",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());

    public static final Supplier<AttachmentType<Integer>> SIN_REDUCTION_TIMER = ATTACHMENT_TYPES.register("sin_reduction_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> SIN = ATTACHMENT_TYPES.register("sin",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    public static final Supplier<AttachmentType<Long>> LAST_DAMAGE_TIME = ATTACHMENT_TYPES.register("last_damage_time",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());
    public static final Supplier<AttachmentType<Long>> LAST_EAT_TIME = ATTACHMENT_TYPES.register("last_eat_time",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> SIN_TIMER = ATTACHMENT_TYPES.register("sin_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}