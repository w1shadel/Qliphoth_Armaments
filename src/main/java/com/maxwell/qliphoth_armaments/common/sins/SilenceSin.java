package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SilenceSin extends PlayerSin {
    private static final int SPRINT_LIMIT = 40;

    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide) return;
        if (player.isSprinting()) {
            int sprintTicks = player.getData(ModAttachments.SPRINT_TIMER);
            sprintTicks++;
            if (sprintTicks >= SPRINT_LIMIT) {
                if (player instanceof ServerPlayer sp) {
                    PlayerSinsHandler.sin(sp, 10);
                }
                sprintTicks = 0;
            }
            player.setData(ModAttachments.SPRINT_TIMER, sprintTicks);
        } else {
            if (player.getData(ModAttachments.SPRINT_TIMER) > 0) {
                player.setData(ModAttachments.SPRINT_TIMER, 0);
            }
        }
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        // 罪が始まった時にタイマーを初期化しておく
        player.setData(ModAttachments.SPRINT_TIMER, 0);
    }
}