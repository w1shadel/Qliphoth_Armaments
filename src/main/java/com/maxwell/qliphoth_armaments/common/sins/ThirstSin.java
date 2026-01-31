package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.world.entity.player.Player;

public class ThirstSin extends PlayerSin {
    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
            data.setLastDamageTime(player.level().getGameTime());
        });
    }
}