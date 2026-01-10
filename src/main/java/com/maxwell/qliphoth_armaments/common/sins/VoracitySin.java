package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class VoracitySin extends PlayerSin {
    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide) return;
        if (player.tickCount % 40 == 0) {
            player.getFoodData().addExhaustion(4.0F);
        }
        if (player.getFoodData().getFoodLevel() <= 0) {
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerSinsHandler.sin(serverPlayer, 20);
            }
        }
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
    }
}