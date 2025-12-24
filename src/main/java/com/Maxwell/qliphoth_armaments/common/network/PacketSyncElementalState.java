package com.Maxwell.qliphoth_armaments.common.network;

import com.Maxwell.qliphoth_armaments.api.QAElements;
import com.Maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.client.Minecraft;
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
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handlePacket(msg));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientHandler {
        public static void handlePacket(PacketSyncElementalState msg) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(msg.entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getCapability(CapabilityHandler.ELEMENTAL_STATE_CAPABILITY).ifPresent(state -> {
                        if (msg.elementOrdinal == -1) {
                            state.clearElement();
                        } else {
                            QAElements element = QAElements.values()[msg.elementOrdinal];
                            state.setElement(element, msg.duration, level.getGameTime());
                        }
                    });
                }
            }
        }
    }
}