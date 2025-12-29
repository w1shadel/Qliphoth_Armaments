package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.util.MinionControlData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class CapabilityHandler {

    // 1.21.1では NeoForgeRegistries.ATTACHMENT_TYPES を直接指定します
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, QA.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IElementalState>> ELEMENTAL_STATE =
            ATTACHMENT_TYPES.register("elemental_state",
                    () -> AttachmentType.<IElementalState>builder(ElementalState::new)
                            .serialize(new ElementalState.Serializer())
                            .build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MinionControlData>> MINION_CONTROL =
            ATTACHMENT_TYPES.register("minion_control",
                    () -> AttachmentType.builder(MinionControlData::new)
                            // もしリコイルのタイマー等を死んだ後も維持したいなら .copyOnDeath() を追加
                            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}