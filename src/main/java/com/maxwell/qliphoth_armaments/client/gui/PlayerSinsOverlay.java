package com.maxwell.qliphoth_armaments.client.gui;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.content.entities.geburah.distortion_sphere.DistortionSphereEffect;
import com.finderfeed.fdbosses.content.entities.geburah.distortion_sphere.DistortionSphereEffectHandler;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

@EventBusSubscriber(
        modid = QA.MOD_ID,
        value = {Dist.CLIENT}
)
public class PlayerSinsOverlay implements LayeredDraw.Layer {
    // FDBossesの罪アイコンテクスチャを使用
    public static final ResourceLocation SINS = FDBosses.location("textures/gui/geburah_sin.png");
    // 画面ひび割れ用のバニラテクスチャ
    public static final ResourceLocation SHATTER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_9.png");

    private static int oldSinCount = 0;

    // アニメーション制御用変数
    private static int animTicker = 0;       // アイコンのポップアップ用
    private static int shakeTicker = 0;      // 画面揺れ用 (アイコン自体)
    private static int flashTicker = 0;      // 画面フラッシュ用
    private static int shatterTicker = 0;    // 画面ひび割れ用
    private static int displayTicker = 0;    // 表示維持タイマー

    // スライド位置制御用
    private static float prevSlideOffset = -100.0F;
    private static float currentSlideOffset = -100.0F;
    private static final float HIDDEN_OFFSET = -100.0F;

    private static final int ANIM_DURATION = 10;
    private static final int SHAKE_DURATION = 5;
    private static final int FLASH_DURATION = 15;
    private static final int SHATTER_DURATION = 40;
    private static final int DISPLAY_TIME = 100;

