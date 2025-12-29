package com.maxwell.qliphoth_armaments.common.entity;

import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerChainEntity extends LivingEntity {

    private static final EntityDataAccessor<Integer> ATTACK_TYPE_ORDINAL = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private UUID ownerUUID;
    private UUID targetUUID;
    private int pullTime;
    private int catchTime;

    private Vec3 chainPullToPos = Vec3.ZERO;
    private Vec3 startingPos = Vec3.ZERO;

    public PlayerChainEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void summon(Level level, Player owner, QAElements type, LivingEntity target, int pullTime, int catchTime) {
        PlayerChainEntity chain = new PlayerChainEntity(ModEntities.PLAYER_CHANE.get(), level);
        chain.ownerUUID = owner.getUUID();
        chain.targetUUID = target.getUUID();
        chain.chainPullToPos = owner.getEyePosition();
        chain.startingPos = getTargetAttachmentPos(target);
        chain.pullTime = pullTime;
        chain.catchTime = catchTime;
        chain.setAttackType(type);
        chain.setOwnerId(owner.getId());
        chain.setPos(chain.chainPullToPos);
        level.addFreshEntity(chain);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (startingPos == null || chainPullToPos == null) {
            this.discard();
            return;
        }
        Player owner = getOwner();
        LivingEntity target = getTarget();
        if (owner == null || !owner.isAlive() || target == null || !target.isAlive() || this.tickCount > this.catchTime + this.pullTime + 20) {
            this.ejectPassengers();
            this.discard();
            return;
        }
        this.chainPullToPos = owner.getEyePosition();
        moveToTargetEndPoint();
    }

    private void moveToTargetEndPoint() {
        if (this.tickCount > this.catchTime && this.tickCount - this.catchTime <= this.pullTime) {
            float p = (float) (this.tickCount - this.catchTime) / (float) this.pullTime;
            Vec3 targetPoint = this.startingPos.lerp(this.chainPullToPos, p);
            Vec3 deltaMovement = targetPoint.subtract(this.position());
            this.setDeltaMovement(deltaMovement);
            LivingEntity target = this.getTarget();
            if (target != null && !this.hasPassenger(target)) {
                target.startRiding(this, true);
            }
        } else if (this.tickCount <= this.catchTime) {
            LivingEntity target = this.getTarget();
            if (target == null) return;
            Vec3 targetAttachmentPos = getTargetAttachmentPos(target);
            float p = (float) this.tickCount / (float) this.catchTime;
            Vec3 targetPoint = this.chainPullToPos.lerp(targetAttachmentPos, p);
            Vec3 deltaMovement = targetPoint.subtract(this.position());
            this.setDeltaMovement(deltaMovement);
            if (this.tickCount == this.catchTime) {
                this.startingPos = targetAttachmentPos;
                this.setPos(targetAttachmentPos);
                this.playSound(SoundEvents.CHAIN_PLACE, 1.0F, 1.5F);
            }
        } else {
            this.setDeltaMovement(Vec3.ZERO);
            this.ejectPassengers();
            this.discard();
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return this.position().add(0, -entity.getBbHeight() / 2.0, 0);
    }

    public static Vec3 getTargetAttachmentPos(LivingEntity target) {
        return target.position().add(0, target.getBbHeight() / 2.0, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_TYPE_ORDINAL, 0);
        builder.define(OWNER_ID, -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.hasUUID("Target")) this.targetUUID = tag.getUUID("Target");
        this.pullTime = tag.getInt("PullTime");
        this.catchTime = tag.getInt("CatchTime");
        if (tag.contains("PullToX")) {
            this.chainPullToPos = new Vec3(tag.getDouble("PullToX"), tag.getDouble("PullToY"), tag.getDouble("PullToZ"));
        }
        if (tag.contains("StartX")) {
            this.startingPos = new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (targetUUID != null) tag.putUUID("Target", targetUUID);
        tag.putInt("PullTime", pullTime);
        tag.putInt("CatchTime", catchTime);
        if (chainPullToPos != null) {
            tag.putDouble("PullToX", chainPullToPos.x);
            tag.putDouble("PullToY", chainPullToPos.y);
            tag.putDouble("PullToZ", chainPullToPos.z);
        }
        if (startingPos != null) {
            tag.putDouble("StartX", startingPos.x);
            tag.putDouble("StartY", startingPos.y);
            tag.putDouble("StartZ", startingPos.z);
        }
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
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}