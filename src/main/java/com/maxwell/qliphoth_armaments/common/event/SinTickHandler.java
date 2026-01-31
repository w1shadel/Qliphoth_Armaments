package com.maxwell.qliphoth_armaments.common.event;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.init.ModSins;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SinTickHandler {
    private static final int SIN_COOLDOWN = 60;
    private static final int TIMER_THRESHOLD = 30;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PlayerSins sins = PlayerSins.getPlayerSins(player);
        if (sins == null)
            return;
        player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
            if (sins.hasSinActive(ModSins.SILENCE.get())) {
                boolean isSprinting = player.isSprinting();
                int sprintTimer = data.getSprintTimer();
                if (isSprinting) {
                    sprintTimer++;
                    if (sprintTimer >= TIMER_THRESHOLD) {
                        PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                        sprintTimer = 0;
                    }
                } else {
                    sprintTimer = 0;
                }
                data.setSprintTimer(sprintTimer);
            }
            if (sins.hasSinActive(ModSins.RESTLESS.get())) {
                double speed = player.getDeltaMovement().horizontalDistanceSqr();
                if (speed < 0.0001) {
                    int stillTimer = data.getStillTimer();
                    stillTimer++;
                    if (stillTimer >= TIMER_THRESHOLD) {
                        PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                        stillTimer = 0;
                    }
                    data.setStillTimer(stillTimer);
                } else {
                    data.setStillTimer(0);
                }
            }
            if (sins.hasSinActive(ModSins.HUBRIS.get())) {
                if (player.isCrouching()) {
                    PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                }
            }
            if (sins.hasSinActive(ModSins.VORACITY.get())) {
                if (player.tickCount % 40 == 0) {
                    player.getFoodData().addExhaustion(4.0F);
                }
                if (player.getFoodData().getFoodLevel() <= 0) {
                    PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                }
            }
            if (sins.hasSinActive(ModSins.THIRST.get())) {
                long lastTime = data.getLastDamageTime();
                long now = player.level().getGameTime();
                if (now - lastTime > 100) {
                    PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                }
            }
            if (sins.hasSinActive(ModSins.ENVY.get())) {
                int currentSlot = player.getInventory().selected;
                int lastSlot = data.getLastSelectedSlot();
                if (lastSlot != -1 && currentSlot != lastSlot) {
                    PlayerSinsHandler.sin(player, 10);
                }
                data.setLastSelectedSlot(currentSlot);
            }
            if (sins.hasSinActive(ModSins.PRIDE.get())) {
                if (player.hurtTime <= 0 && !player.onGround() && !player.isInWater() && !player.isFallFlying()) {
                    PlayerSinsHandler.sin(player, SIN_COOLDOWN);
                }
            }
        });
    }
}
