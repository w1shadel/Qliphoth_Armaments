package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.entity.PlayerChainEntity;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TheSovereigntyItem extends SwordItem implements QAModWeapon {

    private static final String TAG_MODE = "CurrentElement";
    private static final String TAG_SHOCKWAVE_HIT = "SovereigntyShockwave";

    public TheSovereigntyItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof TheSovereigntyItem swordItem) {
            if (player.getAttackStrengthScale(0.5F) < 1.0F) {
                event.setCanceled(true);
                return;
            }
            swordItem.performShockwaveAttack(stack, player);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(TAG_SHOCKWAVE_HIT)) {
            event.getEntity().getPersistentData().remove(TAG_SHOCKWAVE_HIT);
            return;
        }
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof TheSovereigntyItem) {
                event.setAmount(0.01F);
            }
        }
    }

    private void performShockwaveAttack(ItemStack stack, Player player) {
        ServerLevel level = (ServerLevel) player.level();
        QAElements currentElement = getElementFromStack(stack);
        MalkuthAttackType visualType = (currentElement == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        double playerAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = 15.0F + (float) playerAttackDamage;
        Vec3 lookDir = player.getLookAngle().multiply(1.2, 0, 1.2).normalize();
        Vec3 shockwaveOrigin = player.position().add(lookDir.scale(2.0));
        double range = 15.0;
        double angle = Math.PI / 2.5;
        double minDot = Math.cos(angle / 2.0);
        AABB searchBox = player.getBoundingBox().inflate(range + 2.0);
        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity targetInRange : potentialTargets) {
            if (targetInRange == player || player.isAlliedTo(targetInRange)) {
                continue;
            }
            Vec3 toTarget = targetInRange.position().subtract(shockwaveOrigin);
            if (toTarget.lengthSqr() > range * range) {
                continue;
            }
            Vec3 toTargetHorizontal = toTarget.multiply(1, 0, 1).normalize();
            if (toTargetHorizontal.lengthSqr() == 0) continue;
            double dot = lookDir.dot(toTargetHorizontal);
            if (dot < minDot) {
                continue;
            }
            targetInRange.getPersistentData().putBoolean(TAG_SHOCKWAVE_HIT, true);
            ElementalReactionManager.applyState(targetInRange, currentElement, 100);
            targetInRange.hurt(player.damageSources().playerAttack(player), finalDamage);
        }
        Vec3 dir = player.getLookAngle().multiply(1, 0, 1).normalize();
        if (dir.lengthSqr() < 0.01) dir = player.getForward().multiply(1, 0, 1).normalize();
        Vec3 startPos = player.position().add(dir.scale(1.5));
        Vec3 visualEnd = dir.scale(12.0);
        summonStableMalkuthEarthquake(level, visualType, startPos, visualEnd, 15, (float) Math.PI / 4.0F, 0.0F);
        float shakeAmp = 3.0F;
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().amplitude(shakeAmp).outTime(10).build(),
                player.position(), 32.0D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_SWORD_EARTH_IMPACT.get(), SoundSource.PLAYERS, 1.5F, 0.8F);
        toggleMode(stack, player);
        player.getCooldowns().addCooldown(this, 15);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color fireRed = new Color(0xFF4500);
        Color royalGold = new Color(0xFFD700);
        Color iceBlue = new Color(0x00FFFF);
        return GradientTextUtil.createAnimatedGradient(translatedName, 200, fireRed, royalGold, iceBlue);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && isSelected && entity instanceof Player player) {
            if (level.getGameTime() % 20 == 0) {
                QAElements currentElement = getElementFromStack(stack);
                double radius = 6.0D;
                AABB area = player.getBoundingBox().inflate(radius, 2.0D, radius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && !player.isAlliedTo(e) && e instanceof Monster);
                for (LivingEntity target : targets) {
                    if (currentElement == QAElements.FIRE) {
                        target.setSecondsOnFire(3);
                    } else {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }
            QAElements currentElement = getElementFromStack(stack);
            double reach = 20.0D;
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 traceEnd = eyePos.add(lookVec.scale(reach));
            AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0D);
            EntityHitResult result = ProjectileUtil.getEntityHitResult(player, eyePos, traceEnd, searchBox, e -> e instanceof LivingEntity && !e.isSpectator(), reach * reach);
            if (result != null && result.getEntity() instanceof LivingEntity target) {
                PlayerChainEntity.summon(level, player, currentElement, target, 10, 5);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent) BossSounds.MALKUTH_CHAIN_PULL.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                player.getCooldowns().addCooldown(this, 30);
                return InteractionResultHolder.success(stack);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 1.0F, 0.5F);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private void summonStableMalkuthEarthquake(ServerLevel level, MalkuthAttackType type, Vec3 start, Vec3 direction, int lifetime, float arcAngle, float damage) {
        try {
            MalkuthEarthquake.summon(level, type, start, direction, lifetime, arcAngle, damage);
        } catch (Exception e) {
        }
    }

    private void toggleMode(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentMode = tag.getInt(TAG_MODE);
        int newMode = (currentMode == 0) ? 1 : 0;
        tag.putInt(TAG_MODE, newMode);
        float pitch = (newMode == 0) ? 1.0F : 1.2F;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent) BossSounds.MALKUTH_HIT.get(), SoundSource.PLAYERS, 0.5F, pitch);
    }

    private QAElements getElementFromStack(ItemStack stack) {
        int mode = stack.getOrCreateTag().getInt(TAG_MODE);
        return (mode == 0) ? QAElements.FIRE : QAElements.ICE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        QAElements element = getElementFromStack(stack);
        Component elementText = (element == QAElements.FIRE) ? Component.literal("FIRE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD) : Component.literal("ICE").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
        tooltip.add(Component.literal("Current Authority: ").append(elementText));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.qliphoth_armaments.the_sovereignty.loar").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.qliphoth_armaments.the_sovereignty.passive").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.qliphoth_armaments.the_sovereignty.r_skill_1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.qliphoth_armaments.the_sovereignty.r_skill_2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.empty());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        QAElements element = getElementFromStack(stack);
        return (element == QAElements.FIRE) ? 0xFF4500 : 0x00FFFF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 13;
    }
}