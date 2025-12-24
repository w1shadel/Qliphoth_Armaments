package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.init.BossDataComponents;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import com.finderfeed.fdbosses.client.BossParticles;
import com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConductorRequiemItem extends SwordItem implements QAModWeapon {
    private static final String TAG_HAS_ORCHESTRATOR = "HasChesedOrchestrator";

    private boolean hasCore(ItemStack stack) {
        ItemCoreDataComponent coreData = stack.get(BossDataComponents.ITEM_CORE.get());
        return coreData != null && coreData.getCoreType() == ItemCoreDataComponent.CoreType.LIGHTNING;
    }

    public ConductorRequiemItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color lightBlue = new Color(0x00BFFF);
        Color white = Color.WHITE;
        return GradientTextUtil.createAnimatedGradient(translatedName, 150, lightBlue, white, lightBlue);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (player.isSpectator()) {
            manageMinions(player, (ServerLevel) level, 0, false);
            player.getPersistentData().remove(TAG_HAS_ORCHESTRATOR);
            return;
        }
        player.getPersistentData().putBoolean(TAG_HAS_ORCHESTRATOR, true);
        boolean isHolding = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        int desiredCount = isHolding ? 4 : 1;
        boolean isAwakened = hasCore(stack);
        manageMinions(player, (ServerLevel) level, desiredCount, isAwakened);
        if (player.getMainHandItem() == stack) {
            CompoundTag data = player.getPersistentData();
            if (data.contains(TAG_RECOIL_TIMER)) {
                int timer = data.getInt(TAG_RECOIL_TIMER);
                if (timer > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false, false));
                    timer--;
                    data.putInt(TAG_RECOIL_TIMER, timer);
                    if (timer == 0) {
                        Vec3 look = player.getLookAngle();
                        player.push(-look.x * 1.5, 0.4, -look.z * 1.5);
                        player.hurtMarked = true;
                        PositionedScreenShakePacket.send((ServerLevel) level,
                                FDShakeData.builder()
                                        .frequency(20.0F)
                                        .amplitude(3.0F)
                                        .inTime(0)
                                        .stayTime(5)
                                        .outTime(15)
                                        .build(),
                                player.position(), 64.0D);
                        data.remove(TAG_RECOIL_TIMER);
                    }
                }
            }
        }
    }

    private static final int LONG_PRESS_THRESHOLD = 20;

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (livingEntity instanceof Player player) {
            int duration = this.getUseDuration(stack, livingEntity) - count;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 2, false, false, false));
            if (!level.isClientSide() && duration < LONG_PRESS_THRESHOLD) {
                if (duration % 3 == 0) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    double radius = 1.5;
                    double x = player.getX() + (level.random.nextDouble() - 0.5) * radius * 2;
                    double y = player.getY() + 0.5 + level.random.nextDouble() * 1.5;
                    double z = player.getZ() + (level.random.nextDouble() - 0.5) * radius * 2;
                    ArcLightningOptions arc = ArcLightningOptions.builder((ParticleType) BossParticles.ARC_LIGHTNING.get())
                            .end(player.getX(), player.getY() + 1.0, player.getZ())
                            .lifetime(2)
                            .color(100, 255, 255)
                            .lightningSpread(0.15F)
                            .width(0.15F)
                            .segments(4)
                            .build();
                    FDLibCalls.sendParticles(serverLevel, arc, new Vec3(x, y, z), 64.0D);
                }
            }
            if (duration == LONG_PRESS_THRESHOLD) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        BossSounds.CHESED_RAY_CHARGE.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.5F);
                if (!level.isClientSide()) {
                    PositionedScreenShakePacket.send((ServerLevel) level,
                            FDShakeData.builder().amplitude(0.5F).inTime(0).stayTime(2).outTime(5).build(),
                            player.position(), 32.0D);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                sendCommandToMinions(player, "DROP_MONOLITH");
                player.getCooldowns().addCooldown(this, 120);
            }
            return InteractionResultHolder.success(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    private static final String TAG_RECOIL_TIMER = "ChesedRecoilTimer";

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) return;
        int timeUsed = this.getUseDuration(stack, livingEntity) - timeCharged;
        if (timeUsed < LONG_PRESS_THRESHOLD) {
            if (!level.isClientSide()) {
                sendCommandToMinions(player, "FIRE_CROSS_RAY");
                player.getCooldowns().addCooldown(this, 80);
            }
        } else {
            if (!level.isClientSide()) {
                sendCommandToMinions(player, "FIRE_LASER");
                player.getCooldowns().addCooldown(this, 200);
                player.getPersistentData().putInt(TAG_RECOIL_TIMER, 33);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private void sendCommandToMinions(Player owner, String command) {
        if (owner.level().isClientSide()) return;
        owner.level().getEntitiesOfClass(
                ChesedCoreMinionEntity.class,
                owner.getBoundingBox().inflate(64),
                minion -> owner.getUUID().equals(minion.getOwnerUUID())
        ).forEach(minion -> minion.receiveCommand(command));
    }

    private static void manageMinions(Player owner, ServerLevel level, int desiredCount, boolean isAwakened) {
        List<ChesedCoreMinionEntity> currentMinions = level.getEntitiesOfClass(
                ChesedCoreMinionEntity.class,
                owner.getBoundingBox().inflate(64),
                minion -> owner.getUUID().equals(minion.getOwnerUUID())
        );
        currentMinions.sort(Comparator.comparingInt(ChesedCoreMinionEntity::getFormationSlot));
        int currentCount = currentMinions.size();
        if (currentCount < desiredCount) {
            Set<Integer> usedSlots = currentMinions.stream()
                    .map(ChesedCoreMinionEntity::getFormationSlot)
                    .collect(Collectors.toSet());
            for (int slot = 0; slot < desiredCount; slot++) {
                if (!usedSlots.contains(slot)) {
                    spawnMinion(owner, level, slot);
                }
            }

        } else if (currentCount > desiredCount) {
            int toRemove = currentCount - desiredCount;
            for (int i = 0; i < toRemove; i++) {
                ChesedCoreMinionEntity removed = currentMinions.remove(currentMinions.size() - 1);
                removed.discard();
            }
        }
        for (ChesedCoreMinionEntity minion : currentMinions) {
            if (minion.isAwakened() != isAwakened) {
                minion.setAwakened(isAwakened);
            }
        }
        reassignSlots(currentMinions);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    private static void spawnMinion(Player owner, ServerLevel level, int slot) {
        ChesedCoreMinionEntity minion = ModEntities.CHESED_CORE_MINION.get().create(level);
        if (minion != null) {
            minion.setOwner(owner);
            minion.setFormationSlot(slot);
            minion.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
            level.addFreshEntity(minion);
        }
    }

    private static void reassignSlots(List<ChesedCoreMinionEntity> minions) {
        int currentSlot = 0;
        for (ChesedCoreMinionEntity minion : minions) {
            minion.setFormationSlot(currentSlot++);
        }
    }

    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;
        if (player.getPersistentData().contains(TAG_HAS_ORCHESTRATOR)) {
            player.getPersistentData().remove(TAG_HAS_ORCHESTRATOR);
        } else {
            manageMinions(player, (ServerLevel) player.level(), 0, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (hasCore(stack)) {
            tooltip.add(Component.translatable("item.qliphoth_armaments.conductors_requiem.fuse.lore"));
        } else {
            tooltip.add(Component.translatable("item.qliphoth_armaments.conductors_requiem.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        tooltip.add(Component.empty());
        if (Screen.hasShiftDown()) {
            addPassiveSkill(tooltip,
                    "item.qliphoth_armaments.conductors_requiem.passive_1",
                    "item.qliphoth_armaments.conductors_requiem.passive_2");
            if (hasCore(stack)) {
                addCoreSkill(tooltip, "item.qliphoth_armaments.conductors_requiem.fuse.skill1");
            }
            addRightClickSkill(tooltip,
                    "item.qliphoth_armaments.conductors_requiem.r_skill_1",
                    "item.qliphoth_armaments.conductors_requiem.r_skill_2");
            if (hasCore(stack)) {
                addCoreSkill(tooltip, "item.qliphoth_armaments.conductors_requiem.fuse.skill2");
            }
            addLongRightClickSkill(tooltip,
                    "item.qliphoth_armaments.conductors_requiem.lr_skill_1",
                    "item.qliphoth_armaments.conductors_requiem.lr_skill_2",
                    "item.qliphoth_armaments.conductors_requiem.lr_skill_3");
            if (hasCore(stack)) {
                addCoreSkill(tooltip, "item.qliphoth_armaments.conductors_requiem.fuse.skill3");
            }
            addShiftRightClickSkill(tooltip,
                    "item.qliphoth_armaments.conductors_requiem.sr_skill_1",
                    "item.qliphoth_armaments.conductors_requiem.sr_skill_2");
            if (!hasCore(stack)) {
                addFuseHint(tooltip, "item.fdbosses.lightning_core");
            }
        } else {
            addPressShiftHint(tooltip);
        }
    }

}