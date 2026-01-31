package com.maxwell.qliphoth_armaments.client.gui;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.content.entities.geburah.distortion_sphere.DistortionSphereEffect;
import com.finderfeed.fdbosses.content.entities.geburah.distortion_sphere.DistortionSphereEffectHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, value = Dist.CLIENT)
public class PlayerSinsOverlay implements IGuiOverlay {
    public static final ResourceLocation SINS = FDBosses.location("textures/gui/geburah_sin.png");
    private static int oldSinCount = -1;
    private static int animTicker = 0;
    private static int shakeTicker = 0;
    private static int flashTicker = 0;
    private static int displayTicker = 0;
    private static final float HIDDEN_OFFSET = -120.0F;
    private static float currentSlideOffset = HIDDEN_OFFSET;
    private static float prevSlideOffset = HIDDEN_OFFSET;
    private static final int ANIM_DURATION = 10;
    private static final int SHAKE_DURATION = 5;
    private static final int FLASH_DURATION = 15;
    private static final int DISPLAY_TIME = 100;
    private static final Random random = new Random();

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (flashTicker > 0) {
            renderScreenFlash(graphics, partialTick, (float) screenWidth, (float) screenHeight);
        }
        float renderSlideX = Mth.lerp(partialTick, prevSlideOffset, currentSlideOffset);
        if (renderSlideX <= HIDDEN_OFFSET + 0.1F && displayTicker <= 0) {
            return;
        }
        int sinnedTimes = 0;
        try {
            PlayerSins sinsData = PlayerSins.getPlayerSins(player);
            if (sinsData != null) {
                sinnedTimes = sinsData.getSinnedTimes();
            }
        } catch (Exception ignored) {
        }
        boolean isArbiter = hasFullArmor(player);
        int maxSins = isArbiter ? 12 : 6;
        float centerY = screenHeight / 2.0F;
        float iconWidth = 28.0F;
        float iconHeight = 33.0F;
        float stepY = 25.0F;
        int maxPerCol = 6;
        int rows = Math.min(maxSins, maxPerCol);
        float startOffset = (stepY * rows) / 2.0F;
        if (rows % 2 != 0) {
            startOffset += stepY / 2.0F;
        }
        float drawStartY = centerY - startOffset;
        float baseX = 5.0F;
        float stepX = 30.0F;
        float shakeX = 0.0F;
        float shakeY = 0.0F;
        if (shakeTicker > 0) {
            float shakeIntensity = (float) shakeTicker / (float) SHAKE_DURATION * 2.0F;
            shakeX = (random.nextFloat() - 0.5F) * shakeIntensity;
            shakeY = (random.nextFloat() - 0.5F) * shakeIntensity;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(renderSlideX + shakeX, shakeY, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, SINS);
        for (int i = 0; i < maxSins; ++i) {
            int col = i / maxPerCol;
            int row = i % maxPerCol;
            float x = baseX + (col * stepX);
            float y = drawStartY + (row * stepY);
            int vOffset = (i < sinnedTimes) ? (int) iconHeight : 0;
            boolean isAnimating = (i == sinnedTimes - 1) && (animTicker > 0);
            float scale = 1.0F;
            if (isAnimating) {
                float progress = 1.0F - ((float) animTicker - partialTick) / (float) ANIM_DURATION;
                progress = Mth.clamp(progress, 0.0F, 1.0F);
                float bump = Mth.sin(progress * (float) Math.PI);
                scale = 1.0F + bump * 0.4F;
            }
            pose.pushPose();
            float pivotX = x + iconWidth / 2.0F;
            float pivotY = y + iconHeight / 2.0F;
            pose.translate(pivotX, pivotY, 0.0F);
            pose.scale(scale, scale, 1.0F);
            pose.translate(-pivotX, -pivotY, 0.0F);
            graphics.blit(SINS, (int) x, (int) y, 0, vOffset, (int) iconWidth, (int) iconHeight, (int) iconWidth, (int) (iconHeight * 2));
            pose.popPose();
        }
        pose.popPose();
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            oldSinCount = -1;
            displayTicker = 0;
            currentSlideOffset = HIDDEN_OFFSET;
            prevSlideOffset = HIDDEN_OFFSET;
        } else {
            int sinnedTimes = 0;
            try {
                PlayerSins sinsData = PlayerSins.getPlayerSins(mc.player);
                if (sinsData != null) {
                    sinnedTimes = sinsData.getSinnedTimes();
                }
            } catch (Exception ignored) {
            }
            boolean isArbiter = hasFullArmor(mc.player);
            int maxSins = isArbiter ? 12 : 6;
            if (sinnedTimes != oldSinCount) {
                if (oldSinCount != -1) {
                    displayTicker = DISPLAY_TIME;
                    if (sinnedTimes > oldSinCount) {
                        animTicker = ANIM_DURATION;
                        shakeTicker = SHAKE_DURATION;
                        flashTicker = FLASH_DURATION;
                    } else if (sinnedTimes == 0 && oldSinCount >= maxSins) {
                        mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        triggerDistortionEffect(mc.player);
                    }
                }
                oldSinCount = sinnedTimes;
            }
            prevSlideOffset = currentSlideOffset;
            float targetOffset = (displayTicker > 0) ? 0.0F : HIDDEN_OFFSET;
            currentSlideOffset += (targetOffset - currentSlideOffset) * 0.15F;
            if (Math.abs(targetOffset - currentSlideOffset) < 0.1F) {
                currentSlideOffset = targetOffset;
            }
            if (animTicker > 0) animTicker--;
            if (shakeTicker > 0) shakeTicker--;
            if (flashTicker > 0) flashTicker--;
            if (displayTicker > 0) displayTicker--;
        }
    }

    private void renderScreenFlash(GuiGraphics graphics, float pticks, float screenWidth, float screenHeight) {
        float progress = ((float) flashTicker - pticks) / (float) FLASH_DURATION;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        float p = (float) Math.sin(progress * Math.PI / 2.0);
        int r = (int) (0.8F * 255);
        int g = 0;
        int b = (int) (0.1F * 255);
        int alpha = (int) ((0.6F * p) * 255);
        int color = (alpha << 24) | (r << 16) | (g << 8) | b;
        float width = screenWidth / 2.0F * 0.3F * p;
        graphics.fill(0, 0, (int) width, (int) screenHeight, color);
        graphics.fill((int) (screenWidth - width), 0, (int) screenWidth, (int) screenHeight, color);
    }

    private static boolean hasFullArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }

    private static void triggerDistortionEffect(Player player) {
        try {
            Vec3 pos = player.position().add(0, 1.0, 0);
            float floorY = (float) player.getY();
            DistortionSphereEffect effect = new DistortionSphereEffect(pos, 20, 15.0F, 1.0F, floorY);
            DistortionSphereEffectHandler.setDistortionSphereEffect(effect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}