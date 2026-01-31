package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class RestlessSin extends PlayerSin {
    private static final int STILL_LIMIT = 30;

    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide)
            return;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false));
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
        player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
            data.setStillTimer(0);
        });
    }
}