    private static final Random random = new Random();

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        float pticks = deltaTracker.getGameTimeDeltaPartialTick(false);
        Window window = mc.getWindow();
        float screenWidth = (float) window.getGuiScaledWidth();
        float screenHeight = (float) window.getGuiScaledHeight();
        // 1. 画面破壊（Shatter）エフェクト (最背面)
        if (shatterTicker > 0) {
            renderScreenShatter(graphics.pose(), pticks, screenWidth, screenHeight);
        }
        // 2. 画面フラッシュ (その上)
        if (flashTicker > 0) {
            renderScreenFlash(graphics.pose(), pticks, screenWidth, screenHeight);
        }
        // スライド位置計算
        float renderSlideX = Mth.lerp(pticks, prevSlideOffset, currentSlideOffset);
        // 表示中でないならアイコン描画はスキップ
        if (renderSlideX < HIDDEN_OFFSET + 1.0F && displayTicker <= 0) {
            return;
        }
        // FDBossesのPlayerSinsを使わず、ModAttachmentsからデータを取得
        int sinnedTimes = player.getData(ModAttachments.SIN);
        boolean isArbiter = hasFullArmor(player);
        int maxSins = isArbiter ? 12 : 6;
        float centerY = screenHeight / 2.0F;
        // --- レイアウト設定 ---
        float iconWidth = 28.0F;
        float iconHeight = 33.0F;
        float stepY = 25.0F;
        int maxPerCol = 6;
        int rows = Math.min(maxSins, maxPerCol); // 1列あたりの数に基づく行数計算（実際は列数に近い概念）
        // 縦方向の開始位置調整
        float startOffset = (stepY * rows) / 2.0F;
        if (rows % 2 != 0) {
            startOffset += stepY / 2.0F;
        }
        float drawStartY = centerY - startOffset;
        float baseX = 5.0F;
        float stepX = 30.0F;
        // --- アイコン用シェイク（揺れ） ---
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
        // --- 罪アイコン（Foreground） ---
        FDRenderUtil.bindTexture(SINS);
        for (int i = 0; i < maxSins; ++i) {
            // 列と行の計算 (6個で折り返し)
            int col = i / maxPerCol;
            int row = i % maxPerCol;
            float x = baseX + (col * stepX);
            float y = drawStartY + (row * stepY);
            // 罪の数に応じてテクスチャのUVオフセットを変更 (点灯/消灯)
            float texOffset = i < sinnedTimes ? 0.5F : 0.0F;
            // 最新の罪が増えたときのアニメーション
            boolean isAnimating = (i == sinnedTimes - 1) && (animTicker > 0);
            float scale = 1.0F;
            if (isAnimating) {
                float progress = 1.0F - ((float) animTicker - pticks) / (float) ANIM_DURATION;
                progress = Mth.clamp(progress, 0.0F, 1.0F);
                float bump = Mth.sin(progress * (float) Math.PI);
                scale = 1.0F + bump * 0.4F;
            }
            pose.pushPose();
            // 中心を基準にスケール
            float pivotX = x + iconWidth / 2.0F;
            float pivotY = y + iconHeight / 2.0F;
            pose.translate(pivotX, pivotY, 0.0F);
            pose.scale(scale, scale, 1.0F);
            pose.translate(-pivotX, -pivotY, 0.0F);
            FDRenderUtil.blitWithBlend(
                    pose, x, y, iconWidth, iconHeight,
                    0.0F, texOffset, 1.0F, 0.5F, 1.0F, 1.0F, 0.0F, 1.0F
            );
            pose.popPose();
        }
        pose.popPose();
    }

    @SubscribeEvent
    public static void tickEvent(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            oldSinCount = 0;
            animTicker = 0;
            shakeTicker = 0;
            flashTicker = 0;
            shatterTicker = 0;
            displayTicker = 0;
            prevSlideOffset = HIDDEN_OFFSET;
            currentSlideOffset = HIDDEN_OFFSET;
        } else {
            // データ取得
            int sinnedTimes = mc.player.getData(ModAttachments.SIN);
            boolean isArbiter = hasFullArmor(mc.player);
            int maxSins = isArbiter ? 12 : 6;
            if (sinnedTimes != oldSinCount) {
                // 何かしら変動があったら表示時間を延長
                displayTicker = DISPLAY_TIME;
                // 罪が増えた場合
                if (sinnedTimes > oldSinCount) {
                    animTicker = ANIM_DURATION;
                    shakeTicker = SHAKE_DURATION;
                    // 赤いフラッシュ
                    flashTicker = FLASH_DURATION;
                }
                // ★罪が減った かつ 直前が最大値以上だった場合（断罪時）
                else if (sinnedTimes == 0 && oldSinCount >= maxSins) {
                    // 1. 画面破壊エフェクト開始
                    shatterTicker = SHATTER_DURATION;
                    // 2. ガラスが割れる音
                    mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    // 3. 空間歪みエフェクト (FDBossesの機能を利用)
                    triggerDistortionEffect(mc.player);
                }
                oldSinCount = sinnedTimes;
            }
            // スライド位置更新
            prevSlideOffset = currentSlideOffset;
            float targetOffset = (displayTicker > 0) ? 0.0F : HIDDEN_OFFSET;
            currentSlideOffset += (targetOffset - currentSlideOffset) * 0.15F;
            if (Math.abs(targetOffset - currentSlideOffset) < 0.1F) {
                currentSlideOffset = targetOffset;
            }
            // ティッカー更新
            if (animTicker > 0) animTicker--;
            if (shakeTicker > 0) shakeTicker--;
            if (flashTicker > 0) flashTicker--;
            if (shatterTicker > 0) shatterTicker--;
            if (displayTicker > 0) displayTicker--;
        }
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
            // DistortionSphereEffect(位置, 持続時間, 半径, 幅, 床高さ)
            DistortionSphereEffect effect = new DistortionSphereEffect(pos, 20, 15.0F, 1.0F, floorY);
            DistortionSphereEffectHandler.setDistortionSphereEffect(effect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderScreenShatter(PoseStack matrices, float pticks, float screenWidth, float screenHeight) {
        float progress = ((float) shatterTicker - pticks) / (float) SHATTER_DURATION;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        float alpha = 0.8F * progress;
        // バニラのブロック破壊テクスチャのサイズは16x16
        float texSize = 16.0F;
        FDRenderUtil.bindTexture(SHATTER_TEXTURE);
        // 画面全体に描画
        // 引数: matrices, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight, z, alpha
        FDRenderUtil.blitWithBlend(
                matrices,
                0.0F, 0.0F,
                screenWidth, screenHeight,
                0.0F, 0.0F,
                texSize, texSize, // 使用するテクスチャの幅と高さ (16x16)
                texSize, texSize, // テクスチャ全体の幅と高さ (16x16)
                0.0F, alpha
        );
    }

    private void renderScreenFlash(PoseStack matrices, float pticks, float screenWidth, float screenHeight) {
        float progress = ((float) flashTicker - pticks) / (float) FLASH_DURATION;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        float p = (float) Math.sin(progress * Math.PI / 2.0);
        // 赤黒いフラッシュ色
        float r = 0.8F;
        float g = 0.0F;
        float b = 0.1F;
        float a = 0.6F * p;
        // 画面両端から迫る演出
        float width = screenWidth / 2.0F * 0.3F * p;
        float otherSideStart = screenWidth - width;
        FDRenderUtil.fill(matrices, 0.0F, 0.0F, width, screenHeight, r, g, b, a, r, g, b, 0.0F, r, g, b, 0.0F, r, g, b, a);
        FDRenderUtil.fill(matrices, otherSideStart, 0.0F, width, screenHeight, r, g, b, 0.0F, r, g, b, a, r, g, b, a, r, g, b, 0.0F);
    }
}