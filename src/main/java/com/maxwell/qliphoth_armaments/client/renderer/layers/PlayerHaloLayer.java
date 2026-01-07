package com.maxwell.qliphoth_armaments.client.renderer.layers;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PlayerHaloLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation HALO_TEXTURE = FDBosses.location("textures/entities/geburah/geburah_halo.png");

    public PlayerHaloLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        PlayerSins sins = PlayerSins.getPlayerSins(player);
        if (sins == null) return;
        int sinnedTimes = sins.getSinnedTimes();
        if (sinnedTimes < 7) return;
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.8F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        float scale = 2.0F;
        poseStack.scale(scale, scale, scale);
        float time = ageInTicks * 2.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(time));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(HALO_TEXTURE));
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        int light = 15728880;
        float size = 1.0F;
        float half = size / 2.0F;
        vertex(vertexConsumer, pose, -half, -half, 0, 1, light);
        vertex(vertexConsumer, pose, half, -half, 1, 1, light);
        vertex(vertexConsumer, pose, half, half, 1, 0, light);
        vertex(vertexConsumer, pose, -half, half, 0, 0, light);
        poseStack.popPose();
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float u, float v, int light) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}