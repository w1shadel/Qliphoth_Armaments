package com.maxwell.qliphoth_armaments.client.network;

import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientPacketHandlers {

    public static void handleSyncMobSin(int entityId, int sinCount) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity living) {
                living.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(cap -> {
                    cap.setSin(sinCount);
                });
            }
        }
    }

    public static void handleSyncElementalState(int entityId, int elementOrdinal, int duration) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.getCapability(CapabilityHandler.ELEMENTAL_STATE_CAPABILITY).ifPresent(state -> {
                    if (elementOrdinal == -1) {
                        state.clearElement();
                    } else {
                        com.maxwell.qliphoth_armaments.api.QAElements element = com.maxwell.qliphoth_armaments.api.QAElements
                                .values()[elementOrdinal];
                        state.setElement(element, duration, level.getGameTime());
                    }
                });
            }
        }
    }
}
