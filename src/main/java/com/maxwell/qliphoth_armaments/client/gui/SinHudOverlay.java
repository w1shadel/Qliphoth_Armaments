package com.maxwell.qliphoth_armaments.client.gui;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdlib.util.rendering.FDEasings;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SinHudOverlay {

    public static final ResourceLocation SINS = ResourceLocation.fromNamespaceAndPath(FDBosses.MOD_ID, "textures/gui/geburah_sin.png");

    private static int ticker = 0;
    private static int oldSinCount = 0;

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "sin_hud"), new GeburahSinLayer());
    }

    @EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientTickHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                ticker = 0;
                oldSinCount = 0;
            } else {
                int sinnedTimes = player.getData(ModAttachments.SIN);
                if (sinnedTimes != oldSinCount) {
                    oldSinCount = sinnedTimes;
                    ticker = 100;
                }
                ticker = Mth.clamp(ticker - 1, 0, Integer.MAX_VALUE);
            }
        }
    }

    static class GeburahSinLayer implements LayeredDraw.Layer {
        @Override
        public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null && !mc.options.hideGui && hasGeburahArmor(player)) {
                float pticks = deltaTracker.getGameTimeDeltaPartialTick(false);
                Window window = mc.getWindow();
                float height = (float) window.getGuiScaledHeight();
                float width = (float) window.getGuiScaledWidth();
                int sinnedTimes = player.getData(ModAttachments.SIN);
                int maxSins = 12;
                float centerY = height / 2.0F;
                float baseIconH = 33.0F;
                float baseIconW = 28.0F;
                float spacing = 25.0F;
                // フェード / スライド計算
                float visibleT = Mth.clamp((float) ticker / 100.0F, 0.0F, 1.0F);
                float alpha = Mth.lerp(visibleT, 0.6F, 1.0F); // 0.6 -> 1.0
                float slideT = 1.0F;
                if (ticker < 20) {
                    // スライドイン時の部分 (0..1)
                    slideT = Mth.clamp(((float) ticker - pticks) / 20.0F, 0.0F, 1.0F);
                }
                float slideOffset = FDEasings.easeInOut(1.0F - slideT) * 48.0F; // 右からスライド
                // 右寄せ位置
                float xOffset = width - 6.0F - baseIconW; // 右端からの基本オフセット
                xOffset += slideOffset;
                // 縦の開始オフセット（中央揃え）
                float startOffset;
                if (maxSins % 2 == 0) {
                    startOffset = spacing * (float) (maxSins / 2);
                } else {
                    startOffset = spacing / 2.0F + spacing * (float) (maxSins / 2);
                }
                FDRenderUtil.bindTexture(SINS);
                // まず全ての空アイコン（下地）を描画、その上に塗りつぶしを重ねる
                for (int i = 0; i < maxSins; ++i) {
                    float y = centerY - startOffset + (float) i * spacing;
                    // 空アイコンのUVは上半分（v=0.0）
                    FDRenderUtil.blitWithBlend(
                            graphics.pose(),
                            xOffset,
                            y,
                            baseIconW,
                            baseIconH,
                            0.0F, 0.0F,
                            1.0F, 0.5F,
                            1.0F, 1.0F,
                            1.0F * alpha,
                            0.0F
                    );
                }
                // 塗りアイコンを重ねる
                for (int i = 0; i < sinnedTimes && i < maxSins; ++i) {
                    float y = centerY - startOffset + (float) i * spacing;
                    // 最後に増えたアイコンは少し強調（拡大）する
                    float scale = 1.0F;
                    if (i == sinnedTimes - 1 && ticker > 0) {
                        float pulseT = Mth.clamp(((float) ticker) / 20.0F, 0.0F, 1.0F);
                        float eased = FDEasings.easeInOut(1.0F - pulseT);
                        scale = 1.0F + eased * 0.18F; // 最大 +18%
                    }
                    float w = baseIconW * scale;
                    float h = baseIconH * scale;
                    float dx = xOffset - (w - baseIconW); // 拡大時に右辺に留まるよう調整
                    float dy = y - (h - baseIconH) / 2.0F;
                    float texV = 0.5F; // 塗り部分はテクスチャ下半分
                    FDRenderUtil.blitWithBlend(
                            graphics.pose(),
                            dx,
                            dy,
                            w,
                            h,
                            0.0F, texV,
                            1.0F, 0.5F,
                            1.0F, 1.0F,
                            1.0F * alpha,
                            0.0F
                    );
                }
            }
        }

        private boolean hasGeburahArmor(Player player) {
            return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get());
        }
    }
}
