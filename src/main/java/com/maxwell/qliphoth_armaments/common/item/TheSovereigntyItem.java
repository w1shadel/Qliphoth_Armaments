package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthEntity;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdbosses.init.BossDataComponents;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthRampageSwordEntity;
import com.maxwell.qliphoth_armaments.common.entity.PlayerChainEntity;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.maxwell.qliphoth_armaments.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TheSovereigntyItem extends SwordItem implements QAModWeapon {

    private static final float SHOCKWAVE_DAMAGE_MULTIPLIER = 2.0F;
    private static final float REACTION_DAMAGE_MULTIPLIER = 4.0F;
    private static final int RAMPAGE_TOTAL_DURATION = 200;

    public TheSovereigntyItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, (int) attackDamage, attackSpeed)));
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color fireRed = new Color(0xFF4500);
        Color royalGold = new Color(0xFFD700);
        Color iceBlue = new Color(0x00FFFF);
        return GradientTextUtil.createAnimatedGradient(translatedName, 200, fireRed, royalGold, iceBlue);
    }

    private boolean hasCore(ItemStack stack) {
        ItemCoreDataComponent component = stack.get(BossDataComponents.ITEM_CORE);
        if (component != null) {
            return component.getCoreType() == ItemCoreDataComponent.CoreType.FIRE_AND_ICE;
        }
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getRampageMode(stack) || super.isFoil(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && isSelected && entity instanceof Player player) {
            if (getRampageMode(stack)) {
                int duration = getRampageDuration(stack);
                duration--;
                if (duration <= 0) {
                    endRampageMode(stack, player, level);
                } else {
                    setRampageDuration(stack, duration);
                    if (level.getGameTime() % 5 == 0) {
                        replenishSwords(player, level);
                    }
                    if (level.getGameTime() % 10 == 0) {
                        autoLaunchSword(player, level);
                    }
                }
            } else {
                removeAllSwords(player, level);
            }
            if (level.getGameTime() % 20 == 0) {
                QAElements currentElement = getElementFromStack(stack);
                double radius = getRampageMode(stack) ? 12.0D : 6.0D;
                AABB area = player.getBoundingBox().inflate(radius, 2.0D, radius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && !player.isAlliedTo(e) && e instanceof Monster);
                for (LivingEntity target : targets) {
                    if (currentElement == QAElements.FIRE) {
                        target.setRemainingFireTicks(3);
                    } else {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    }
                    if (level instanceof ServerLevel serverLevel) {
                        MalkuthAttackType type = (currentElement == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
                        Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(type);
                        BallParticleOptions options = BallParticleOptions.builder()
                                .color(col.x, col.y, col.z).scalingOptions(0, 0, 5).size(0.1F).brightness(2).build();
                        serverLevel.sendParticles(options, target.getX(), target.getY() + 1, target.getZ(), 1, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }
        }
    }

    private void autoLaunchSword(Player player, Level level) {
        double range = 20.0D;
        AABB searchArea = player.getBoundingBox().inflate(range);
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, searchArea, e -> {
            if (e == player) return false;
            if (e.isAlliedTo(player)) return false;
            if (!e.isAlive()) return false;
            if (e instanceof Player) return false;
            return e instanceof Monster;
        });
        LivingEntity priorityTarget = player.getLastHurtMob();
        if (priorityTarget == null || !priorityTarget.isAlive()) {
            priorityTarget = player.getLastHurtByMob();
        }
        LivingEntity target = null;
        if (priorityTarget != null && priorityTarget.isAlive() && priorityTarget.distanceToSqr(player) < range * range) {
            target = priorityTarget;
        } else if (!enemies.isEmpty()) {
            target = enemies.stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .orElse(null);
        }
        if (target == null) return;
        List<MalkuthRampageSwordEntity> swords = level.getEntitiesOfClass(MalkuthRampageSwordEntity.class,
                player.getBoundingBox().inflate(10.0),
                e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID()) && !e.isLaunched());
        if (!swords.isEmpty()) {
            MalkuthRampageSwordEntity sword = swords.get(0);
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            sword.launch(target, targetPos);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        if (!level.isClientSide && stack.getItem() instanceof TheSovereigntyItem swordItem) {
            Entity targetEntity = event.getTarget();
            if (!(targetEntity instanceof LivingEntity)) return;
            LivingEntity target = (LivingEntity) targetEntity;
            if (swordItem.getRampageMode(stack)) {
                swordItem.performRampageAttack(stack, target, player);
                event.setCanceled(true);
            } else {
                swordItem.performNormalAttack(stack, target, player);
                event.setCanceled(true);
            }
        }
    }

    private void replenishSwords(Player player, Level level) {
        List<Integer> existingIndices = level.getEntitiesOfClass(MalkuthRampageSwordEntity.class,
                        player.getBoundingBox().inflate(30.0),
                        e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID()))
                .stream()
                .map(MalkuthRampageSwordEntity::getSwordIndex)
                .toList();
        for (int i = 0; i < 6; i++) {
            if (!existingIndices.contains(i)) {
                MalkuthRampageSwordEntity.summon(level, player, i);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 2.0F);
            }
        }
    }

    private void performRampageAttack(ItemStack stack, LivingEntity target, Player player) {
        ServerLevel level = (ServerLevel) player.level();
        List<MalkuthRampageSwordEntity> swords = level.getEntitiesOfClass(MalkuthRampageSwordEntity.class,
                player.getBoundingBox().inflate(10.0),
                e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID()) && !e.isLaunched());
        if (!swords.isEmpty()) {
            MalkuthRampageSwordEntity sword = swords.get(0);
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            sword.launch(target, targetPos);
            player.getCooldowns().addCooldown(this, 3);
        } else {
            player.playSound(SoundEvents.DISPENSER_FAIL, 1.0F, 1.5F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            QAElements currentElement = getElementFromStack(stack);
            if (player.isShiftKeyDown()) {
                if (getRampageMode(stack)) {
                    endRampageMode(stack, player, level);
                } else {
                    setRampageMode(stack, true);
                    setRampageDuration(stack, RAMPAGE_TOTAL_DURATION);
                    for (int i = 0; i < 6; i++) {
                        MalkuthRampageSwordEntity.summon(level, player, i);
                    }
                    player.displayClientMessage(Component.literal("Sovereignty Unbound!").withStyle(ChatFormatting.GOLD), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            (SoundEvent) BossSounds.MALKUTH_SWORD_ULTIMATE_IMPACT.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
                    player.getCooldowns().addCooldown(this, 40);
                }
                player.swing(hand);
                return InteractionResultHolder.success(stack);
            } else {
                if (getRampageMode(stack)) {
                    player.displayClientMessage(Component.literal("Cannot use Chain Pull in Rampage Mode.").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.fail(stack);
                }
                double reach = 20.0D;
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 traceEnd = eyePos.add(lookVec.scale(reach));
                AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0D);
                EntityHitResult result = ProjectileUtil.getEntityHitResult(
                        player, eyePos, traceEnd, searchBox, e -> e instanceof LivingEntity && !e.isSpectator(), reach * reach);
                if (result != null && result.getEntity() instanceof LivingEntity target) {
                    PlayerChainEntity.summon(level, player, currentElement, target, 10, 5);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            (SoundEvent) BossSounds.MALKUTH_CHAIN_PULL.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                    player.getCooldowns().addCooldown(this, 30);
                    player.swing(hand);
                    return InteractionResultHolder.success(stack);
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 1.0F, 0.5F);
                    player.swing(hand);
                    return InteractionResultHolder.fail(stack);
                }
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private void removeAllSwords(Player player, Level level) {
        level.getEntitiesOfClass(MalkuthRampageSwordEntity.class, player.getBoundingBox().inflate(30.0),
                        e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID()))
                .forEach(Entity::discard);
    }

    private void endRampageMode(ItemStack stack, Player player, Level level) {
        setRampageMode(stack, false);
        setRampageDuration(stack, 0);
        removeAllSwords(player, level);
        player.displayClientMessage(Component.literal("Rampage Mode Ended.").withStyle(ChatFormatting.RED), true);
    }

    private void performNormalAttack(ItemStack stack, LivingEntity target, Player player) {
        ServerLevel level = (ServerLevel) player.level();
        QAElements currentElement = getElementFromStack(stack);
        MalkuthAttackType visualType = (currentElement == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        boolean triggerReaction = false;
        if (currentElement == QAElements.FIRE) {
            if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || target.hasEffect(MobEffects.WEAKNESS))
                triggerReaction = true;
        } else {
            if (target.isOnFire()) triggerReaction = true;
        }
        ElementalReactionManager.applyState(target, currentElement, 100);
        if (!player.getCooldowns().isOnCooldown(this)) {
            Vec3 dir = player.getLookAngle().multiply(1, 0, 1).normalize();
            if (dir.lengthSqr() < 0.01) dir = player.getForward().multiply(1, 0, 1).normalize();
            Vec3 startPos = player.position().add(dir.scale(1.5));
            Vec3 visualEnd = dir.scale(12.0);
            float damageMult = triggerReaction ? REACTION_DAMAGE_MULTIPLIER : SHOCKWAVE_DAMAGE_MULTIPLIER;
            MalkuthEarthquake.summon(level, visualType, startPos, visualEnd, 15, (float) Math.PI / 4.0F, 0.0F);
            float damage = getScaledDamage(player, damageMult);
            MalkuthPlayerAttackLogic.summon(level, player, startPos, dir, currentElement, damage, false);
            float shakeAmp = triggerReaction ? 6.0F : 3.0F;
            PositionedScreenShakePacket.send(level,
                    FDShakeData.builder().amplitude(shakeAmp).outTime(10).build(),
                    target.position(), 32.0D);
            if (triggerReaction) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        (SoundEvent) BossSounds.MALKUTH_VOLCANO_ERRUPTION.get(), SoundSource.PLAYERS, 1.5F, 1.2F);
                target.clearFire();
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        (SoundEvent) BossSounds.MALKUTH_SWORD_EARTH_IMPACT.get(), SoundSource.PLAYERS, 1.5F, 0.8F);
            }
            toggleMode(stack, player);
            player.getCooldowns().addCooldown(this, 15);
        }
    }

    private boolean getRampageMode(ItemStack stack) {
        // コンポーネントが存在しない場合は false を返す
        return stack.getOrDefault(ModDataComponents.RAMPAGE_MODE, false);
    }

    private void setRampageMode(ItemStack stack, boolean active) {
        // コンポーネントに値をセットする
        stack.set(ModDataComponents.RAMPAGE_MODE, active);
    }

    private int getRampageDuration(ItemStack stack) {
        // コンポーネントが存在しない場合は 0 を返す
        return stack.getOrDefault(ModDataComponents.RAMPAGE_DURATION, 0);
    }

    private void setRampageDuration(ItemStack stack, int duration) {
        stack.set(ModDataComponents.RAMPAGE_DURATION, duration);
    }

    private void toggleMode(ItemStack stack, Player player) {
        // 現在のモードを取得
        int currentMode = stack.getOrDefault(ModDataComponents.MODE, 0);
        // モードを切り替え
        int newMode = (currentMode == 0) ? 1 : 0;
        // 新しい値を保存
        stack.set(ModDataComponents.MODE, newMode);
        float pitch = (newMode == 0) ? 1.0F : 1.2F;
        // サウンド再生 (SoundSource等は既存のコードに合わせてください)
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_HIT.get(), SoundSource.PLAYERS, 0.5F, pitch);
    }

    private QAElements getElementFromStack(ItemStack stack) {
        int mode = stack.getOrDefault(ModDataComponents.MODE, 0);
        return (mode == 0) ? QAElements.FIRE : QAElements.ICE;
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (playerAttack * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        QAElements element = getElementFromStack(stack);
        Component elementText = (element == QAElements.FIRE)
                ? Component.literal("FIRE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                : Component.literal("ICE").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
        tooltip.add(Component.translatable("Current Authority: ").append(elementText));
        tooltip.add(Component.empty());
        if (getRampageMode(stack)) {
            tooltip.add(Component.literal("Rampage Mode: ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal(String.format("%.1fs remaining", getRampageDuration(stack) / 20.0F)).withStyle(ChatFormatting.YELLOW)));
            tooltip.add(Component.literal("Active: Auto-targeting flying swords!").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Left-Click: Manual Launch.").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Shift + Right-Click: Exit Rampage Mode.").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.literal("Passive: Surrounding enemies bow before your element.").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Left-Click: Shockwave Attack (Combo: Rupture!).").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("Right-Click: Chain Pull.").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("Shift + Right-Click: Activate Rampage Mode.").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.empty());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (getRampageMode(stack)) {
            float progress = (float) getRampageDuration(stack) / RAMPAGE_TOTAL_DURATION;
            return Mth.hsvToRgb(progress * 0.333F, 1.0F, 1.0F);
        }
        QAElements element = getElementFromStack(stack);
        return (element == QAElements.FIRE) ? 0xFF4500 : 0x00FFFF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (getRampageMode(stack)) {
            return (int) (getRampageDuration(stack) / (float) RAMPAGE_TOTAL_DURATION * 13);
        }
        return 13;
    }
}