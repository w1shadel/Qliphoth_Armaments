package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncMobSin(int entityId, int sinCount) implements CustomPacketPayload {
    public static final Type<PacketSyncMobSin> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "sync_mob_sin"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncMobSin> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketSyncMobSin::entityId,
            ByteBufCodecs.VAR_INT, PacketSyncMobSin::sinCount,
            PacketSyncMobSin::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(entityId);
                if (entity instanceof LivingEntity living) {
                    living.setData(ModAttachments.SIN, sinCount);
                }
            }
        });
    }
}