package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.client.particles.chesed_attack_ray.ChesedRayOptions;
import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.items.WeaponCoreItem;
import com.finderfeed.fdbosses.init.BossDamageSources;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.impact_frames.ImpactFrame;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class LaserRifleItem extends SwordItem implements QAModWeapon {

    // 最大チャージ時間は長いままでOK
    private static final int MAX_CHARGE_TIME = 72000;
    // 最低チャージ時間。6秒 (120 ticks)
    private static final int MIN_CHARGE_TIME = 120;
    // クールダウン時間。10秒 (200 ticks)
    private static final int COOLDOWN_TICKS = 200;

    public LaserRifleItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    /**
     * ▼▼▼ 変更点1: アイテム名をグラデーションアニメーション化 ▼▼▼
     * ConductorRequiemItemを参考に、getNameメソッドをオーバーライドします。
     */
    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        // 色を定義 (サイバーな青 -> 輝く白 -> シアン)
        Color color1 = new Color(0, 200, 255); // Electric Blue
        Color color2 = Color.WHITE;
        Color color3 = new Color(0, 255, 255); // Cyan
        // ユーティリティクラスを呼び出してアニメーション付きComponentを生成
        return GradientTextUtil.createAnimatedGradient(translatedName, 120, color1, color2, color3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                BossSounds.CHESED_RAY_CHARGE_FAST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }
        int chargeTime = this.getUseDuration(pStack) - pTimeLeft;
        if (chargeTime < MIN_CHARGE_TIME) {
            return;
        }
        if (!pLevel.isClientSide) {
            // ▼▼▼ 変更点2: レーザー発射時にItemStackを渡すように変更 ▼▼▼
            // コアの有無で性能を変えるため、スタック情報が必要になります。
            performLaserAttack((ServerLevel) pLevel, player, pStack);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    /**
     * ▼▼▼ 変更点3: レーザーの性能を大幅に強化 ▼▼▼
     * @param stack コアの有無を判定するために追加
     */
    private void performLaserAttack(ServerLevel level, Player owner, ItemStack stack) {
        boolean hasCore = hasCore(stack);

        Vec3 startPos = owner.getEyePosition();
        Vec3 lookDir = owner.getLookAngle().normalize();
        double maxRange = 256.0D; // 射程を延長
        Vec3 endPos = startPos.add(lookDir.scale(maxRange));

        // コアの有無でレーザーの太さと色を変化させる
        float laserWidth = hasCore ? 9.0F : 6.0F;
        Color laserColor = hasCore ? new Color(255, 255, 100) : new Color(150, 255, 255); // コアありだと金色っぽく
        Color lightningColor = hasCore ? new Color(255, 255, 220) : new Color(200, 255, 255);

        ChesedRayOptions options = ChesedRayOptions.builder()
                .time(20, 30, 15)
                .width(laserWidth) // ★レーザーを極太に！
                .color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue())
                .lightningColor(lightningColor.getRed(), lightningColor.getGreen(), lightningColor.getBlue())
                .end(endPos)
                .build();
        FDLibCalls.sendParticles(level, options, startPos, 256.0D);

        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                BossSounds.CHESED_FINAL_ATTACK_RAY.get(), SoundSource.PLAYERS, 3.0F, 0.7F); // 音量を大きく、ピッチを低く

        ImpactFrame baseFrame = new ImpactFrame(1.5F, 0.2F, 10, false);
        FDLibCalls.sendImpactFrames(level, owner.position(), 256.0F, baseFrame);

        // 当たり判定もレーザーの見た目に合わせて拡大
        double traceRadius = hasCore ? 4.5D : 3.0D;
        List<Entity> hitEntities = FDHelpers.traceEntities(level, startPos, endPos, traceRadius,
                (entity) -> entity != owner && entity instanceof LivingEntity);

        // ★ダメージ倍率を大幅に引き上げ！
        float damageMultiplier = hasCore ? 500.0F : 250.0F;
        float damage = getScaledDamage(owner, damageMultiplier);

        for (Entity entity : hitEntities) {
            if (entity instanceof LivingEntity living) {
                living.invulnerableTime = 0;
                ElementalReactionManager.applyState(living, QAElements.LIGHTNING, 300); // 状態付与時間も延長
                living.hurt(BossDamageSources.chesedAttack(owner), damage);
                living.setRemainingFireTicks(100); // 燃焼効果も追加
                living.push(lookDir.x, 0.3, lookDir.z); // ノックバック強化
            }
        }

        // ブロック破壊処理 (変更なし)
        Vec3 currentPos = startPos;
        double stepSize = 0.5;
        Vec3 stepVec = lookDir.scale(stepSize);
        int steps = (int) (maxRange / stepSize);
        for (int i = 0; i < steps; i++) {
            currentPos = currentPos.add(stepVec);
            BlockPos blockPos = BlockPos.containing(currentPos);
            BlockState state = level.getBlockState(blockPos);
            if (!state.isAir() && state.getDestroySpeed(level, blockPos) >= 0 && state.getDestroySpeed(level, blockPos) < 50.0f) {
                level.destroyBlock(blockPos, true, owner);
            }
        }
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (playerAttack * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return MAX_CHARGE_TIME;
    }

    private boolean hasCore(ItemStack stack) {
        return WeaponCoreItem.getItemCore(stack) == ItemCoreDataComponent.CoreType.LIGHTNING;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (hasCore(stack)) {
            tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.lore_fuse").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        }
        tooltip.add(Component.empty());
        if (Screen.hasShiftDown()) {
            addRightClickSkill(tooltip,
                    "item.qliphoth_armaments.seraphim_railgun.r_skill_1",
                    "item.qliphoth_armaments.seraphim_railgun.r_skill_2");
            if (!hasCore(stack)) {
                addFuseHint(tooltip, "item.fdbosses.lightning_core");
            }
        } else {
            addPressShiftHint(tooltip);
        }
    }
}