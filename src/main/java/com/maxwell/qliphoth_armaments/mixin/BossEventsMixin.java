package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.BossEvents;
import com.maxwell.qliphoth_armaments.common.item.QAModWeapon;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossEvents.class)
public class BossEventsMixin {
    @Inject(method = "rightClickItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event, CallbackInfo ci) {
        if (event.getItemStack().getItem() instanceof QAModWeapon) {
            ci.cancel();
        }
    }
}