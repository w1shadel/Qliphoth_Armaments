package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.client.overlay.GeburahSinsOverlay;
import com.maxwell.qliphoth_armaments.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GeburahSinsOverlay.class)
public class GeburahSinsOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void onRender(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean hasSpecificArmor = mc.player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get());
            if (hasSpecificArmor) {
                ci.cancel();
            }
        }
    }
}