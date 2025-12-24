package com.maxwell.qliphoth_armaments.common.entity;

import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.finderfeed.fdbosses.client.particles.chesed_attack_ray.ChesedRayOptions;
import com.finderfeed.fdbosses.content.entities.chesed_boss.ChesedBossBuddy;
import com.finderfeed.fdbosses.init.BossAnims;
import com.finderfeed.fdbosses.init.BossDamageSources;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.FDMob;
import com.finderfeed.fdlib.systems.impact_frames.ImpactFrame;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.ProjectileMovementPath;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.finderfeed.fdlib.util.client.particles.lightning_particle.LightningParticleOptions;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChesedCoreMinionEntity extends FDMob implements ChesedBossBuddy {
    private int passiveAttackTimer = 0;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_ID =
            SynchedEntityData.defineId(ChesedCoreMinionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_FORMATION_SLOT =
            SynchedEntityData.defineId(ChesedCoreMinionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_AWAKENED =
            SynchedEntityData.defineId(ChesedCoreMinionEntity.class, EntityDataSerializers.BOOLEAN);

    private String currentCommand = "";
    private int crossfireTimer = 0;
    private boolean isBusy = false;
    private Vec3 lockedTargetPos = null;
    private int laserDelayTimer = 0;
    private boolean isFiringLaser = false;
    private static final int LASER_DELAY_TICKS = 33;

    public ChesedCoreMinionEntity(EntityType<? extends FDMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setAwakened(boolean awakened) {
        this.entityData.set(DATA_IS_AWAKENED, awakened);
    }

    public boolean isAwakened() {
        return this.entityData.get(DATA_IS_AWAKENED);
    }

    @Override
    public LivingEntity getKillCredit() {
        return this.getOwner();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.noPhysics = true;
        if (this.isBusy) {
            tickCrossfireSequence();
            return;
        }
        if (this.isFiringLaser) {
            tickLaserSequence();
            return;
        }
        if (!this.level().isClientSide() && this.getFormationSlot() == 0) {
            tickPassiveAutoAttack();
        }
        Player owner = getOwner();
        if (owner == null) {
            if (!this.level().isClientSide()) this.discard();
            return;
        }
        if (!this.level().isClientSide() && (!owner.isAlive() || owner.isSpectator())) {
            this.discard();
            return;
        }
        Vec3 lookVec = owner.getViewVector(1.0F);
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 targetPos = calculateFormationPosition(owner, lookVec, rightVec);
        Vec3 moveVector = targetPos.subtract(this.position());
        double speed = 0.5;
        this.setDeltaMovement(moveVector.scale(speed));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        if (!this.level().isClientSide()) {
            handleAttacks();
        }
    }

    public void receiveCommand(String command) {
        this.currentCommand = command;
    }

    private Vec3 calculateFormationPosition(Player owner, Vec3 lookVec, Vec3 rightVec) {
        Vec3 basePos = owner.position().add(0, owner.getEyeHeight() * 0.8, 0);
        int slot = this.getFormationSlot();
        switch (slot) {
            case 0:
                return basePos.subtract(lookVec.scale(2.0));
            case 1:
                return basePos.subtract(lookVec.scale(1.5)).add(rightVec.scale(1.5));
            case 2:
                return basePos.subtract(lookVec.scale(1.5)).subtract(rightVec.scale(1.5));
            case 3:
                return basePos.add(0, 1.5, 0);
            default:
                return basePos.subtract(lookVec.scale(2.0));
        }
    }

    private void handleAttacks() {
        int slot = getFormationSlot();
        Player owner = getOwner();
        if (owner == null) return;
        if (currentCommand.equals("FIRE_LASER")) {
            if (slot == 0) fireLaser();
            this.currentCommand = "";
            return;
        }
        if (!currentCommand.isEmpty()) {
            LivingEntity target = findTarget(owner);
            if (target != null) {
                if (currentCommand.equals("DROP_MONOLITH")) {
                    if (!this.isBusy) {
                        this.isBusy = true;
                        this.crossfireTimer = 1;
                        this.lockedTargetPos = target.position();
                    }
                } else {
                    switch (currentCommand) {
                        case "FIRE_CROSS_RAY":
                            if (slot == 1 || slot == 2) fireCrossRay(target);
                            break;
                    }
                }
            }
            this.currentCommand = "";
        }
        if (slot == 1 || slot == 2) {
            if (this.tickCount % 40 == 0) {
                LivingEntity target = findTarget(owner);
                if (target != null) fireEnergySphere(target);
            }
        }
    }

    private void tickPassiveAutoAttack() {
        if (this.passiveAttackTimer > 0) {
            this.passiveAttackTimer--;
            return;
        }
        Player owner = getOwner();
        if (owner == null) return;
        LivingEntity target = findTarget(owner);
        if (target != null && target.isAlive() && this.distanceToSqr(target) < 32 * 32) {
            boolean awakened = isAwakened();
            Vec3 start = getEyePosition();
            Vec3 end = target.getEyePosition();
            ChesedRayOptions weakLaser = ChesedRayOptions.builder()
                    .time(2, 5, 3)
                    .width(awakened ? 0.3F : 0.15F)
                    .color(150, 255, 255)
                    .lightningColor(200, 255, 255)
                    .end(end)
                    .build();
            FDLibCalls.sendParticles((ServerLevel) this.level(), weakLaser, start, 64.0D);
            float damage = getScaledDamage(owner, awakened ? 0.5F : 0.3F);
            ElementalReactionManager.applyElementalDamage(target, BossDamageSources.chesedAttack(this), damage, QAElements.LIGHTNING);
            this.level().playSound(null, getX(), getY(), getZ(),
                    BossSounds.CHESED_LIGHTNING_RAY.get(),
                    SoundSource.NEUTRAL, 0.3F, 2.0F);
            if (awakened) {
                List<LivingEntity> chains = this.level().getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(6.0),
                        e -> e != target && e != owner && e != this && !(e instanceof ChesedBossBuddy));
                for (LivingEntity chainTarget : chains) {
                    ElementalReactionManager.applyElementalDamage(chainTarget, BossDamageSources.chesedAttack(this), damage * 0.7F, QAElements.LIGHTNING);
                    ((ServerLevel) this.level()).sendParticles(
                            LightningParticleOptions.builder()
                                    .color(100, 255, 255).lifetime(10).quadSize(0.1F).randomRoll(true).build(),
                            chainTarget.getX(), chainTarget.getY() + chainTarget.getBbHeight() / 2, chainTarget.getZ(),
                            3, 0.2, 0.2, 0.2, 0);
                }
            }
            this.passiveAttackTimer = awakened ? 8 : 15;
            this.lookAt(EntityAnchorArgument.Anchor.EYES, end);
        }
    }

    private void fireLaser() {
        if (this.isFiringLaser) return;
        getAnimationSystem().startAnimation("ATTACK", AnimationTicker.builder(BossAnims.CHESED_ATTACK)
                .setToNullTransitionTime(0).setSpeed(0.8f).build());
        this.level().playSound(null, getX(), getY(), getZ(),
                BossSounds.CHESED_RAY_CHARGE_FAST.get(), SoundSource.NEUTRAL, 1.5F, 0.8F);
        this.isFiringLaser = true;
        this.laserDelayTimer = 0;
    }

    private void tickLaserSequence() {
        this.laserDelayTimer++;
        if (this.laserDelayTimer >= LASER_DELAY_TICKS) {
            performActualLaser();
            this.isFiringLaser = false;
            this.laserDelayTimer = 0;
        }
    }

    private void performActualLaser() {
        Player owner = getOwner();
        if (owner == null) return;
        Vec3 startPos = this.getEyePosition();
        Vec3 lookDir = owner.getLookAngle().normalize();
        double maxRange = 200.0D;
        Vec3 endPos = startPos.add(lookDir.scale(maxRange));
        ChesedRayOptions options = ChesedRayOptions.builder()
                .time(15, 25, 10).width(isAwakened() ? 2.5F : 1.5F)
                .color(150, 255, 255).lightningColor(200, 255, 255).end(endPos).build();
        FDLibCalls.sendParticles((ServerLevel) this.level(), options, startPos, 128.0D);
        this.level().playSound(null, getX(), getY(), getZ(),
                BossSounds.CHESED_FINAL_ATTACK_RAY.get(), SoundSource.NEUTRAL, 2.0F, 0.9F);
        if (isAwakened()) {
            for (int i = 1; i < 20; i++) {
                double dist = i * 8.0;
                if (dist > maxRange) break;
                Vec3 checkPos = startPos.add(lookDir.scale(dist));
                List<LivingEntity> stormTargets = this.level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(checkPos.add(-5, -5, -5), checkPos.add(5, 5, 5)),
                        e -> !(e instanceof Player || e instanceof ChesedBossBuddy));
                for (LivingEntity stormTarget : stormTargets) {
                    float damage = getScaledDamage(owner, 10.0F);
                    EntityType.LIGHTNING_BOLT.spawn((ServerLevel) this.level(), (ItemStack) null, (Player) null, stormTarget.blockPosition(), net.minecraft.world.entity.MobSpawnType.TRIGGERED, true, true);
                    ElementalReactionManager.applyElementalDamage(stormTarget, BossDamageSources.chesedAttack(this), damage, QAElements.LIGHTNING);
                }
            }
        }
        ImpactFrame baseFrame = new ImpactFrame(0.8F, 0.1F, 6, false);
        FDLibCalls.sendImpactFrames((ServerLevel) this.level(), this.position(), 128.0F, baseFrame);
        FDLibCalls.sendParticles((ServerLevel) this.level(),
                BallParticleOptions.builder().size(50.0F).scalingOptions(2, 0, 5).color(100, 230, 255).build(),
                endPos, 128.0D);
        List<Entity> hitEntities = FDHelpers.traceEntities(this.level(), startPos, endPos, 3.0,
                (entity) -> !(entity instanceof Player || entity instanceof ChesedBossBuddy));
        float damage = getScaledDamage(owner, 6.0F);
        if (isAwakened()) damage *= 1.5F;
        for (Entity entity : hitEntities) {
            if (entity instanceof LivingEntity living) {
                living.invulnerableTime = 0;
                ElementalReactionManager.applyElementalDamage(living, BossDamageSources.chesedAttack(this), damage, QAElements.LIGHTNING);
                living.push(lookDir.x, 0.2, lookDir.z);
            }
        }
        if (!this.level().isClientSide()) {
            Vec3 currentPos = startPos;
            double stepSize = 0.5;
            Vec3 stepVec = lookDir.scale(stepSize);
            int steps = (int) (maxRange / stepSize);
            for (int i = 0; i < steps; i++) {
                currentPos = currentPos.add(stepVec);
                BlockPos blockPos = new BlockPos((int) currentPos.x, (int) currentPos.y, (int) currentPos.z);
                BlockState state = this.level().getBlockState(blockPos);
                if (!state.isAir() && state.getDestroySpeed(this.level(), blockPos) >= 0) {
                    this.level().destroyBlock(blockPos, false);
                }
            }
        }
    }

    private void fireCrossRay(LivingEntity target) {
        Player owner = getOwner();
        if (owner == null) return;
        Vec3 startPos = this.getEyePosition();
        Vec3 endPos = target.getEyePosition();
        ChesedRayOptions options = ChesedRayOptions.builder()
                .time(2, 10, 3).width(0.2F).color(100, 255, 255).lightningColor(90, 180, 255).end(endPos).build();
        FDLibCalls.sendParticles((ServerLevel) this.level(), options, startPos, 64.0D);
        PositionedScreenShakePacket.send((ServerLevel) this.level(),
                FDShakeData.builder().amplitude(0.5F).outTime(5).build(),
                endPos, 64.0D);
        this.level().playSound(null, endPos.x, endPos.y, endPos.z, BossSounds.CHESED_LIGHTNING_RAY.get(), SoundSource.NEUTRAL, 0.8F, 1.2F);
        float damage = getScaledDamage(owner, 1.2F);
        ElementalReactionManager.applyElementalDamage(target, BossDamageSources.chesedAttack(this), damage, QAElements.LIGHTNING);
        if (isAwakened()) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    BossSounds.ELECTRIC_HUM.get(), SoundSource.NEUTRAL, 1.0F, 2.0F);
        }
    }

    private void tickCrossfireSequence() {
        Player owner = getOwner();
        if (owner == null || this.lockedTargetPos == null) {
            resetState();
            return;
        }
        int slot = getFormationSlot();
        Vec3 center = this.lockedTargetPos.add(0, 1.5, 0);
        if (this.crossfireTimer < 10) {
            double radius = 6.0D;
            double offsetX = 0;
            double offsetZ = 0;
            switch (slot % 4) {
                case 0:
                    offsetX = radius;
                    break;
                case 1:
                    offsetX = -radius;
                    break;
                case 2:
                    offsetZ = radius;
                    break;
                case 3:
                    offsetZ = -radius;
                    break;
            }
            Vec3 dest = new Vec3(this.lockedTargetPos.x + offsetX, this.lockedTargetPos.y + 2.0, this.lockedTargetPos.z + offsetZ);
            Vec3 current = this.position();
            Vec3 move = dest.subtract(current).scale(0.2);
            this.setPos(current.add(move));
            this.lookAt(EntityAnchorArgument.Anchor.EYES, center);
        }
        if (this.crossfireTimer == 10) {
            this.level().playSound(null, getX(), getY(), getZ(),
                    BossSounds.CHESED_RAY_CHARGE_FAST.get(), SoundSource.NEUTRAL, 1.0F, 1.5F);
            ChesedRayOptions charge = ChesedRayOptions.builder()
                    .time(5, 5, 5).width(0.2F).color(100, 255, 255).end(center).build();
            FDLibCalls.sendParticles((ServerLevel) this.level(), charge, getEyePosition(), 64.0D);
        }
        if (this.crossfireTimer >= 20 && this.crossfireTimer < 60) {
            if (this.crossfireTimer % 5 == 0) {
                Vec3 dir = center.subtract(getEyePosition()).normalize();
                Vec3 endPos = center.add(dir.scale(6.0));
                ChesedRayOptions laser = ChesedRayOptions.builder()
                        .time(2, 4, 2).width(0.6F).color(80, 220, 255).lightningColor(200, 255, 255).end(endPos).build();
                FDLibCalls.sendParticles((ServerLevel) this.level(), laser, getEyePosition(), 64.0D);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(center.add(-2, -2, -2), center.add(2, 2, 2)),
                        e -> e != owner && e != this && !(e instanceof ChesedBossBuddy));
                for (LivingEntity e : targets) {
                    float damage = getScaledDamage(owner, 0.4F);
                    ElementalReactionManager.applyElementalDamage(e, BossDamageSources.chesedAttack(this), damage, QAElements.LIGHTNING);
                    Vec3 pull = center.subtract(e.position()).normalize().scale(0.1);
                    e.push(pull.x, pull.y, pull.z);
                }
                if (this.crossfireTimer % 10 == 0) {
                    this.level().playSound(null, center.x, center.y, center.z,
                            BossSounds.CHESED_LIGHTNING_RAY.get(), SoundSource.NEUTRAL, 0.5F, 1.5F + (this.random.nextFloat() * 0.5F));
                }
            }
        }
        if (this.crossfireTimer >= 70) {
            resetState();
        } else {
            this.crossfireTimer++;
        }
    }

    private void resetState() {
        this.isBusy = false;
        this.crossfireTimer = 0;
        this.lockedTargetPos = null;
    }

    private void fireEnergySphere(LivingEntity target) {
        Player owner = getOwner();
        if (owner == null) return;
        if (target == null || !target.isAlive()) {
            target = findTarget(owner);
        }
        if (target == null) return;
        float damage = getScaledDamage(owner, 0.8F);
        Vec3 startPos = this.position().add(0, 0.5, 0);
        Vec3 endPos = target.getEyePosition();
        double distance = startPos.distanceTo(endPos);
        double ticksPerBlock = 3.0;
        int duration = (int) (distance * ticksPerBlock);
        duration = Math.max(10, Math.min(100, duration));
        ProjectileMovementPath path = new ProjectileMovementPath(duration, false)
                .addPos(startPos)
                .addPos(endPos);
        MinionElectricSphereEntity.summon(this.level(), damage, path, owner);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                BossSounds.FAST_LIGHTNING_STRIKE.get(), SoundSource.NEUTRAL, 0.5F, 1.5F);
    }

    @Nullable
    private LivingEntity findTarget(Player owner) {
        double range = 64.0D;
        if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive()) {
            if (owner.distanceToSqr(owner.getLastHurtMob()) < range * range) {
                return owner.getLastHurtMob();
            }
        }
        AABB searchBox = owner.getBoundingBox().inflate(range);
        List<Monster> nearbyEnemies = this.level().getEntitiesOfClass(
                Monster.class,
                searchBox,
                entity -> entity.isAlive() && !(entity instanceof ChesedBossBuddy)
        );
        LivingEntity bestTarget = null;
        double bestDotProduct = -1.0D;
        Vec3 eyePos = owner.getEyePosition();
        Vec3 lookVec = owner.getLookAngle().normalize();
        for (LivingEntity enemy : nearbyEnemies) {
            if (enemy.distanceToSqr(owner) > range * range) continue;
            Vec3 toEnemy = enemy.position().add(0, enemy.getBbHeight() / 2.0, 0).subtract(eyePos).normalize();
            double dot = lookVec.dot(toEnemy);
            if (dot > 0.5) {
                if (dot > bestDotProduct) {
                    bestDotProduct = dot;
                    bestTarget = enemy;
                }
            }
        }
        return bestTarget;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void push(Entity entityIn) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FDMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FLYING_SPEED, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_ID, Optional.empty());
        builder.define(DATA_FORMATION_SLOT, 0);
        builder.define(DATA_IS_AWAKENED, false);
    }

    public void setFormationSlot(int slot) {
        this.entityData.set(DATA_FORMATION_SLOT, slot);
    }

    public int getFormationSlot() {
        return this.entityData.get(DATA_FORMATION_SLOT);
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_ID, Optional.of(player.getUUID()));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_ID).orElse(null);
    }

    @Nullable
    public Player getOwner() {
        UUID uuid = getOwnerUUID();
        if (uuid == null) return null;
        return this.level().getPlayerByUUID(uuid);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID() != null) {
            compound.putUUID("Owner", this.getOwnerUUID());
        }
        compound.putInt("FormationSlot", this.getFormationSlot());
        compound.putBoolean("Awakened", this.isAwakened());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            UUID ownerUUID = compound.getUUID("Owner");
            this.entityData.set(DATA_OWNER_ID, Optional.of(ownerUUID));
        } else {
            this.entityData.set(DATA_OWNER_ID, Optional.empty());
        }
        this.setFormationSlot(compound.getInt("FormationSlot"));
        this.setAwakened(compound.getBoolean("Awakened"));
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (playerAttack * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
    }
}