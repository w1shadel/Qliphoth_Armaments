package com.maxwell.qliphoth_armaments.common.entity;

import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import com.finderfeed.fdbosses.client.BossParticles;
import com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions;
import com.finderfeed.fdbosses.content.entities.chesed_boss.ChesedBossBuddy;
import com.finderfeed.fdbosses.init.BossAnims;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.nbt.AutoSerializable;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.FDLivingEntity;
import com.finderfeed.fdlib.util.ProjectileMovementPath;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.finderfeed.fdlib.util.client.particles.lightning_particle.LightningParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MinionElectricSphereEntity extends FDLivingEntity implements AutoSerializable, ChesedBossBuddy {

    private ProjectileMovementPath path;
    private float damage;
    @Nullable
    private UUID ownerUUID;
    private static final int MAX_LIFESPAN_TICKS = 200;
    @Nullable
    @Override
    public LivingEntity getKillCredit() {
        if (this.ownerUUID != null) {
            return this.level().getPlayerByUUID(this.ownerUUID);
        }
        return null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public MinionElectricSphereEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        if (this.level().isClientSide) {
            this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder((Animation) BossAnims.ELECTRIC_ORB_IDLE.get()).build());
        }
    }

    public static MinionElectricSphereEntity summon(Level level, float damage, ProjectileMovementPath path, @Nullable Player owner) {
        MinionElectricSphereEntity sphereEntity = new MinionElectricSphereEntity(ModEntities.MINION_ELECTRIC_SPHERE.get(), level);
        sphereEntity.setPos((Vec3) path.getPositions().get(0));
        sphereEntity.path = path;
        sphereEntity.damage = damage;
        if (owner != null) {
            sphereEntity.ownerUUID = owner.getUUID();
        }
        level.addFreshEntity(sphereEntity);
        return sphereEntity;
    }

    public void tick() {
        this.noPhysics = true;
        super.tick();

        if (!this.level().isClientSide) {

            if (this.path == null) {
                this.discard(); 
                return;         
            }

            if (this.tickCount > MAX_LIFESPAN_TICKS) {
                this.explode();
                this.discard();
                return;
            }

            if (this.path.isFinished()) {
                this.explode();
                this.discard();
                return;
            }

            this.path.tick(this);
            this.detectEntitiesAndExplode();

        }

        else {
            this.idleParticles();

            this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder((Animation) BossAnims.ELECTRIC_ORB_IDLE.get()).build());
        }
    }
    private void idleParticles() {
        if (this.tickCount >= 10) {
            for (int i = 0; i < 1; ++i) {
                float offs = 0.25F;
                Vec3 p1 = this.position().add((double) this.random.nextFloat() * 0.025 - 0.0125, (double) offs, (double) this.random.nextFloat() * 0.025 - 0.0125);
                Vec3 p2 = this.position().add((double) 0.0F, (double) (this.getBbHeight() - offs), (double) 0.0F);
                Vec3 sp = this.getDeltaMovement();
                this.level().addParticle(ArcLightningOptions.builder((ParticleType) BossParticles.ARC_LIGHTNING.get()).end(p2.x, p2.y, p2.z).endSpeed(sp).lifetime(2).color(1 + this.random.nextInt(40), 183 + this.random.nextInt(60), 165 + this.random.nextInt(60)).lightningSpread(0.25F).width(0.1F).segments(6).circleOffset(0.25F).build(), true, p1.x, p1.y, p1.z, sp.x, sp.y, sp.z);
            }

        }
    }

    private void detectEntitiesAndExplode() {
        if (this.level().isClientSide()) return;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(),
                (entity) -> {
                    if (entity == this) return false;
                    if (entity instanceof MinionElectricSphereEntity) return false;
                    if (entity instanceof ChesedCoreMinionEntity) return false;
                    if (this.ownerUUID != null && entity.getUUID().equals(this.ownerUUID)) return false;
                    return true;
                });
        if (!list.isEmpty()) {
            this.explode();
            this.discard();
        }
    }

    private void explode() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.5, 1.5, 1.5),
                (living) -> {
                    if (living instanceof ChesedBossBuddy) return false;
                    if (living instanceof MinionElectricSphereEntity) return false;
                    if (living instanceof ChesedCoreMinionEntity) return false;
                    if (this.ownerUUID != null && living.getUUID().equals(this.ownerUUID)) return false;
                    return true;
                });
        LivingEntity owner = this.getKillCredit();
        DamageSource source = this.level().damageSources().indirectMagic(this, owner);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), BossSounds.FAST_LIGHTNING_STRIKE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        FDLibCalls.sendParticles((ServerLevel) this.level(), BallParticleOptions.builder().size(2.0F).scalingOptions(0, 0, 2).color(150, 230, 255).build(), this.position(), 30.0F);
        for (LivingEntity entity : list) {
            ElementalReactionManager.applyElementalDamage(entity, source, this.damage, QAElements.LIGHTNING);
            entity.invulnerableTime = 0;
        }
        Level var6 = this.level();
        if (var6 instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(LightningParticleOptions.builder().color(20, 200 + this.random.nextInt(50), 255).lifetime(10).maxLightningSegments(3).randomRoll(true).physics(true).build(), this.getX(), this.getY() + (double) 0.2F, this.getZ(), 30, (double) 0.02F, (double) 0.02F, (double) 0.02F, (double) 0.05F);
        }
    }

    public boolean hurt(DamageSource src, float damage) {
        return (src.is(DamageTypes.GENERIC_KILL) || src.is(DamageTypes.FELL_OUT_OF_WORLD)) && super.hurt(src, damage);
    }

    protected void onInsideBlock(BlockState p_20005_) {
        super.onInsideBlock(p_20005_);
        if (!p_20005_.isAir()) {
            this.explode();
            this.discard();
        }
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean save(CompoundTag tag) {
        if (this.path != null) {
            CompoundTag pathTag = new CompoundTag();
            this.path.autoSave(pathTag);
            tag.put("Path", pathTag);
        }
        tag.putFloat("Damage", this.damage);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Path")) {
            this.path = new ProjectileMovementPath();
            this.path.autoLoad("Path", tag);
        }
        this.damage = tag.getFloat("Damage");
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        super.load(tag);
    }
}