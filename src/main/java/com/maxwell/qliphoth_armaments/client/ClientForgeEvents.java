package com.maxwell.qliphoth_armaments.client;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.item.ConductorRequiemItem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
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
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.contains("ChesedRecoilTimer")) {
                event.setNewFovModifier(event.getNewFovModifier() * 0.7F);
            }
        }
    }

    private static final Map<ResourceLocation, RenderType> ICON_RENDER_TYPES = new HashMap<>();

    private static RenderType getIconRenderType(ResourceLocation texture) {
        return ICON_RENDER_TYPES.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false);
            return RenderType.create("element_icon", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true, state);
        });
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
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        AABB renderBounds = player.getBoundingBox().inflate(64.0);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, renderBounds, entity -> entity != player);
        for (LivingEntity entity : nearbyEntities) {
            QAElements element = ElementalReactionManager.getCurrentState(entity);
            if (element != null) {
                renderElementIcon(poseStack, bufferSource, entity, element, partialTicks);
            }
        }
        bufferSource.endBatch();
    }

    private static void renderElementIcon(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, QAElements element, float partialTicks) {
        ResourceLocation iconTexture;
        if (element == QAElements.FIRE) {
            iconTexture = ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "textures/gui/effect/fire_icon.png");
        } else if (element == QAElements.ICE) {
            iconTexture = ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "textures/gui/effect/ice_icon.png");
        } else {
            iconTexture = ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "textures/gui/effect/lightning_icon.png");
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
        RenderType renderType = getIconRenderType(iconTexture);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.2F, 1.2F);
        Matrix4f outlineMatrix = poseStack.last().pose();
        float r = 0.0f, g = 0.0f, b = 0.0f, a = 1.0f;
        buffer.addVertex(outlineMatrix, -0.5F, 0.5F, 0).setColor(r, g, b, a).setUv(0, 1);
        buffer.addVertex(outlineMatrix, 0.5F, 0.5F, 0).setColor(r, g, b, a).setUv(1, 1);
        buffer.addVertex(outlineMatrix, 0.5F, -0.5F, 0).setColor(r, g, b, a).setUv(1, 0);
        buffer.addVertex(outlineMatrix, -0.5F, -0.5F, 0).setColor(r, g, b, a).setUv(0, 0);
        poseStack.popPose();
        r = 1.0f;
        g = 1.0f;
        b = 1.0f;
        a = 1.0f;
        buffer.addVertex(matrix, -0.5F, 0.5F, 0).setColor(r, g, b, a).setUv(0, 1);
        buffer.addVertex(matrix, 0.5F, 0.5F, 0).setColor(r, g, b, a).setUv(1, 1);
        buffer.addVertex(matrix, 0.5F, -0.5F, 0).setColor(r, g, b, a).setUv(1, 0);
        buffer.addVertex(matrix, -0.5F, -0.5F, 0).setColor(r, g, b, a).setUv(0, 0);
        poseStack.popPose();
    }
}