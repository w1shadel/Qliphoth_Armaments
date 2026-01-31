package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.entities.geburah.sins.GeburahTriggerSinEffectPacket;
import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdlib.network.FDPacketHandler;
import com.maxwell.qliphoth_armaments.common.event.ArmorEventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSinsHandler.class)
public class PlayerSinsHandlerMixin {
    @Inject(method = "sin(Lnet/minecraft/server/level/ServerPlayer;IFI)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onSin(ServerPlayer player, int cooldown, float soundPitch, int amount, CallbackInfo ci) {
        ci.cancel();
        PlayerSins playerSins = PlayerSins.getPlayerSins(player);
        if (playerSins == null || !BossUtil.isPlayerInSurvival(player) || !player.isAlive()) return;
        if (playerSins.getSinGainCooldown() > 0) return;
        FDPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GeburahTriggerSinEffectPacket(soundPitch));
        int current = playerSins.getSinnedTimes();
        int nextCount = current + amount;
        int maxAllowed = ArmorEventHandler.hasFullArmor(player) ? 12 : 6;
        if (nextCount >= maxAllowed) {
            player.hurt(com.finderfeed.fdbosses.init.BossDamageSources.GEBURAH_SINNED_TOO_MUCH_SOURCE, Float.MAX_VALUE);
        } else {
            playerSins.setSinnedTimes(nextCount);
            playerSins.setSinGainCooldown(cooldown);
            PlayerSins.setPlayerSins(player, playerSins);
        }
    }
}