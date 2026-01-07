package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.entities.geburah.sins.GeburahTriggerSinEffectPacket;
import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdbosses.init.BossDamageSources;
import com.maxwell.qliphoth_armaments.common.event.ArmorEventHandler;
import com.maxwell.qliphoth_armaments.common.item.GeburahArmorItem;
import com.maxwell.qliphoth_armaments.init.ModItems;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
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
        if (!playerSins.isGainingSinsOnCooldown() && BossUtil.isPlayerInSurvival(player) && player.isAlive()) {
            PacketDistributor.sendToPlayer(player, new GeburahTriggerSinEffectPacket(soundPitch), new CustomPacketPayload[0]);
            int sinnedTimes = playerSins.getSinnedTimes();
            boolean isArbiter = hasFullArmor(player);
            int maxSins = isArbiter ? 12 : 6;
            if (sinnedTimes + amount >= maxSins) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (isArbiter && GeburahArmorItem.hasJusticeCore(chest)) {
                    ArmorEventHandler.consumeCoreAndReset(player, chest, playerSins);
                    return;
                }
                player.hurt(BossDamageSources.GEBURAH_SINNED_TOO_MUCH_SOURCE, Float.MAX_VALUE);

            } else {
                playerSins.setSinnedTimes(sinnedTimes + amount);
                playerSins.setSinGainCooldown(cooldown);
                PlayerSins.setPlayerSins(player, playerSins);
            }
        }
    }

    @Unique
    private static boolean hasFullArmor(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }
}