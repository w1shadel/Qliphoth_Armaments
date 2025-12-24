package com.maxwell.qliphoth_armaments.api;

import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.api.capabilities.IElementalState;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthEntity;
import com.finderfeed.fdbosses.init.BossEffects;
import com.finderfeed.fdbosses.init.BossEntities;
import com.maxwell.qliphoth_armaments.common.network.PacketSyncElementalState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

public class ElementalReactionManager {
    public static boolean applyElementalDamage(LivingEntity target, DamageSource source, float amount, QAElements type) {
        if (target.level().isClientSide) return false;
        QAElements currentStatus = getCurrentState(target);
        boolean isCombo = false;
        float finalDamage = amount;
        if (currentStatus != null) {
            if (currentStatus == QAElements.ICE && type == QAElements.FIRE) {
                isCombo = true;
                finalDamage *= 2.0F;
                triggerMeltdownEffect(target);
            } else if (currentStatus == QAElements.FIRE && type == QAElements.ICE) {
                isCombo = true;
                finalDamage *= 1.5F;
                triggerShatterEffect(target);
            } else if (currentStatus == QAElements.FIRE && type == QAElements.LIGHTNING) {
                isCombo = true;
                finalDamage *= 1.2F;
                triggerOverloadEffect(target, source);
            } else if (currentStatus == QAElements.LIGHTNING && type == QAElements.FIRE) {
                isCombo = true;
                finalDamage *= 1.2F;
                triggerOverloadEffect(target, source);
            } else if (currentStatus == QAElements.ICE && type == QAElements.LIGHTNING) {
                isCombo = true;
                triggerSuperconductEffect(target);
                clearState(target);
            } else if (currentStatus == QAElements.LIGHTNING && type == QAElements.ICE) {
                isCombo = true;
                triggerSuperconductEffect(target);
                clearState(target);
            }
        }
        boolean hit = target.hurt(source, finalDamage);
        if (hit) {
            if (isCombo) {
                clearState(target);
            } else {
                applyState(target, type, 100);
            }
        }
        return hit;
    }

    public static QAElements getCurrentState(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        IElementalState state = entity.getData(CapabilityHandler.ELEMENTAL_STATE.get());
        return state.getElement(entity.level().getGameTime());
    }

    public static void applyState(LivingEntity entity, QAElements type, int duration) {

        IElementalState state = entity.getData(CapabilityHandler.ELEMENTAL_STATE.get());
        state.setElement(type, duration, entity.level().getGameTime());
        if (type == QAElements.FIRE) {
            entity.setRemainingFireTicks(20);
        }
        if (type == QAElements.ICE) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
        }
        if (type == QAElements.LIGHTNING) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
        }


        if (!entity.level().isClientSide()) {
            PacketSyncElementalState packet = new PacketSyncElementalState(entity.getId(), type, duration);

            PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
        }
    }

    public static void clearState(LivingEntity entity) {

        IElementalState state = entity.getData(CapabilityHandler.ELEMENTAL_STATE.get());
        state.clearElement();
        entity.clearFire();
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (!entity.level().isClientSide()) {
            PacketSyncElementalState packet = new PacketSyncElementalState(entity.getId(), null, 0);
            PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
        }
    }


    public static void triggerMeltdownEffect(LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.5F, 1.0F);
        level.sendParticles(ParticleTypes.CLOUD,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
        spawnVisualEntity(BossEntities.EARTH_SHATTER.get(), level, target.position().add(0, 0.1, 0));
    }

    public static void triggerShatterEffect(LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.7F);
        Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(MalkuthAttackType.ICE);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                15, 0.5, 0.5, 0.5, 0.1);
        spawnVisualEntity(BossEntities.EARTH_SHATTER.get(), level, target.position());
    }

    public static void triggerOverloadEffect(LivingEntity target, DamageSource source) {
        ServerLevel level = (ServerLevel) target.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.2F);
        spawnVisualEntity(BossEntities.RADIAL_EARTHQUAKE.get(), level, target.position());
        spawnVisualEntity(BossEntities.CHESED_ELECTRIC_SPHERE.get(), level, target.position().add(0, target.getBbHeight() / 2, 0));
        level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0)).forEach(entity -> {
            if (entity != target && entity.isAlliedTo(source.getEntity()) == false) {
                entity.hurt(source, 5.0F);
                double dx = entity.getX() - target.getX();
                double dz = entity.getZ() - target.getZ();
                entity.knockback(1.5F, dx, dz);
                entity.addEffect(new MobEffectInstance(BossEffects.SHOCKED, 60, 0));
            }
        });
    }

    public static void triggerSuperconductEffect(LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.5F, 2.0F);
        level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                30, 0.5, 0.5, 0.5, 0.2);
        spawnVisualEntity(BossEntities.CHESED_ELECTRIC_SPHERE.get(), level, target.position().add(0, target.getBbHeight() / 2, 0));
        target.addEffect(new MobEffectInstance(BossEffects.SHOCKED, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
    }

    private static void spawnVisualEntity(EntityType<?> type, Level level, net.minecraft.world.phys.Vec3 pos) {
        Entity entity = type.create(level);
        if (entity != null) {
            entity.setPos(pos);
            level.addFreshEntity(entity);
        }
    }
}