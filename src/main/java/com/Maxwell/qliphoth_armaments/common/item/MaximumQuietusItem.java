package com.Maxwell.qliphoth_armaments.common.item;

import com.Maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.Maxwell.qliphoth_armaments.api.QAElements;
import com.Maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.Maxwell.qliphoth_armaments.common.entity.PlayerCannonProjectile;
import com.Maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthEntity;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdbosses.content.items.WeaponCoreItem;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class MaximumQuietusItem extends SwordItem implements QAModWeapon {

    private static final String TAG_MODE = "AttackMode";
    private static final int CHARGE_LV2 = 20;

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color fireRed = new Color(0xFF4500);
        Color magicPurple = new Color(0x9400D3);
        Color iceBlue = new Color(0x00FFFF);
        return GradientTextUtil.createAnimatedGradient(translatedName, 200, fireRed, magicPurple, iceBlue);
    }

    public MaximumQuietusItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    private boolean hasCore(ItemStack stack) {
        return WeaponCoreItem.getItemCore(stack) == ItemCoreDataComponent.CoreType.FIRE_AND_ICE;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && attacker instanceof Player player) {
            QAElements currentElement = getElementFromStack(stack);
            // 状態異常の付与 (ここは変更なし)
            ElementalReactionManager.applyState(target, currentElement, 100);
            if (hasCore(stack)) {
                if (!player.getCooldowns().isOnCooldown(this)) {
                    ServerLevel level = (ServerLevel) target.level();
                    // 視覚エフェクトの種類決定
                    MalkuthAttackType visualType = (currentElement == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
                    // 向きの計算
                    Vec3 dir = player.getLookAngle().multiply(1, 0, 1).normalize();
                    if (dir.lengthSqr() < 0.01) dir = player.getForward().multiply(1, 0, 1).normalize();
                    // ★★★ 修正ポイント: 発生地点をプレイヤーの少し前にずらす ★★★
                    // dir.scale(1.5) することで、プレイヤーの約1.5ブロック前から攻撃が発生するようにします
                    // これにより、プレイヤー自身が巻き込まれるのを防ぎます
                    Vec3 startPos = player.position().add(dir.scale(1.5));
                    Vec3 dirAndLen = dir.scale(15.0);
                    // ずらした startPos を使用して召喚
                    MalkuthEarthquake.summon(level, visualType, startPos, dirAndLen, 20, (float) Math.PI / 3.0F, 0.0F);
                    // 攻撃ロジックも見た目に合わせて同じ場所から発生させる
                    MalkuthPlayerAttackLogic.summon(level, player, startPos, dir, currentElement, 30.0F, false);
                    PositionedScreenShakePacket.send(level,
                            FDShakeData.builder().amplitude(2.0F).outTime(10).build(),
                            target.position(), 24.0D);
                    player.getCooldowns().addCooldown(this, 10);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isAwakened = hasCore(stack);
        if (isAwakened && player.isShiftKeyDown()) {
            if (!level.isClientSide) toggleMode(stack, player);
            player.playSound(BossSounds.BUTTON_CLICK.get(), 1.0F, 2.0F);
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResultHolder.success(stack);
        }
        if (!isAwakened && !player.isCrouching()) {
            if (!level.isClientSide) toggleMode(stack, player);
            player.playSound(BossSounds.BUTTON_CLICK.get(), 1.0F, 2.0F);
            return InteractionResultHolder.success(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player)) return;
        boolean isAwakened = hasCore(stack);
        int usedTicks = this.getUseDuration(stack) - count;
        if (isAwakened) {
            return;
        }
        if (player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 2, false, false, false));
        } else {
            if (usedTicks > 5) {
                player.stopUsingItem();
                return;
            }
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        int usedTicks = this.getUseDuration(stack) - timeLeft;
        boolean isAwakened = hasCore(stack);
        if (!level.isClientSide) {
            QAElements elementType = getElementFromStack(stack);
            if (isAwakened) {
                shootUltimate((ServerLevel) level, player, elementType);
                player.getCooldowns().addCooldown(this, 40);

            } else {
                if (usedTicks >= CHARGE_LV2) {
                    shootProjectile((ServerLevel) level, player, elementType, 2.0F, 30.0F, false);
                    player.getCooldowns().addCooldown(this, 30);
                } else {
                    shootProjectile((ServerLevel) level, player, elementType, 1.0F, 15.0F, false);
                    player.getCooldowns().addCooldown(this, 15);
                }
            }
        }
    }

    private void shootProjectile(ServerLevel level, Player player, QAElements type, float speedMult, float damage, boolean isAwakened) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.5));
        Vec3 velocity = look.scale(3.5 * speedMult);
        MalkuthAttackType visualType = (type == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        BossUtil.malkuthCannonShoot(level, visualType, spawnPos, look, 50.0);
        PlayerCannonProjectile.summonForPlayer(level, player, spawnPos, velocity, type, damage, false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_CANNON_SHOOT.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().amplitude(1.0F * speedMult).outTime(5).build(),
                player.position(), 16.0D);
        if (!isAwakened) player.push(-look.x * 0.5, 0.1, -look.z * 0.5);
    }

    private void shootUltimate(ServerLevel level, Player player, QAElements type) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.5));
        Vec3 velocity = look.scale(4.5);
        MalkuthAttackType visualType = (type == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        BossUtil.malkuthCannonShoot(level, visualType, spawnPos, look, 150.0);
        spawnPlayerChargeParticles(level, player, visualType);
        PlayerCannonProjectile.summonForPlayer(level, player, spawnPos, velocity, type, 50.0F, true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_VOLCANO_ERRUPTION.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().frequency(40.0F).amplitude(5.0F).inTime(2).stayTime(5).outTime(10).build(),
                player.position(), 64.0D);
        player.push(-look.x * 1.5, 0.3, -look.z * 1.5);
    }

    private void spawnPlayerChargeParticles(ServerLevel level, Player player, MalkuthAttackType type) {
        Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(type);
        BallParticleOptions options = BallParticleOptions.builder()
                .color(col.x, col.y, col.z).scalingOptions(0, 0, 15).size(0.25F).brightness(3).friction(0.8F).build();
        for (int i = 0; i < 20; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double offsetX = Math.cos(angle) * 1.5;
            double offsetZ = Math.sin(angle) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            level.sendParticles(options,
                    player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private void toggleMode(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentMode = tag.getInt(TAG_MODE);
        int newMode = (currentMode == 0) ? 1 : 0;
        tag.putInt(TAG_MODE, newMode);
        Component modeName = (newMode == 0)
                ? Component.literal("Fire").withStyle(ChatFormatting.GOLD)
                : Component.literal("Ice").withStyle(ChatFormatting.AQUA);
        player.displayClientMessage(Component.translatable("Mode Switched: %s", modeName), true);
    }

    private QAElements getElementFromStack(ItemStack stack) {
        int mode = stack.getOrCreateTag().getInt(TAG_MODE);
        return (mode == 0) ? QAElements.FIRE : QAElements.ICE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (hasCore(stack)) {
            tooltip.add(Component.translatable("item.qliphoth_armaments.maximum_quietus.fuse.lore"));
        } else {
            tooltip.add(Component.translatable("item.qliphoth_armaments.maximum_quietus.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        tooltip.add(Component.empty());
        if (Screen.hasShiftDown()) {
            addPassiveSkill(tooltip,
                    "item.qliphoth_armaments.maximum_quietus.passive_1",
                    "item.qliphoth_armaments.maximum_quietus.passive_2");
            if (hasCore(stack)) {
                addRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse1",
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse2");
                addShiftRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse3",
                        "item.qliphoth_armaments.maximum_quietus.r_skill_2");
            } else {
                addRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill_1",
                        "item.qliphoth_armaments.maximum_quietus.r_skill_2");
                addShiftRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.sr_skill_1",
                        "item.qliphoth_armaments.maximum_quietus.sr_skill_2");
            }
            if (!hasCore(stack)) {
                addFuseHint(tooltip, "item.fdbosses.fire_and_ice_core");
            }
        } else {
            addPressShiftHint(tooltip);
        }
    }

}