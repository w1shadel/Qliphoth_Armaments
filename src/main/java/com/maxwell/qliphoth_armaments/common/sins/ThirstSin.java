package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ThirstSin extends PlayerSin {
    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide) return;
        long lastTime = player.getData(ModAttachments.LAST_DAMAGE_TIME);
        long now = player.level().getGameTime();
        if (now - lastTime > 100) {
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerSinsHandler.sin(serverPlayer, 20);
            }
        }
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        player.setData(ModAttachments.LAST_DAMAGE_TIME, player.level().getGameTime());
    }
}