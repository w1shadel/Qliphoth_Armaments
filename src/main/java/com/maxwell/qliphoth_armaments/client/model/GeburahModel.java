package com.maxwell.qliphoth_armaments.client.model;

import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GeburahModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "geburah_armor"), "main");

    private final ModelPart capeUpper;
    private final ModelPart capeMiddle;
    private final ModelPart capeUnder;

    private final ModelPart head_light;

    public GeburahModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        ModelPart headArmor = head.getChild("head_armor");
        if (headArmor.hasChild("head_light")) {
            this.head_light = headArmor.getChild("head_light");
        } else {
            this.head_light = new ModelPart(java.util.Collections.emptyList(), java.util.Collections.emptyMap());
        }
        ModelPart body = root.getChild("body");
        ModelPart bodyArmor = body.getChild("body_armor");
        if (!bodyArmor.hasChild("cape_upper")) {
            throw new RuntimeException("パーツ 'cape_upper' が見つかりません！Blockbenchの構造を確認してください。");
        }
        this.capeUpper = bodyArmor.getChild("cape_upper");
        this.capeMiddle = this.capeUpper.getChild("cape_middle");
        this.capeUnder = this.capeMiddle.getChild("capu_under");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 50).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 50).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition leftarm_armor = left_arm.addOrReplaceChild("leftarm_armor", CubeListBuilder.create().texOffs(28, 23).addBox(4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                .texOffs(0, 56).addBox(3.4F, -1.0F, -2.6F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offset(-5.0F, -2.0F, 0.0F));
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body_armor = body.addOrReplaceChild("body_armor", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 1.0F, -2.5F, 8.0F, 11.0F, 5.0F, new CubeDeformation(0.28F))
                .texOffs(22, 55).addBox(-0.5F, 3.2F, -2.8F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.28F))
                .texOffs(58, 18).addBox(-1.0F, 0.6F, -2.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.28F))
                .texOffs(58, 14).addBox(-3.5F, 3.0F, -2.7F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.28F))
                .texOffs(58, 22).addBox(1.5F, 2.9F, -2.7F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.28F))
                .texOffs(20, 30).addBox(-1.0F, 4.9F, -2.7F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.28F))
                .texOffs(26, 0).addBox(-4.5F, 10.6F, -2.6F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.28F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition cape_upper = body_armor.addOrReplaceChild("cape_upper", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition cube_r1 = cape_upper.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 58).addBox(-5.0F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.0F, 11.4F, 3.8F, 0.1745F, 0.0F, 0.0F));
        PartDefinition cube_r2 = cape_upper.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 51).addBox(-5.0F, -11.0F, -0.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.0F, 12.0F, 3.9F, 0.1745F, 0.0F, 0.0F));
        PartDefinition cube_r3 = cape_upper.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(58, 34).addBox(-5.0F, -2.7F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.3F, 9.5F, 3.6F, 0.1745F, 0.0F, 0.0F));
        PartDefinition cube_r4 = cape_upper.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(58, 8).addBox(-1.6F, -5.7F, -0.5F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.0F, 11.0F, 3.8F, 0.1745F, 0.0F, 0.0F));
        PartDefinition cube_r5 = cape_upper.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 58).addBox(4.0F, -6.0F, -0.5F, 1.0F, 1.6F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(-0.1F, 11.3F, 3.7F, 0.1745F, 0.0F, 0.0F));
        PartDefinition cape_middle = cape_upper.addOrReplaceChild("cape_middle", CubeListBuilder.create(), PartPose.offset(0.0F, 10.2F, 3.5F));
        PartDefinition cube_r6 = cape_middle.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(58, 26).addBox(-5.0F, 5.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.2F, 1.4F, 0.5F, 0.4363F, 0.0F, 0.0F));
        PartDefinition cube_r7 = cape_middle.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(40, 49).addBox(-4.0F, 0.7F, -0.5F, 9.0F, 8.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));
        PartDefinition capu_under = cape_middle.addOrReplaceChild("capu_under", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 0.3F));
        PartDefinition cube_r8 = capu_under.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(58, 31).addBox(3.0F, 4.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(-0.5F, 7.4F, 2.5F, 1.0734F, 0.0F, 0.0F));
        PartDefinition cube_r9 = capu_under.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(22, 50).addBox(-5.0F, 2.0F, -0.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.1F, 7.5F, 3.0F, 1.0734F, 0.0F, 0.0F));
        PartDefinition cube_r10 = capu_under.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(58, 6).addBox(-5.0F, -0.4F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.28F)), PartPose.offsetAndRotation(0.1F, 8.0F, 4.2F, 1.0734F, 0.0F, 0.0F));
        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 48).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 39).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition rightarm_armor = right_arm.addOrReplaceChild("rightarm_armor", CubeListBuilder.create().texOffs(28, 7).addBox(-8.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                .texOffs(54, 0).addBox(-8.6F, -1.0F, -2.6F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offset(5.0F, -2.0F, 0.0F));
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head_light = head.addOrReplaceChild("head_light", CubeListBuilder.create().texOffs(85, 4).addBox(-5.0F, -5.0F, -1.0F, 10.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -6.0F));
        PartDefinition head_armor = head.addOrReplaceChild("head_armor", CubeListBuilder.create().texOffs(0, 30).addBox(2.0F, -3.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(40, 39).addBox(2.0F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(20, 39).addBox(-4.0F, -3.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(0, 41).addBox(-4.0F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(0, 16).addBox(-3.4F, -7.4F, -3.8F, 7.0F, 7.0F, 7.0F, new CubeDeformation(1.0F))
                .texOffs(58, 37).addBox(-1.2F, -7.8F, -3.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(1.0F))
                .texOffs(34, 55).addBox(-1.2F, -3.3F, -3.6F, 2.0F, 3.0F, 1.0F, new CubeDeformation(1.0F))
                .texOffs(44, 58).addBox(-0.5F, -4.9F, -3.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition rightleg_armor = right_leg.addOrReplaceChild("rightleg_armor", CubeListBuilder.create().texOffs(44, 7).addBox(-1.5F, -6.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(-0.2F, 6.0F, 0.0F));
        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition leftleg_armor = left_leg.addOrReplaceChild("leftleg_armor", CubeListBuilder.create().texOffs(44, 23).addBox(0.6F, 12.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(-1.9F, -12.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void tickCape(T entity, float partialTick) {
        // ★重要: 整数(tickCount) + 小数(partialTick) = 滑らかな時間(smoothTime)
        float smoothTime = entity.tickCount + partialTick;
        // --- マントの物理演算 ---
        // 移動速度や歩行位置も補完するとさらに滑らかになります
        // (厳密な補完は複雑なので、今回は速度はそのまま、時間だけ滑らかにします)
        float speed = entity.walkAnimation.speed();
        float walkPos = entity.walkAnimation.position(); // こだわるならここもinterpolateが必要ですが、まずは時間だけで十分効果があります
        float waveSpeed = 0.05F + speed * 0.1F;
        // ★ entity.tickCount の代わりに smoothTime を使う！
        float idleUpper = (float) Math.sin(smoothTime * waveSpeed) * 0.08F;
        float idleMiddle = (float) Math.sin(smoothTime * waveSpeed - 0.5F) * 0.1F;
        float idleUnder = (float) Math.sin(smoothTime * waveSpeed - 1.0F) * 0.15F;
        float drag = speed * 1.0F;
        float flap = (float) Math.sin(walkPos * 0.6F) * speed * 0.2F;
        this.capeUpper.xRot = 0.15F + idleUpper + drag + flap;
        this.capeMiddle.xRot = idleMiddle + flap * 0.5F;
        this.capeUnder.xRot = idleUnder + flap * 0.5F;
        // --- 十字架の回転 ---
        // ★ここも smoothTime に変えることで、回転が超滑らかになります
        this.head_light.zRot = smoothTime * 0.2F;
    }
}