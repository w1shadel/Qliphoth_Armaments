package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.QA;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class CapabilityHandler {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, QA.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IElementalState>> ELEMENTAL_STATE =
            ATTACHMENT_TYPES.register("elemental_state",
                    () -> AttachmentType.<IElementalState>builder(ElementalState::new)
                            .serialize(new ElementalState.Serializer())
                            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}