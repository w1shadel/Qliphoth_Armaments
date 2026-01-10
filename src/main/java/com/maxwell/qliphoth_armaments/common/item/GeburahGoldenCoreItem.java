package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdlib.systems.impact_frames.ImpactFrame;
import com.finderfeed.fdlib.systems.shake.DefaultShakePacket;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.entity.PlayerChainEntity;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeburahGoldenCoreItem extends SwordItem implements QAModWeapon {
    private final Random random = new Random();
    private final float baseChainDamage;

    public GeburahGoldenCoreItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, 0, attackSpeed)));
        this.baseChainDamage = attackDamage;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            spawnPortalChain(player, target);
        }
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            LivingEntity target = getTargetLookingAt(player, 15.0);
            if (target != null) {
                spawnPortalChain(player, target);
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    private void spawnPortalChain(Player player, LivingEntity target) {
        Level level = player.level();
        double offsetX = (random.nextDouble() - 0.5) * 5.0;
        double offsetY = (random.nextDouble() * 2.0) + 1.0;
        double offsetZ = (random.nextDouble() - 0.5) * 5.0;
        Vec3 portalPos = player.position().add(offsetX, offsetY, offsetZ);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, portalPos.x, portalPos.y, portalPos.z, 10, 0.2, 0.2, 0.2, 0.05);
        }
        level.playSound(null, portalPos.x, portalPos.y, portalPos.z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 0.5F);
        float damage = calculateChainDamage(player, 1.5f);
        PlayerChainEntity.summonAttack(level, player, QAElements.FIRE, target, portalPos, damage, false);
    }

    private float calculateChainDamage(Player owner, float multiplier) {
        double playerBaseDamage = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) ((this.baseChainDamage + playerBaseDamage) * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof ServerPlayer player)) return;
        int duration = this.getUseDuration(stack, livingEntity) - timeCharged;
        if (duration < 10) return;
        int portalCount = Math.min(duration / 5, 8);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(20.0),
                e -> e != player && e.isAlive() && !e.isAlliedTo(player) && e.isPickable());
        if (targets.isEmpty()) return;
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        ImpactFrame frame = new ImpactFrame(0.5f, 0.1f, 2, false);
        PacketDistributor.sendToPlayer(player, new com.finderfeed.fdlib.systems.impact_frames.ImpactFramesPacket(List.of(frame, new ImpactFrame(frame).setInverted(true))));
        PacketDistributor.sendToPlayer(player, new DefaultShakePacket(FDShakeData.builder().amplitude(0.5f).stayTime(5).outTime(20).build()));
        level.playSound(null, player.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.5f);
        int numTargets = targets.size();
        for (int i = 0; i < portalCount; i++) {
            double angle = Math.toRadians(i * (360.0 / 8.0));
            double radius = 3.5;
            Vec3 portalOffset = new Vec3(Math.cos(angle) * radius, 1.8, Math.sin(angle) * radius);
            Vec3 portalPos = player.position().add(portalOffset);
            LivingEntity target = targets.get(i % numTargets);
            float damage = calculateChainDamage(player, 2.0f);
            PlayerChainEntity.summonAttack(level, player, QAElements.FIRE, target, portalPos, damage, true);
        }
        player.getCooldowns().addCooldown(this, 500);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color sephirotGold = new Color(0xFFD700);
        Color bloodRed = new Color(0xBB0000);
        return GradientTextUtil.createAnimatedGradient(translatedName, 300, sephirotGold, bloodRed, sephirotGold);
    }

    private LivingEntity getTargetLookingAt(Player player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && e.isPickable() && !e.isSpectator());
        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : entities) {
            Vec3 targetMidPos = e.position().add(0, e.getBbHeight() / 2.0, 0);
            Vec3 toTargetVec = targetMidPos.subtract(eyePos);
            double distance = toTargetVec.length();
            if (distance > range) continue;
            double dot = lookVec.dot(toTargetVec.normalize());
            if (dot > 0.7) {
                double angleError = Math.acos(dot);
                double score = angleError * 10.0 + (distance * 0.2);
                if (score < bestScore) {
                    bestScore = score;
                    bestTarget = e;
                }
            }
        }
        if (bestTarget == null) {
            bestTarget = entities.stream()
                    .filter(e -> e.distanceTo(player) < 3.0)
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .orElse(null);
        }
        return bestTarget;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah_golden_core.lore").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.empty());
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            addPassiveSkill(tooltipComponents,
                    "tooltip.qliphoth_armaments.geburah_golden_core.passive.authority",
                    "tooltip.qliphoth_armaments.geburah_golden_core.passive.authority.desc");
            addSkillComponent(tooltipComponents,
                    "tooltip.qliphoth_armaments.geburah_golden_core.active.portal_chain", ChatFormatting.GOLD,
                    "tooltip.qliphoth_armaments.geburah_golden_core.active.portal_chain.desc");
            addLongRightClickSkill(tooltipComponents,
                    "tooltip.qliphoth_armaments.geburah_golden_core.active.ring_of_sins",
                    "tooltip.qliphoth_armaments.geburah_golden_core.active.ring_of_sins.desc1",
                    "tooltip.qliphoth_armaments.geburah_golden_core.active.ring_of_sins.desc2");
        } else {
            addPressShiftHint(tooltipComponents);
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}