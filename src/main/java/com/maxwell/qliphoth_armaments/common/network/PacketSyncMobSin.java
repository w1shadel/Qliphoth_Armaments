package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncMobSin {
    private final int entityId;
    private final int sinCount;

    public PacketSyncMobSin(int entityId, int sinCount) {
        this.entityId = entityId;
        this.sinCount = sinCount;
    }

    public PacketSyncMobSin(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.sinCount = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeVarInt(this.sinCount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.maxwell.qliphoth_armaments.client.network.ClientPacketHandlers
                            .handleSyncMobSin(entityId, sinCount));
        });
        context.setPacketHandled(true);
        return true;
    }

}