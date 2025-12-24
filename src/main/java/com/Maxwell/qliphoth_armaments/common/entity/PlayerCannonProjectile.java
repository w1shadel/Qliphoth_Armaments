package com.Maxwell.qliphoth_armaments.common.entity;

import com.Maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.Maxwell.qliphoth_armaments.api.QAElements;
import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthDamageSource;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_cannon.MalkuthCannonProjectile;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdbosses.init.BossEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerCannonProjectile extends MalkuthCannonProjectile {

    public PlayerCannonProjectile(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public static PlayerCannonProjectile summonForPlayer(Level level, Player owner, Vec3 pos, Vec3 speed, QAElements type, float damage, boolean isUltimate) {
        PlayerCannonProjectile projectile = new PlayerCannonProjectile((EntityType) BossEntities.MALKUTH_CANNON_PROJECTILE.get(), level);
        projectile.setPos(pos);
        projectile.setDeltaMovement(speed);
        if (type == QAElements.FIRE) {
            projectile.setMalkuthAttackType(MalkuthAttackType.FIRE);
        } else {
            projectile.setMalkuthAttackType(MalkuthAttackType.ICE);
        }
        projectile.setOwner(owner);
        projectile.getPersistentData().putFloat("PlayerDamage", damage);
        projectile.getPersistentData().putBoolean("IsUltimate", isUltimate);
        projectile.getPersistentData().putString("QA_Element", type.name());
        level.addFreshEntity(projectile);
        return projectile;
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity entity) {
        if (entity.is(this.getOwner())) return false;
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult res) {
        if (res.getEntity() instanceof MalkuthCannonProjectile) return;
        if (!this.level().isClientSide) this.explodeForPlayer(res.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult res) {
        if (!this.level().isClientSide) this.explodeForPlayer(res.getLocation());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > 200) {
            this.explodeForPlayer(this.position());
        }
    }

    private void explodeForPlayer(Vec3 pos) {
        if (this.level().isClientSide) return;
        ServerLevel level = (ServerLevel) this.level();
        QAElements elementType = QAElements.valueOf(this.getPersistentData().getString("QA_Element"));
        MalkuthAttackType visualType = this.getMalkuthAttackType();
        boolean isUltimate = this.getPersistentData().getBoolean("IsUltimate");
        float damage = this.getPersistentData().getFloat("PlayerDamage");
        BossUtil.malkuthFireballExplosionParticles(level, pos, visualType);
        DamageSource source;
        if (this.getOwner() instanceof Player player) {
            source = level.damageSources().indirectMagic(this, player);
        } else {
            source = level.damageSources().magic();
        }
        MalkuthDamageSource malkuthSource = new MalkuthDamageSource(source, visualType, 100);
        if (isUltimate) {
            Vec3 motion = this.getDeltaMovement();
            Vec3 direction = new Vec3(motion.x, 0, motion.z).normalize();
            if (direction.length() < 0.01) direction = new Vec3(1, 0, 0);
            float range = 20.0F;
            int duration = 20;
            float arcAngle = (float) Math.PI / 2.5F;
            Vec3 dirAndLen = direction.scale(range);
            if (visualType.isFire()) {
                MalkuthEarthquake.summon(
                        level,
                        MalkuthAttackType.FIRE,
                        pos,
                        dirAndLen,
                        duration,
                        arcAngle,
                        0.0F
                );
                MalkuthPlayerAttackLogic.summon(
                        level,
                        (Player) this.getOwner(),
                        pos,
                        direction,
                        elementType,
                        damage * (elementType == QAElements.FIRE ? 3.0F : 1.5F),
                        false
                );

            } else {
                MalkuthEarthquake.summon(level, MalkuthAttackType.ICE, pos, dirAndLen, duration, arcAngle, 0.0F);
                MalkuthPlayerAttackLogic.summon(level, (Player) this.getOwner(), pos, direction, QAElements.ICE, damage * 1.5F, false);
            }
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
                if (target == this.getOwner()) continue;
                target.hurt(malkuthSource, damage);
                target.push(0, 1.2, 0);
            }

        } else {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0).move(pos.subtract(this.position())))) {
                if (target == this.getOwner()) continue;
                ElementalReactionManager.applyElementalDamage(target, source, damage, elementType);
            }
        }
        this.discard();
    }
}