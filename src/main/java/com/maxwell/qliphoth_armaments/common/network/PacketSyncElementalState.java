package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncElementalState {

    private final int entityId;
    private final int elementOrdinal;
    private final int duration;

    public PacketSyncElementalState(int entityId, QAElements element, int duration) {
        this.entityId = entityId;
        this.elementOrdinal = (element == null) ? -1 : element.ordinal();
        this.duration = duration;
    }

    public PacketSyncElementalState(int entityId, int elementOrdinal, int duration) {
        this.entityId = entityId;
        this.elementOrdinal = elementOrdinal;
        this.duration = duration;
    }

    public static void encode(PacketSyncElementalState msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.elementOrdinal);
        buf.writeInt(msg.duration);
    }

    public static PacketSyncElementalState decode(FriendlyByteBuf buf) {
        return new PacketSyncElementalState(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketSyncElementalState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.maxwell.qliphoth_armaments.client.network.ClientPacketHandlers
                            .handleSyncElementalState(msg.entityId, msg.elementOrdinal, msg.duration));
        });
        ctx.get().setPacketHandled(true);
    }
}