package com.maxwell.qliphoth_armaments.client.renderer;


import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.maxwell.qliphoth_armaments.common.entity.PlayerChainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
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
        Player owner = chain.getOwnerClient();
        if (owner == null) return;

        matrices.pushPose();

        Vec3 handPos = getPlayerHandPos(owner, pticks);






        double lerpX = Mth.lerp(pticks, chain.xOld, chain.getX());
        double lerpY = Mth.lerp(pticks, chain.yOld, chain.getY());
        double lerpZ = Mth.lerp(pticks, chain.zOld, chain.getZ());
        Vec3 chainPos = new Vec3(lerpX, lerpY, lerpZ);

        Vec3 between = handPos.subtract(chainPos);



        FDRenderUtil.applyMovementMatrixRotations(matrices, between);

        VertexConsumer b = src.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(chain)));
        float length = (float)between.length();
        Matrix4f m = matrices.last().pose();
        float width = 0.15F; 

        if (!chain.getPassengers().isEmpty()) {
            matrices.mulPose(Axis.YP.rotationDegrees(-135.0F * (chain.tickCount + pticks) * 0.1F)); 
        }


        drawChainSegment(b, m, length, width, light);

        matrices.popPose();

        super.render(chain, yaw, pticks, matrices, src, light);
    }

    private void drawChainSegment(VertexConsumer b, Matrix4f m, float length, float width, int light) {

        float textureScale = length * 3.0F;

        vertex(b, m, -width, 0, 0, 0, 0, light);
        vertex(b, m, -width, length, 0, textureScale, 0, light);
        vertex(b, m, width, length, 0, textureScale, 1, light);
        vertex(b, m, width, 0, 0, 0, 1, light);

        vertex(b, m, width, 0, 0, 0, 1, light);
        vertex(b, m, width, length, 0, textureScale, 1, light);
        vertex(b, m, -width, length, 0, textureScale, 0, light);
        vertex(b, m, -width, 0, 0, 0, 0, light);

        vertex(b, m, 0, 0, -width, 0, 0, light);
        vertex(b, m, 0, length, -width, textureScale, 0, light);
        vertex(b, m, 0, length, width, textureScale, 1, light);
        vertex(b, m, 0, 0, width, 0, 1, light);

        vertex(b, m, 0, 0, width, 0, 1, light);
        vertex(b, m, 0, length, width, textureScale, 1, light);
        vertex(b, m, 0, length, -width, textureScale, 0, light);
        vertex(b, m, 0, 0, -width, 0, 0, light);
    }

    private void vertex(VertexConsumer b, Matrix4f m, float x, float y, float z, float u, float v, int light) {
        b.vertex(m, x, y, z)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0) 
                .endVertex();
    }

    /**
     * プレイヤーの右手の位置を推定する
     */
    private Vec3 getPlayerHandPos(Player player, float pticks) {

        double x = Mth.lerp(pticks, player.xo, player.getX());
        double y = Mth.lerp(pticks, player.yo, player.getY());
        double z = Mth.lerp(pticks, player.zo, player.getZ());

        float yaw = Mth.lerp(pticks, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;


        boolean isRightHand = player.getMainArm() == HumanoidArm.RIGHT;
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

    @Override
    public boolean shouldRender(PlayerChainEntity p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }
}