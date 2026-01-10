package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EnvySin extends PlayerSin {
    private int startSlot = -1;

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        this.startSlot = player.getInventory().selected;
    }

    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (startSlot != -1 && player.getInventory().selected != startSlot) {
            if (player instanceof ServerPlayer sp) PlayerSinsHandler.sin(sp, 10);
        }
    }
}