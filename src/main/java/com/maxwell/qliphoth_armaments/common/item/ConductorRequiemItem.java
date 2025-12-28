package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.client.BossParticles;
import com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions;
import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.items.WeaponCoreItem;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.maxwell.qliphoth_armaments.common.config.QAConfig;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "qliphoth_armaments", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConductorRequiemItem extends SwordItem implements QAModWeapon {

    private static final String TAG_TARGET_MINION_COUNT = "QAChesedTargetMinions";
    private static final String TAG_IS_AWAKENED = "QAChesedIsAwakened";
    private static final String TAG_RECOIL_TIMER = "ChesedRecoilTimer";

    private static final int LONG_PRESS_THRESHOLD = 20;

    public ConductorRequiemItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    private boolean hasCore(ItemStack stack) {
        return WeaponCoreItem.getItemCore(stack) == ItemCoreDataComponent.CoreType.LIGHTNING;
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
        if (player.isSpectator()) return;
        boolean isHolding = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        boolean hotbarOnly = QAConfig.COMMON.conductorRequiemHotbarOnly.get();
        boolean isInHotbarOrOffhand = (slotId >= 0 && slotId <= 8) || slotId == 40;
        int localDesiredCount = 0;
        if (isHolding) {
            localDesiredCount = 4;
        } else {
            if (!hotbarOnly || isInHotbarOrOffhand) {
                localDesiredCount = 1;
            }
        }
        CompoundTag data = player.getPersistentData();
        int currentMax = data.getInt(TAG_TARGET_MINION_COUNT);
        if (localDesiredCount > currentMax) {
            data.putInt(TAG_TARGET_MINION_COUNT, localDesiredCount);
        }
        if (hasCore(stack)) {
            data.putBoolean(TAG_IS_AWAKENED, true);
        }
        if (player.getMainHandItem() == stack) {
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
                                FDShakeData.builder().frequency(20.0F).amplitude(3.0F).inTime(0).stayTime(5).outTime(15).build(),
                                player.position(), 64.0D);
                        data.remove(TAG_RECOIL_TIMER);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        CompoundTag data = player.getPersistentData();
        int targetCount = data.getInt(TAG_TARGET_MINION_COUNT);
        boolean isAwakened = data.getBoolean(TAG_IS_AWAKENED);
        manageMinions(player, (ServerLevel) player.level(), targetCount, isAwakened);
        data.putInt(TAG_TARGET_MINION_COUNT, 0);
        data.putBoolean(TAG_IS_AWAKENED, false);
    }

    private static void manageMinions(Player owner, ServerLevel level, int desiredCount, boolean isAwakened) {
        List<ChesedCoreMinionEntity> currentMinions = level.getEntitiesOfClass(
                ChesedCoreMinionEntity.class,
                owner.getBoundingBox().inflate(128),
                minion -> minion.getOwnerUUID() != null && minion.getOwnerUUID().equals(owner.getUUID())
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
                    usedSlots.add(slot);
                }
            }
        } else if (currentCount > desiredCount) {
            int toRemove = currentCount - desiredCount;
            for (int i = currentCount - 1; i >= desiredCount; i--) {
                ChesedCoreMinionEntity removed = currentMinions.get(i);
                removed.discard();
            }
        }
        for (int i = 0; i < Math.min(currentCount, desiredCount); i++) {
            ChesedCoreMinionEntity minion = currentMinions.get(i);
            if (minion.isAlive() && minion.isAwakened() != isAwakened) {
                minion.setAwakened(isAwakened);
            }
        }
        int validCount = 0;
        for (ChesedCoreMinionEntity minion : currentMinions) {
            if (minion.isAlive()) {
                if (validCount < desiredCount) {
                    minion.setFormationSlot(validCount++);
                }
            }
        }
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

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (livingEntity instanceof Player player) {
            int duration = this.getUseDuration(stack) - count;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 2, false, false, false));
            if (!level.isClientSide()) {
                ServerLevel serverLevel = (ServerLevel) level;
                if (duration < LONG_PRESS_THRESHOLD) {
                    if (duration % 2 == 0) {
                        double radius = 1.5;
                        double angle = (duration * 0.5);
                        double x = player.getX() + Math.cos(angle) * radius;
                        double z = player.getZ() + Math.sin(angle) * radius;
                        double y = player.getY() + 1.5 + Math.sin(duration * 0.2) * 0.5;
                        BallParticleOptions options = BallParticleOptions.builder()
                                .color(0.2F, 0.8F, 1.0F).scalingOptions(0, 0, 5).size(0.15F).brightness(3).build();
                        serverLevel.sendParticles(options, x, y, z, 0,
                                (player.getX() - x) * 0.1, (player.getY() + 1.0 - y) * 0.1, (player.getZ() - z) * 0.1, 1.0);
                    }
                    if (duration % 5 == 0) {
                        spawnArcLightning(player, serverLevel);
                    }
                }
                if (duration == LONG_PRESS_THRESHOLD) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            BossSounds.CHESED_RAY_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F);
                    BallParticleOptions burst = BallParticleOptions.builder()
                            .color(0.5F, 1.0F, 1.0F).scalingOptions(0, 0, 10).size(0.3F).brightness(5).build();
                    serverLevel.sendParticles(burst, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                    PositionedScreenShakePacket.send(serverLevel,
                            FDShakeData.builder().amplitude(1.0F).inTime(2).stayTime(5).outTime(5).build(),
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
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5F, 0.1F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        BossSounds.MALKUTH_SWORD_EARTH_IMPACT.get(), SoundSource.PLAYERS, 0.8F, 1.5F);
                PositionedScreenShakePacket.send((ServerLevel) level,
                        FDShakeData.builder().amplitude(2.0F).outTime(10).build(),
                        player.position(), 16.0D);
            }
            player.swing(hand);
            return InteractionResultHolder.success(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) return;
        int timeUsed = this.getUseDuration(stack) - timeCharged;
        if (timeUsed < LONG_PRESS_THRESHOLD) {
            if (!level.isClientSide()) {
                sendCommandToMinions(player, "FIRE_CROSS_RAY");
                player.getCooldowns().addCooldown(this, 80);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.5F, 2.0F);
                PositionedScreenShakePacket.send((ServerLevel) level,
                        FDShakeData.builder().amplitude(0.5F).outTime(5).build(),
                        player.position(), 16.0D);
            }
        } else {
            if (!level.isClientSide()) {
                sendCommandToMinions(player, "FIRE_LASER");
                player.getCooldowns().addCooldown(this, 200);
                player.getPersistentData().putInt(TAG_RECOIL_TIMER, 33);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        BossSounds.CHESED_RAY_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        player.swing(player.getUsedItemHand());
    }

    private void spawnArcLightning(Player player, ServerLevel level) {
        double radius = 1.5;
        double x = player.getX() + (level.random.nextDouble() - 0.5) * radius * 2;
        double y = player.getY() + 0.5 + level.random.nextDouble() * 1.5;
        double z = player.getZ() + (level.random.nextDouble() - 0.5) * radius * 2;
        ArcLightningOptions arc = ArcLightningOptions.builder((ParticleType) BossParticles.ARC_LIGHTNING.get())
                .end(player.getX(), player.getY() + 1.0, player.getZ())
                .lifetime(4)
                .color(100, 255, 255)
                .lightningSpread(0.1F)
                .width(0.1F)
                .segments(4)
                .build();
        FDLibCalls.sendParticles(level, arc, new Vec3(x, y, z), 64.0D);
    }

    private void sendCommandToMinions(Player owner, String command) {
        if (owner.level().isClientSide()) return;
        owner.level().getEntitiesOfClass(
                ChesedCoreMinionEntity.class,
                owner.getBoundingBox().inflate(128),
                minion -> owner.getUUID().equals(minion.getOwnerUUID())
        ).forEach(minion -> minion.receiveCommand(command));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
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