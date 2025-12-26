package com.maxwell.qliphoth_armaments.common.item;

import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthDamageSource;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class SeismicImpactAxeItem extends AxeItem {

    private final QAElements element;
    private static final float EARTHQUAKE_DAMAGE_MULTIPLIER = 3.0F;
    private static final float IMPACT_DAMAGE_MULTIPLIER = 2.0F;
    private static final int SKILL_COOLDOWN = 60;
    private static final int ELEMENTAL_EFFECT_DURATION = 100;

    public SeismicImpactAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties, QAElements element) {
        super(tier, attackDamage, attackSpeed, properties);
        this.element = element;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean success = super.hurtEnemy(stack, target, attacker);
        if (success && !target.level().isClientSide) {
            ElementalReactionManager.applyState(target, this.element, ELEMENTAL_EFFECT_DURATION);
        }
        return success;
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        if (this.element == QAElements.FIRE) {
            Color fireRed = new Color(0xFF4500);
            Color darkRed = new Color(0x8B0000);
            return GradientTextUtil.createAnimatedGradient(translatedName, 200, fireRed, darkRed, fireRed);
        } else {
            Color iceBlue = new Color(0x00FFFF);
            Color darkBlue = new Color(0x00008B);
            return GradientTextUtil.createAnimatedGradient(translatedName, 200, iceBlue, darkBlue, iceBlue);
        }
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (playerAttack * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            MalkuthAttackType visualType = (this.element == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
            float damage = getScaledDamage(player, EARTHQUAKE_DAMAGE_MULTIPLIER);
            Vec3 lookDir = player.getLookAngle();
            Vec3 direction = new Vec3(lookDir.x, 0, lookDir.z).normalize();
            if (direction.length() < 0.01) direction = new Vec3(1, 0, 0);
            Vec3 spawnPos;
            if (this.element == QAElements.FIRE) {
                spawnPos = player.position().add(direction.scale(1.5));
            } else {
                spawnPos = player.position();
            }
            float range = 20.0F;
            int duration = 20;
            float arcAngle = (float) Math.PI / 2.5F;
            Vec3 dirAndLen = direction.scale(range);
            MalkuthEarthquake.summon(
                    serverLevel,
                    visualType,
                    spawnPos,
                    dirAndLen,
                    duration,
                    arcAngle,
                    0.0F
            );
            float logicDamage = damage * (this.element == QAElements.FIRE ? 1.0F : 0.8F);
            MalkuthPlayerAttackLogic.summon(
                    serverLevel,
                    player,
                    spawnPos,
                    direction,
                    this.element,
                    logicDamage,
                    false
            );
            DamageSource baseSource = player.damageSources().playerAttack(player);
            MalkuthDamageSource malkuthSource = new MalkuthDamageSource(baseSource, visualType, 100);
            Vec3 explosionCenter;
            double explosionRadius = 5.0;
            if (this.element == QAElements.FIRE) {
                explosionCenter = player.position().add(direction.scale(6.0));
            } else {
                explosionCenter = player.position();
            }
            AABB area = new AABB(explosionCenter, explosionCenter).inflate(explosionRadius);
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity target : nearbyEntities) {
                if (target == player || target.isAlliedTo(player)) continue;
                target.hurt(malkuthSource, getScaledDamage(player, IMPACT_DAMAGE_MULTIPLIER));
                ElementalReactionManager.applyState(target, this.element, ELEMENTAL_EFFECT_DURATION + 200);
                target.push(0, 1.2, 0);
            }
            PositionedScreenShakePacket.send(serverLevel,
                    FDShakeData.builder().frequency(40.0F).amplitude(3.0F).inTime(2).stayTime(5).outTime(10).build(),
                    player.position(), 32.0D);
            player.getCooldowns().addCooldown(this, SKILL_COOLDOWN);
            player.push(-direction.x * 0.5, 0.1, -direction.z * 0.5);
        }
        player.swing(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ChatFormatting color = (this.element == QAElements.FIRE) ? ChatFormatting.GOLD : ChatFormatting.AQUA;
        String elementName = (this.element == QAElements.FIRE) ? "Fire" : "Ice";
        tooltip.add(Component.literal("Element: " + elementName).withStyle(color));
        tooltip.add(Component.translatable("item.qliphoth_armaments.seismic_axe.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.qliphoth_armaments.mw_item.r_skill_1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.qliphoth_armaments.mw_item.r_skill_2").withStyle(ChatFormatting.GRAY));
    }
}