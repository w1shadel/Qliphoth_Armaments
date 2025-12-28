package com.maxwell.qliphoth_armaments.common.entity;

import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class MalkuthRampageSwordEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(MalkuthRampageSwordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TYPE_ORDINAL = SynchedEntityData.defineId(MalkuthRampageSwordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWORD_INDEX = SynchedEntityData.defineId(MalkuthRampageSwordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LAUNCHED = SynchedEntityData.defineId(MalkuthRampageSwordEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUUID;
    private int lifeTime = 0;

    public MalkuthRampageSwordEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public static void summon(Level level, Player owner, int index) {
        MalkuthRampageSwordEntity sword = new MalkuthRampageSwordEntity(ModEntities.MALKUTH_RAMPAGE_SWORD.get(), level);
        sword.setOwnerId(owner.getId());
        sword.ownerUUID = owner.getUUID();
        sword.setSwordIndex(index);
        QAElements type = (index < 3) ? QAElements.FIRE : QAElements.ICE;
        sword.setAttackType(type);
        sword.setPos(owner.getX(), owner.getY() + 2.0, owner.getZ());
        level.addFreshEntity(sword);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(ATTACK_TYPE_ORDINAL, 0);
        this.entityData.define(SWORD_INDEX, 0);
        this.entityData.define(IS_LAUNCHED, false);
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();
        if (!level().isClientSide) {
            if (owner == null || !owner.isAlive()) {
                discard();
                return;
            }
        }
        if (isLaunched()) {
            tickLaunched();
        } else {
            if (owner != null) {
                this.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
                this.setRot(owner.getYRot(), owner.getXRot());
            }
        }
    }

    private void tickLaunched() {
        lifeTime++;
        if (lifeTime > 100) {
            discard();
            return;
        }
        Vec3 motion = getDeltaMovement();
        Vec3 nextPos = position().add(motion);
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            onHit(hitResult);
        }
        this.setPos(nextPos);
        if (level().isClientSide) {
            if (getAttackType() == QAElements.FIRE) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
            } else {
                level().addParticle(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 0, 0, 0);
            }
        }
    }

    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof Player && entity.getUUID().equals(ownerUUID)) return false;
        if (entity instanceof MalkuthRampageSwordEntity) return false;
        return !entity.isSpectator() && entity.isAlive() && entity.isPickable();
    }

    private void onHit(HitResult result) {
        if (level().isClientSide) return;
        QAElements element = getAttackType();
        float damage = 12.0F;
        if (result instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            Player owner = getOwner();
            DamageSource source = level().damageSources().playerAttack(owner);
            if (target instanceof LivingEntity livingTarget) {
                ElementalReactionManager.applyElementalDamage(livingTarget, source, damage, element);
                if (element == QAElements.FIRE) {
                    livingTarget.setSecondsOnFire(5);
                } else {
                }
            } else {
                target.hurt(source, damage);
            }
        }
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        this.discard();
    }

    public void launch(Entity target, Vec3 targetPos) {
        if (isLaunched()) return;
        setLaunched(true);
        Player owner = getOwner();
        Vec3 start = this.position();
        if (owner != null) {
            start = owner.position().add(0, 1.8, 0);
            this.setPos(start);
        }
        Vec3 dir = targetPos.subtract(start).normalize();
        this.setDeltaMovement(dir.scale(3.0));
        double hDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.setYRot((float) (Mth.atan2(-dir.x, dir.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(dir.y, hDist) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 1.0F);
    }

    public Player getOwner() {
        int id = entityData.get(OWNER_ID);
        Entity e = level().getEntity(id);
        return e instanceof Player ? (Player) e : null;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerId(int id) {
        this.entityData.set(OWNER_ID, id);
    }

    public int getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public void setAttackType(QAElements type) {
        this.entityData.set(ATTACK_TYPE_ORDINAL, type == QAElements.FIRE ? 0 : 1);
    }

    public QAElements getAttackType() {
        return this.entityData.get(ATTACK_TYPE_ORDINAL) == 0 ? QAElements.FIRE : QAElements.ICE;
    }

    public MalkuthAttackType getBossAttackType() {
        return getAttackType() == QAElements.FIRE ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
    }

    public void setSwordIndex(int index) {
        this.entityData.set(SWORD_INDEX, index);
    }

    public int getSwordIndex() {
        return this.entityData.get(SWORD_INDEX);
    }

    public void setLaunched(boolean launched) {
        this.entityData.set(IS_LAUNCHED, launched);
    }

    public boolean isLaunched() {
        return this.entityData.get(IS_LAUNCHED);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setSwordIndex(tag.getInt("Index"));
        this.setLaunched(tag.getBoolean("Launched"));
        this.setAttackType(tag.getBoolean("IsFire") ? QAElements.FIRE : QAElements.ICE);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Index", getSwordIndex());
        tag.putBoolean("Launched", isLaunched());
        tag.putBoolean("IsFire", getAttackType() == QAElements.FIRE);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}