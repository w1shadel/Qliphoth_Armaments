package com.maxwell.qliphoth_armaments.client.gui;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdbosses.init.BossRegistries;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, value = Dist.CLIENT)
public class CurrentSinOverlay implements IGuiOverlay {
    public static final ResourceLocation BASE_SCREEN = FDBosses.location("textures/entities/geburah/screen_sin/base_screen.png");
    public static final ResourceLocation EXTEND_SCREEN = new ResourceLocation(QA.MOD_ID, "textures/gui/extend_screen.png");
    private static List<PlayerSin> lastActiveSins = new ArrayList<>();
    private static int displayTicker = 0;
    private static final int DISPLAY_TIME = 100;
    private static float currentSlideOffset = 200.0F;
    private static float prevSlideOffset = 200.0F;
    private static final float HIDDEN_OFFSET = 200.0F;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (!hasFullArmor(player)) return;
        if (mc.level == null) return;
        float renderXOffset = Mth.lerp(partialTick, prevSlideOffset, currentSlideOffset);
        if (renderXOffset >= HIDDEN_OFFSET - 0.1F && displayTicker <= 0) {
            return;
        }
        if (lastActiveSins.isEmpty()) return;
        float w = (float) screenWidth;
        float h = (float) screenHeight;
        float baseW = 130.0F;
        float baseH = 46.0F;
        float extendW = baseW * (44.0F / 38.0F);
        float extendH = baseH * (24.0F / 18.0F);
        float baseX = w - baseW - 10.0F;
        float baseY = (h - baseH) / 2.0F;
        float extendX = baseX - (extendW - baseW) / 2.0F;
        float extendY = baseY - (extendH - baseH) / 2.0F;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(renderXOffset, 0.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, EXTEND_SCREEN);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);
        graphics.blit(EXTEND_SCREEN, (int) extendX, (int) extendY, 0, 0, (int) extendW, (int) extendH, (int) extendW, (int) extendH);
        RenderSystem.setShaderTexture(0, BASE_SCREEN);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
        graphics.blit(BASE_SCREEN, (int) baseX, (int) baseY, 0, 0, (int) baseW, (int) baseH, (int) baseW, (int) baseH);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.translate(0.0F, 0.0F, 1.0F);
        int count = Math.min(lastActiveSins.size(), 4);
        if (count > 0) {
            float iconSize = 26.0F;
            float gap = 4.0F;
            float totalWidth = (iconSize * count) + (gap * (count - 1));
            float startX = baseX + (baseW - totalWidth) / 2.0F;
            float iconY = baseY + (baseH - iconSize) / 2.0F - 3.0F;
            IForgeRegistry<PlayerSin> registry = RegistryManager.ACTIVE.getRegistry(BossRegistries.PLAYER_SIN.location());
            for (int i = 0; i < count; i++) {
                PlayerSin sin = lastActiveSins.get(i);
                ResourceLocation sinIcon = null;
                try {
                    if (registry != null) {
                        ResourceLocation sinKey = registry.getKey(sin);
                        if (sinKey != null) {
                            if (sinKey.getNamespace().equals(QA.MOD_ID)) {
                                sinIcon = new ResourceLocation(QA.MOD_ID, "textures/gui/sins/sin_" + sinKey.getPath() + ".png");
                            } else {
                                sinIcon = FDBosses.location("textures/entities/geburah/screen_sin/sin_" + sinKey.getPath() + ".png");
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                float currentIconX = startX + (iconSize + gap) * i;
                if (sinIcon != null) {
                    RenderSystem.setShaderTexture(0, sinIcon);
                    graphics.blit(sinIcon, (int) currentIconX, (int) iconY, 0, 0, (int) iconSize, (int) iconSize, (int) iconSize, (int) iconSize);
                }
            }
        }
        pose.translate(0.0F, 0.0F, 1.0F);
        float scale = 0.6F;
        pose.pushPose();
        float textX = baseX + (baseW / 2.0F);
        float textY = baseY + baseH - 12.0F;
        pose.translate(textX, textY, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawCenteredString(mc.font, "CURRENT SINS", 0, 0, 0xFFAAAAAA);
        pose.popPose();
        pose.popPose();
    }

    @SubscribeEvent
    public static void tickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            lastActiveSins.clear();
            displayTicker = 0;
            prevSlideOffset = HIDDEN_OFFSET;
            currentSlideOffset = HIDDEN_OFFSET;
        } else {
            List<PlayerSin> currentSins = getCurrentSins(mc.player);
            if (!isSameSinList(currentSins, lastActiveSins)) {
                lastActiveSins = new ArrayList<>(currentSins);
                if (!lastActiveSins.isEmpty()) {
                    displayTicker = DISPLAY_TIME;
                }
            }
            prevSlideOffset = currentSlideOffset;
            float targetOffset = (displayTicker > 0) ? 0.0F : HIDDEN_OFFSET;
            currentSlideOffset += (targetOffset - currentSlideOffset) * 0.15F;
            if (Math.abs(targetOffset - currentSlideOffset) < 0.1F) {
                currentSlideOffset = targetOffset;
            }
            if (displayTicker > 0) {
                displayTicker--;
            }
        }
    }

    private static List<PlayerSin> getCurrentSins(Player player) {
        List<PlayerSin> list = new ArrayList<>();
        try {
            PlayerSins sins = PlayerSins.getPlayerSins(player);
            if (sins != null) {
                for (ActivePlayerSinInstance instance : sins.getActiveSins()) {
                    list.add(instance.getSin());
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static boolean isSameSinList(List<PlayerSin> list1, List<PlayerSin> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!Objects.equals(list1.get(i), list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasFullArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }
}