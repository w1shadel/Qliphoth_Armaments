package com.maxwell.qliphoth_armaments.common.entity;

import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class PlayerChainEntity extends Entity {

    private static final EntityDataAccessor<Integer> ATTACK_TYPE_ORDINAL = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private UUID ownerUUID;
    private UUID targetUUID;
    private int pullTime;
    private int catchTime;
    private Vec3 chainPullToPos;
    private Vec3 startingPos;

    public PlayerChainEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void summon(Level level, Player owner, QAElements type, LivingEntity target, int pullTime, int catchTime) {
        PlayerChainEntity chain = new PlayerChainEntity(ModEntities.PLAYER_CHANE.get(), level);
        chain.ownerUUID = owner.getUUID();
        chain.targetUUID = target.getUUID();
        chain.chainPullToPos = owner.position().add(0, 1, 0);
        chain.startingPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        chain.pullTime = pullTime;
        chain.catchTime = catchTime;
        chain.setAttackType(type);
        chain.ownerUUID = owner.getUUID();
        chain.setOwnerId(owner.getId());
        chain.setPos(chain.chainPullToPos);
        level.addFreshEntity(chain);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Player owner = getOwner();
        LivingEntity target = getTarget();
        if (owner == null || target == null || !target.isAlive()) {
            this.discard();
            return;
        }
        this.chainPullToPos = owner.position().add(0, 1, 0);
        moveToTargetEndPoint();
    }

    private void moveToTargetEndPoint() {
        if (this.tickCount > this.catchTime && this.tickCount - this.catchTime <= this.pullTime) {
            float p = (float) (this.tickCount - this.catchTime) / (float) this.pullTime;
            Vec3 targetPoint = interpolateVectors(this.startingPos, this.chainPullToPos, p);
            this.setPos(targetPoint);
            LivingEntity target = this.getTarget();
            if (target != null) {
                if (!this.hasPassenger(target)) {
                    target.startRiding(this, true);
                }
            }
        } else if (this.tickCount <= this.catchTime) {
            LivingEntity target = this.getTarget();
            if (target == null) return;
            Vec3 targetAttachmentPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
            float p = (float) this.tickCount / (float) this.catchTime;
            Vec3 targetPoint = interpolateVectors(this.chainPullToPos, targetAttachmentPos, p);
            this.setPos(targetPoint);
            if (this.tickCount == this.catchTime) {
                this.startingPos = targetAttachmentPos;
                this.setPos(targetAttachmentPos);
                this.playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE, 1.0F, 1.5F);
            }
        } else {
            this.ejectPassengers();
            this.discard();
        }
    }

    private Vec3 interpolateVectors(Vec3 start, Vec3 end, float delta) {
        double d0 = start.x + (end.x - start.x) * (double) delta;
        double d1 = start.y + (end.y - start.y) * (double) delta;
        double d2 = start.z + (end.z - start.z) * (double) delta;
        return new Vec3(d0, d1, d2);
    }

    private Player getOwner() {
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(ownerUUID);
            return e instanceof Player ? (Player) e : null;
        }
        return null;
    }

    private LivingEntity getTarget() {
        if (targetUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(targetUUID);
            return e instanceof LivingEntity ? (LivingEntity) e : null;
        }
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.5D;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ATTACK_TYPE_ORDINAL, 0);
        this.entityData.define(OWNER_ID, -1);
    }

    public void setAttackType(QAElements type) {
        this.entityData.set(ATTACK_TYPE_ORDINAL, type == QAElements.FIRE ? 0 : 1);
    }

    public QAElements getAttackType() {
        return this.entityData.get(ATTACK_TYPE_ORDINAL) == 0 ? QAElements.FIRE : QAElements.ICE;
    }

    public void setOwnerId(int id) {
        this.entityData.set(OWNER_ID, id);
    }

    public int getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public Player getOwnerClient() {
        Entity e = this.level().getEntity(getOwnerId());
        return e instanceof Player ? (Player) e : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.hasUUID("Target")) this.targetUUID = tag.getUUID("Target");
        this.pullTime = tag.getInt("PullTime");
        this.catchTime = tag.getInt("CatchTime");

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (targetUUID != null) tag.putUUID("Target", targetUUID);
        tag.putInt("PullTime", pullTime);
        tag.putInt("CatchTime", catchTime);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}