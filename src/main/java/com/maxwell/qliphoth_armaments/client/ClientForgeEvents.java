package com.maxwell.qliphoth_armaments.client;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.item.ConductorRequiemItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.getItem() instanceof ConductorRequiemItem) {
            if (event.getPlayer().isUsingItem()) {
                int useTicks = event.getPlayer().getTicksUsingItem();
                float maxTicks = 20.0F;
                float progress = Math.min(useTicks / maxTicks, 1.0F);
                float zoom = 1.0F - (0.2F * progress);
                event.setNewFovModifier(event.getNewFovModifier() * zoom);
            }
            if (stack.getOrCreateTag().contains("ChesedRecoilTimer")) {
                event.setNewFovModifier(event.getNewFovModifier() * 0.7F);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;
        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();
        AABB renderBounds = player.getBoundingBox().inflate(64.0);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, renderBounds, (entity) -> entity != player);
        for (LivingEntity entity : nearbyEntities) {
            QAElements element = ElementalReactionManager.getCurrentState(entity);
            if (element != null) {
                renderElementIcon(poseStack, entity, element, partialTicks);
            }
        }
    }

    private static void renderElementIcon(PoseStack poseStack, LivingEntity entity, QAElements element, float partialTicks) {
        ResourceLocation iconTexture;
        if (element == QAElements.FIRE) {
            iconTexture = new ResourceLocation(QA.MOD_ID, "textures/gui/effect/fire_icon.png");
        } else if (element == QAElements.ICE) {
            iconTexture = new ResourceLocation(QA.MOD_ID, "textures/gui/effect/ice_icon.png");
        } else {
            iconTexture = new ResourceLocation(QA.MOD_ID, "textures/gui/effect/lightning_icon.png");
        }
        double entityX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float width = entity.getBbWidth();
        float scaleMultiplier = Mth.clamp(width / 0.6F, 0.8F, 2.5F);
        float finalScale = 0.4F * scaleMultiplier;
        float yOffset = entity.getBbHeight() + 0.5F + (finalScale * 0.5F);
        poseStack.pushPose();
        poseStack.translate(entityX - camPos.x, entityY - camPos.y + yOffset, entityZ - camPos.z);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(-finalScale, -finalScale, finalScale);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShaderTexture(0, iconTexture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        poseStack.pushPose();
        float outlineScale = 1.2F;
        poseStack.scale(outlineScale, outlineScale, outlineScale);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix4f outlineMatrix = poseStack.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(outlineMatrix, -0.5F, -0.5F, 0).uv(0, 0).endVertex();
        buffer.vertex(outlineMatrix, -0.5F, 0.5F, 0).uv(0, 1).endVertex();
        buffer.vertex(outlineMatrix, 0.5F, 0.5F, 0).uv(1, 1).endVertex();
        buffer.vertex(outlineMatrix, 0.5F, -0.5F, 0).uv(1, 0).endVertex();
        tesselator.end();
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.translate(0, 0, -0.01F);
        Matrix4f matrix = poseStack.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, -0.5F, -0.5F, 0).uv(0, 0).endVertex();
        buffer.vertex(matrix, -0.5F, 0.5F, 0).uv(0, 1).endVertex();
        buffer.vertex(matrix, 0.5F, 0.5F, 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, 0.5F, -0.5F, 0).uv(1, 0).endVertex();
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}