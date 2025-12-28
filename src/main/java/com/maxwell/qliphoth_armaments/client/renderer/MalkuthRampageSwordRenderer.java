package com.maxwell.qliphoth_armaments.client.renderer;

import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthEntity;
import com.finderfeed.fdbosses.init.BossModels;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthRampageSwordEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class MalkuthRampageSwordRenderer extends EntityRenderer<MalkuthRampageSwordEntity> {

    private static FDModel model;

    public MalkuthRampageSwordRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        if (model == null) {
            model = new FDModel((FDModelInfo) BossModels.MALKUTH_SWORD.get());
        }
    }

    @Override
    public void render(MalkuthRampageSwordEntity entity, float yaw, float pticks, PoseStack matrices, MultiBufferSource src, int light) {
        matrices.pushPose();
        float scale = 14.0F * 0.08F;
        if (!entity.isLaunched()) {
            Player owner = entity.getOwner();
            if (owner != null && owner.isAlive()) {
                double px = Mth.lerp(pticks, owner.xo, owner.getX());
                double py = Mth.lerp(pticks, owner.yo, owner.getY());
                double pz = Mth.lerp(pticks, owner.zo, owner.getZ());
                double sx = Mth.lerp(pticks, entity.xOld, entity.getX());
                double sy = Mth.lerp(pticks, entity.yOld, entity.getY());
                double sz = Mth.lerp(pticks, entity.zOld, entity.getZ());
                matrices.translate(px - sx, py - sy, pz - sz);
                float bodyRot = Mth.lerp(pticks, owner.yBodyRotO, owner.yBodyRot);
                matrices.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
                int index = entity.getSwordIndex();
                boolean isRight = index < 3;
                int subIndex = index % 3;
                float sideDir = isRight ? 1.0F : -1.0F;
                float basePathX = 0.5F * sideDir;
                float basePathY = 1.7F;
                float basePathZ = 0.3F;
                float interval = 0.5F;
                float finalX = basePathX + (subIndex * interval * sideDir);
                float hover = Mth.sin((entity.tickCount + pticks) * 0.1F + index) * 0.05F;
                matrices.translate(finalX, basePathY + hover, basePathZ);
                matrices.mulPose(Axis.XP.rotationDegrees(20.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(10.0F * sideDir));
            }
        } else {
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getViewYRot(pticks)));
            matrices.mulPose(Axis.XP.rotationDegrees(-entity.getViewXRot(pticks)));
            matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
        matrices.scale(scale, scale, scale);
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        model.render(matrices, src.getBuffer(RenderType.entityTranslucent(MalkuthEntity.MALKUTH_SWORD_SOLID)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation emissiveTex = entity.getBossAttackType().isFire() ?
                MalkuthEntity.MALKUTH_FIRE_SWORD : MalkuthEntity.MALKUTH_ICE_SWORD;
        model.render(matrices, src.getBuffer(RenderType.eyes(emissiveTex)), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MalkuthRampageSwordEntity entity) {
        return MalkuthEntity.MALKUTH_SWORD_SOLID;
    }
}