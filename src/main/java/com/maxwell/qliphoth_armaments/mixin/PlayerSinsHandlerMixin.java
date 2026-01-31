package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.entities.geburah.sins.GeburahTriggerSinEffectPacket;
import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdlib.network.FDPacketHandler;
import com.maxwell.qliphoth_armaments.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSinsHandler.class)
public class PlayerSinsHandlerMixin {
    @Inject(method = "sin(Lnet/minecraft/server/level/ServerPlayer;IFI)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onSin(ServerPlayer player, int cooldown, float soundPitch, int amount, CallbackInfo ci) {
        ci.cancel();
        PlayerSins playerSins = PlayerSins.getPlayerSins(player);
        if (playerSins == null || !BossUtil.isPlayerInSurvival(player) || !player.isAlive())
            return;
        if (playerSins.getSinGainCooldown() > 0)
            return;
        FDPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GeburahTriggerSinEffectPacket(soundPitch));
        int current = playerSins.getSinnedTimes();
        if (current + amount >= 6) {
            playerSins.setSinnedTimes(current + amount);
            playerSins.setSinGainCooldown(cooldown);
            PlayerSins.setPlayerSins(player, playerSins);
        } else {
            int nextCount = current + amount;
            playerSins.setSinnedTimes(nextCount);
            playerSins.setSinGainCooldown(cooldown);
            PlayerSins.setPlayerSins(player, playerSins);
        }
    }

    @Unique
    private static boolean qliphoth$hasFullArmor(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }
}