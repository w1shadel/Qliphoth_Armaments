package com.maxwell.qliphoth_armaments.common.entity;

import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.event.SinManager;
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
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerChainEntity extends LivingEntity {

    private static final EntityDataAccessor<Integer> ATTACK_TYPE_ORDINAL = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ATTACK_MODE = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> ORIGIN_POS = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> ADD_SIN = SynchedEntityData.defineId(PlayerChainEntity.class, EntityDataSerializers.BOOLEAN);
    private UUID ownerUUID;
    private UUID targetUUID;
    private int pullTime;
    private int catchTime;
    private float damage;
    private static final int LINGER_TIME = 15;
    private Vec3 chainPullToPos = Vec3.ZERO;
    private Vec3 startingPos = Vec3.ZERO;

    public PlayerChainEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void summon(Level level, Player owner, QAElements type, LivingEntity target, int pullTime, int catchTime) {
        PlayerChainEntity chain = new PlayerChainEntity(ModEntities.PLAYER_CHANE.get(), level);
        chain.setupCommon(owner, target, type);
        chain.chainPullToPos = owner.getEyePosition();
        chain.startingPos = getTargetAttachmentPos(target);
        chain.pullTime = pullTime;
        chain.catchTime = catchTime;
        chain.setAttackMode(false);
        chain.setPos(chain.chainPullToPos);
        level.addFreshEntity(chain);
    }

    public static void summonAttack(Level level, Player owner, QAElements type, LivingEntity target, Vec3 portalPos, float damage, boolean addSin) {
        PlayerChainEntity chain = new PlayerChainEntity(ModEntities.PLAYER_CHANE.get(), level);
        chain.setupCommon(owner, target, type);
        chain.setOriginPos(portalPos);
        chain.setAttackMode(true);
        chain.setShouldAddSin(addSin);
        chain.chainPullToPos = portalPos;
        chain.startingPos = getTargetAttachmentPos(target);
        chain.catchTime = 20;
        chain.damage = damage;
        chain.setPos(portalPos.x, portalPos.y, portalPos.z);
        level.addFreshEntity(chain);
    }

    public void setAttackMode(boolean mode) {
        this.entityData.set(IS_ATTACK_MODE, mode);
    }

    public boolean isAttackMode() {
        return this.entityData.get(IS_ATTACK_MODE);
    }

    private void setupCommon(Player owner, LivingEntity target, QAElements type) {
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.setAttackType(type);
        this.setOwnerId(owner.getId());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (isAttackMode()) {
            tickAttack();
        } else {
            tickPull();
        }
    }

    public void setShouldAddSin(boolean value) {
        this.entityData.set(ADD_SIN, value);
    }

    public boolean shouldAddSin() {
        return this.entityData.get(ADD_SIN);
    }

    private void tickAttack() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || this.tickCount > this.catchTime + LINGER_TIME) {
            this.discard();
            return;
        }
        this.startingPos = getTargetAttachmentPos(target);
        float progress = Math.min((float) this.tickCount / (float) this.catchTime, 1.0f);
        Vec3 portal = getOriginPos();
        if (portal != null) {
            Vec3 currentTipPos = portal.lerp(this.startingPos, progress);
            this.setPos(currentTipPos.x, currentTipPos.y, currentTipPos.z);
        }
        if (this.tickCount == this.catchTime) {
            Player owner = getOwner();
            if (owner != null) {
                target.hurt(this.damageSources().playerAttack(owner), this.damage);
                if (this.shouldAddSin()) {
                    SinManager.applySinToEntity(target, 1);
                }
            }
            this.playSound(SoundEvents.CHAIN_HIT, 1.2F, 0.8F);
        }
    }

    public void tickPull() {
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
        builder.define(ORIGIN_POS, new Vector3f(Float.NaN, Float.NaN, Float.NaN));
        builder.define(IS_ATTACK_MODE, false);
        builder.define(ADD_SIN, false);
    }

    public void setOriginPos(Vec3 pos) {
        this.entityData.set(ORIGIN_POS, new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
    }

    public Vec3 getOriginPos() {
        Vector3f v = this.entityData.get(ORIGIN_POS);
        if (v.x == 0 && v.y == 0 && v.z == 0) return null;
        return new Vec3(v.x, v.y, v.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.hasUUID("Target")) this.targetUUID = tag.getUUID("Target");
        this.pullTime = tag.getInt("PullTime");
        this.catchTime = tag.getInt("CatchTime");
        this.damage = tag.getFloat("Damage");
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
        tag.putFloat("Damage", damage);
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