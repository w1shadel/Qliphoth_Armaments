package com.maxwell.qliphoth_armaments.common.network;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.api.capabilities.IElementalState;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncElementalState(int entityId, int elementOrdinal, int duration) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncElementalState> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "sync_elemental_state"));

    public static final StreamCodec<ByteBuf, PacketSyncElementalState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketSyncElementalState::entityId,
            ByteBufCodecs.VAR_INT, PacketSyncElementalState::elementOrdinal,
            ByteBufCodecs.VAR_INT, PacketSyncElementalState::duration,
            PacketSyncElementalState::new
    );

    public PacketSyncElementalState(int entityId, QAElements element, int duration) {
        this(entityId, (element == null) ? -1 : element.ordinal(), duration);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncElementalState msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    Entity entity = level.getEntity(msg.entityId);
                    if (entity instanceof LivingEntity livingEntity) {
                        IElementalState state = livingEntity.getData(CapabilityHandler.ELEMENTAL_STATE.get());
                        if (msg.elementOrdinal == -1) {
                            state.clearElement();
                        } else {
                            QAElements element = QAElements.values()[msg.elementOrdinal];
                            state.setElement(element, msg.duration, level.getGameTime());
                        }

                    }
                }
            } catch (Exception e) {
            }
        });
    }
}