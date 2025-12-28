package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.QA;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(QA.MOD_ID)
                .versioned("1");
        registrar.playToClient(
                PacketSyncElementalState.TYPE,
                PacketSyncElementalState.STREAM_CODEC,
                PacketSyncElementalState::handle
        );
    }
}