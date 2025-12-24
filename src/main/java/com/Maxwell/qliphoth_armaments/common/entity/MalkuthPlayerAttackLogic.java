package com.Maxwell.qliphoth_armaments.common.entity;

import com.Maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.Maxwell.qliphoth_armaments.api.QAElements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class MalkuthPlayerAttackLogic extends Entity {

    private UUID ownerUUID;

    private QAElements attackType;
    private float damage;
    private boolean isSword;
    private int maxLifeTime;
    private Vec3 direction;

    public MalkuthPlayerAttackLogic(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void summon(Level level, Player owner, Vec3 pos, Vec3 dir, QAElements type, float damage, boolean isSword) {
        MalkuthPlayerAttackLogic logic = new MalkuthPlayerAttackLogic(EntityType.MARKER, level);
        logic.setPos(pos);
        logic.ownerUUID = owner.getUUID();
        logic.attackType = type;
        logic.damage = damage;
        logic.isSword = isSword;
        logic.direction = dir.normalize();
        logic.maxLifeTime = isSword ? 100 : 30;
        level.addFreshEntity(logic);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount > maxLifeTime) {
            this.discard();
            return;
        }
        Player owner = this.level().getPlayerByUUID(ownerUUID);
        if (owner == null) return;
        if (isSword) {
            if (this.tickCount == 90) {
                performSwordDamage(owner);
            }
        } else {
            if (this.tickCount <= 20) {
                performEarthquakeDamage(owner);
            }
        }
    }

    private void performSwordDamage(Player owner) {
        double length = 32.0;
        double width = 10.0;
        Vec3 start = this.position();
        Vec3 end = start.add(direction.scale(length));
        AABB searchBox = new AABB(start, end).inflate(width, 10.0, width);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        DamageSource source = this.level().damageSources().playerAttack(owner);
        for (LivingEntity target : targets) {
            if (target == owner) continue;
            if (!isInOrientedBox(target.position(), start, direction, length, width)) continue;
            ElementalReactionManager.applyElementalDamage(target, source, damage, this.attackType);
            target.push(0, 0.5, 0);
        }
    }

    private void performEarthquakeDamage(Player owner) {
        float maxTime = 20.0F;
        float progress = (float) this.tickCount / maxTime;
        float maxLength = 20.0F;
        float currentDistMin = (progress - 0.1F) * maxLength;
        float currentDistMax = progress * maxLength + 2.0F;
        float angle = (float) Math.PI / 2.5F;
        AABB searchBox = this.getBoundingBox().inflate(maxLength);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        DamageSource source = this.level().damageSources().playerAttack(owner);
        for (LivingEntity target : targets) {
            if (target == owner) continue;
            Vec3 toTarget = target.position().subtract(this.position());
            double dist = toTarget.length();
            if (dist < currentDistMin || dist > currentDistMax) continue;
            Vec3 toTargetDir = toTarget.normalize();
            double dot = direction.dot(toTargetDir);
            if (dot < Math.cos(angle / 2.0)) continue;
            if (target.invulnerableTime == 0) {
                ElementalReactionManager.applyElementalDamage(target, source, damage, this.attackType);
                target.push(0, 0.8, 0);
            }
        }
    }

    private boolean isInOrientedBox(Vec3 pos, Vec3 start, Vec3 dir, double length, double width) {
        Vec3 vec = pos.subtract(start);
        double forward = vec.dot(dir);
        if (forward < 0 || forward > length) return false;
        Vec3 right = dir.cross(new Vec3(0, 1, 0));
        double side = Math.abs(vec.dot(right));
        return side < width / 2.0;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
    }
}