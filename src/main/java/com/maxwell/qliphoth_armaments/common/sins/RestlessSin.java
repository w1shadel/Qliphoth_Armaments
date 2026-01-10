package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class RestlessSin extends PlayerSin {
    private static final int STILL_LIMIT = 30;

    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide) return;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false));
        double speed = player.getDeltaMovement().horizontalDistanceSqr();
        if (speed < 0.0001) {
            int stillTicks = player.getData(ModAttachments.STILL_TIMER);
            stillTicks++;
            if (stillTicks >= STILL_LIMIT) {
                if (player instanceof ServerPlayer serverPlayer) {
                    PlayerSinsHandler.sin(serverPlayer, 20);
                }
                stillTicks = 0;
            }
            player.setData(ModAttachments.STILL_TIMER, stillTicks);
        } else {
            if (player.getData(ModAttachments.STILL_TIMER) > 0) {
                player.setData(ModAttachments.STILL_TIMER, 0);
            }
        }
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        player.setData(ModAttachments.STILL_TIMER, 0);
    }
}