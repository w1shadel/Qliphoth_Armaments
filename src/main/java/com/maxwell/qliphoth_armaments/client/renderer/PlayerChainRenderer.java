package com.maxwell.qliphoth_armaments.client.renderer;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.maxwell.qliphoth_armaments.common.entity.PlayerChainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PlayerChainRenderer extends EntityRenderer<PlayerChainEntity> {

    private static final ResourceLocation TEXTURE = FDBosses.location("textures/util/chain_segment.png");

    public PlayerChainRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(PlayerChainEntity chain, float yaw, float pticks, PoseStack matrices, MultiBufferSource src, int light) {
        Vec3 startPos;
        if (chain.isAttackMode()) {
            startPos = chain.getOriginPos();
        } else {
            Player owner = chain.getOwnerClient();
            if (owner == null) return;
            startPos = getPlayerHandPos(owner, pticks);
        }
        if (startPos == null) return;
        double x = Mth.lerp(pticks, chain.xo, chain.getX());
        double y = Mth.lerp(pticks, chain.yo, chain.getY());
        double z = Mth.lerp(pticks, chain.zo, chain.getZ());
        Vec3 currentEntityPos = new Vec3(x, y, z);
        Vec3 between = startPos.subtract(currentEntityPos);
        float length = (float) between.length();
        if (length < 0.01f) return;
        matrices.pushPose();
        FDRenderUtil.applyMovementMatrixRotations(matrices, between);
        VertexConsumer b = src.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f m = matrices.last().pose();
        float width = 0.15F;
        float timeOffset = (chain.tickCount + pticks) * 0.1F;
        float vScale = length * 2.0F;
        drawChainSegmentAnimated(b, m, length, width, light, vScale, timeOffset);
        matrices.popPose();
        super.render(chain, yaw, pticks, matrices, src, light);
    }

    private void drawChainSegmentAnimated(VertexConsumer b, Matrix4f m, float length, float width, int light, float vScale, float vOffset) {
        vertex(b, m, -width, 0, 0, 0, vOffset, light);
        vertex(b, m, -width, length, 0, 0, vScale + vOffset, light);
        vertex(b, m, width, length, 0, 1, vScale + vOffset, light);
        vertex(b, m, width, 0, 0, 1, vOffset, light);
        vertex(b, m, 0, 0, -width, 0, vOffset, light);
        vertex(b, m, 0, length, -width, 0, vScale + vOffset, light);
        vertex(b, m, 0, length, width, 1, vScale + vOffset, light);
        vertex(b, m, 0, 0, width, 1, vOffset, light);
    }

    private void vertex(VertexConsumer b, Matrix4f m, float x, float y, float z, float u, float v, int light) {
        b.addVertex(m, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }

    private Vec3 getPlayerHandPos(Player player, float pticks) {
        double x = Mth.lerp(pticks, player.xo, player.getX());
        double y = Mth.lerp(pticks, player.yo, player.getY());
        double z = Mth.lerp(pticks, player.zo, player.getZ());
        float yaw = Mth.lerp(pticks, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        boolean isRightHand = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
        float offsetSide = isRightHand ? 0.5F : -0.5F;
        double ox = -Math.cos(yaw) * offsetSide;
        double oz = -Math.sin(yaw) * offsetSide;
        double oy = player.getBbHeight() * 0.65;
        return new Vec3(x + ox, y + oy, z + oz);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerChainEntity entity) {
        return TEXTURE;
    }
}