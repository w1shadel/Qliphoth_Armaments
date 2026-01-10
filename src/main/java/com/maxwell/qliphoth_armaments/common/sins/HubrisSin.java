package com.maxwell.qliphoth_armaments.common.sins;

import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class HubrisSin extends PlayerSin {
    @Override
    public void playerTick(Player player, ActivePlayerSinInstance instance) {
        if (player.level().isClientSide) return;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1, false, false));
        if (player.isCrouching()) {
            if (player.tickCount % 20 == 0) {
            }
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerSinsHandler.sin(serverPlayer, 20);
            }
        }
    }

    @Override
    public void onSinAdded(Player player, ActivePlayerSinInstance instance) {
    }
}