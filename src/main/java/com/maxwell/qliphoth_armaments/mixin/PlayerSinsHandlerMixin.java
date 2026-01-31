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
        // 元の処理（上限6固定）を停止
        ci.cancel();
        PlayerSins playerSins = PlayerSins.getPlayerSins(player);
        if (playerSins == null || !BossUtil.isPlayerInSurvival(player) || !player.isAlive()) return;
        if (playerSins.getSinGainCooldown() > 0) return;
        // 音などのパケット送信（ライブラリの標準処理）
        FDPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GeburahTriggerSinEffectPacket(soundPitch));
        int current = playerSins.getSinnedTimes();
        int nextCount = current + amount;
        // 動的な上限設定：フルセットなら12、それ以外は6
        int maxAllowed = ArmorEventHandler.hasFullArmor(player) ? 12 : 6;
        if (nextCount >= maxAllowed) {
            // 上限に達した場合、即死ダメージを与える。
            // ※コアがある場合のキャンセル処理は、ArmorEventHandlerのLivingDamageEventで行う。
            player.hurt(com.finderfeed.fdbosses.init.BossDamageSources.GEBURAH_SINNED_TOO_MUCH_SOURCE, Float.MAX_VALUE);
        } else {
            // 上限未満なら罪を加算
            playerSins.setSinnedTimes(nextCount);
            playerSins.setSinGainCooldown(cooldown);
            PlayerSins.setPlayerSins(player, playerSins);
        }
    }
}