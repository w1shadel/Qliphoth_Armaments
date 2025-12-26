package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(QA.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.messageBuilder(PacketSyncElementalState.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSyncElementalState::encode)
                .decoder(PacketSyncElementalState::decode)
                .consumerMainThread(PacketSyncElementalState::handle)
                .add();
    }
}