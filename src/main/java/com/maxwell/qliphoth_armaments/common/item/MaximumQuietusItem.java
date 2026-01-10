package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.BossUtil;
import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthAttackType;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.MalkuthEntity;
import com.finderfeed.fdbosses.content.entities.malkuth_boss.malkuth_earthquake.MalkuthEarthquake;
import com.finderfeed.fdbosses.init.BossDataComponents;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.maxwell.qliphoth_armaments.common.entity.PlayerCannonProjectile;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.maxwell.qliphoth_armaments.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;

public class MaximumQuietusItem extends SwordItem implements QAModWeapon {
    private static final int CHARGE_LV1 = 10;
    private static final int CHARGE_LV2 = 25;
    private static final float AWAKENED_MELEE_PROC_MULTIPLIER = 3.5F;
    private static final float CHARGED_SHOT_MULTIPLIER = 3.5F;
    private static final float NORMAL_SHOT_MULTIPLIER = 1.8F;
    private static final float ULTIMATE_SHOT_MULTIPLIER = 6.0F;

    public MaximumQuietusItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
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
        Color magicPurple = new Color(0x9400D3);
        Color iceBlue = new Color(0x00FFFF);
        return GradientTextUtil.createAnimatedGradient(translatedName, 200, fireRed, magicPurple, iceBlue);
    }

    private boolean hasCore(ItemStack stack) {
        ItemCoreDataComponent component = stack.get(BossDataComponents.ITEM_CORE);
        if (component != null) {
            return component.getCoreType() == ItemCoreDataComponent.CoreType.FIRE_AND_ICE;
        }
        return false;
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (playerAttack * multiplier);
        return Math.max(1.0f, finalDamage);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && attacker instanceof Player player) {
            QAElements currentElement = getElementFromStack(stack);
            ElementalReactionManager.applyState(target, currentElement, 100);
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F, 0.8F);
            if (hasCore(stack)) {
                if (!player.getCooldowns().isOnCooldown(this)) {
                    ServerLevel level = (ServerLevel) target.level();
                    MalkuthAttackType visualType = (currentElement == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
                    Vec3 dir = player.getLookAngle().multiply(1, 0, 1).normalize();
                    if (dir.lengthSqr() < 0.01) dir = player.getForward().multiply(1, 0, 1).normalize();
                    Vec3 startPos = player.position().add(dir.scale(1.5));
                    Vec3 dirAndLen = dir.scale(15.0);
                    summonStableMalkuthEarthquake(level, visualType, startPos, dirAndLen, 20, (float) Math.PI / 3.0F, 0.0F);
                    MalkuthPlayerAttackLogic.summon(level, player, startPos, dir, currentElement,
                            getScaledDamage(player, AWAKENED_MELEE_PROC_MULTIPLIER), false);
                    PositionedScreenShakePacket.send(level,
                            FDShakeData.builder().amplitude(3.0F).frequency(15.0F).outTime(8).build(),
                            target.position(), 24.0D);
                    player.getCooldowns().addCooldown(this, 10);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private void summonStableMalkuthEarthquake(ServerLevel level, MalkuthAttackType type, Vec3 start, Vec3 direction, int lifetime, float arcAngle, float damage) {
        try {
            MalkuthEarthquake.summon(level, type, start, direction, lifetime, arcAngle, damage);
        } catch (Exception e) {
            spawnFallbackParticles(level, type, start, direction);
        }
    }

    private void spawnFallbackParticles(ServerLevel level, MalkuthAttackType type, Vec3 start, Vec3 dir) {
        Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(type);
        double length = dir.length();
        Vec3 normDir = dir.normalize();
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) {
                Vec3 pos = start.add(normDir.scale(i));
                BallParticleOptions options = BallParticleOptions.builder()
                        .color(col.x, col.y, col.z).scalingOptions(0, 0, 4).size(0.15F).brightness(3).build();
                level.sendParticles(options, pos.x, pos.y + 0.5, pos.z, 1, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isAwakened = hasCore(stack);
        if (isAwakened && player.isShiftKeyDown()) {
            if (!level.isClientSide) toggleMode(stack, player);
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResultHolder.success(stack);
        }
        if (!isAwakened && !player.isCrouching()) {
            if (!level.isClientSide) toggleMode(stack, player);
            return InteractionResultHolder.success(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player)) return;
        boolean isAwakened = hasCore(stack);
        int usedTicks = this.getUseDuration(stack, player) - count;
        if (isAwakened) {
            return;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            QAElements element = getElementFromStack(stack);
            MalkuthAttackType visualType = (element == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
            Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(visualType);
            if (usedTicks % 2 == 0) {
                double radius = 1.5;
                double angle = (usedTicks * 0.5) % (Math.PI * 2);
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (usedTicks % 10) * 0.1;
                BallParticleOptions options = BallParticleOptions.builder()
                        .color(col.x, col.y, col.z).scalingOptions(0, 0, 5).size(0.15F).brightness(3).build();
                serverLevel.sendParticles(options,
                        player.getX() + offsetX, player.getY() + 1.0 + offsetY, player.getZ() + offsetZ,
                        0, -offsetX * 0.1, 0.05, -offsetZ * 0.1, 1.0);
            }
            if (usedTicks == CHARGE_LV1) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 0.8F);
            }
            if (usedTicks == CHARGE_LV2) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                BallParticleOptions burst = BallParticleOptions.builder()
                        .color(col.x, col.y, col.z).scalingOptions(0, 0, 10).size(0.3F).brightness(5).build();
                serverLevel.sendParticles(burst, player.getX(), player.getY() + 1.5, player.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
        if (player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 2, false, false, false));
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        int usedTicks = this.getUseDuration(stack, player) - timeLeft;
        boolean isAwakened = hasCore(stack);
        if (!level.isClientSide) {
            QAElements elementType = getElementFromStack(stack);
            if (isAwakened) {
                shootUltimate((ServerLevel) level, player, elementType,
                        getScaledDamage(player, ULTIMATE_SHOT_MULTIPLIER));
                player.getCooldowns().addCooldown(this, 40);
            } else {
                if (usedTicks >= CHARGE_LV2) {
                    shootProjectile((ServerLevel) level, player, elementType, 2.5F,
                            getScaledDamage(player, CHARGED_SHOT_MULTIPLIER), false);
                    player.getCooldowns().addCooldown(this, 30);
                } else if (usedTicks >= 5) {
                    shootProjectile((ServerLevel) level, player, elementType, 1.2F,
                            getScaledDamage(player, NORMAL_SHOT_MULTIPLIER), false);
                    player.getCooldowns().addCooldown(this, 15);
                }
            }
        }
    }

    private void shootProjectile(ServerLevel level, Player player, QAElements type, float speedMult, float damage, boolean isAwakened) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.5));
        Vec3 velocity = look.scale(3.5 * speedMult);
        MalkuthAttackType visualType = (type == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        BossUtil.malkuthCannonShoot(level, visualType, spawnPos, look, 50.0);
        PlayerCannonProjectile.summonForPlayer(level, player, spawnPos, velocity, type, damage, false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_CANNON_SHOOT.get(), SoundSource.PLAYERS, 2.0F, 1.0F + (speedMult * 0.2F));
        float shakeAmp = 1.0F * speedMult;
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().amplitude(shakeAmp).frequency(20.0F).outTime(5).build(),
                player.position(), 16.0D);
        if (!isAwakened) {
            player.push(-look.x * 0.4 * speedMult, 0.1, -look.z * 0.4 * speedMult);
        }
    }

    private void shootUltimate(ServerLevel level, Player player, QAElements type, float damage) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.5));
        Vec3 velocity = look.scale(4.5);
        MalkuthAttackType visualType = (type == QAElements.FIRE) ? MalkuthAttackType.FIRE : MalkuthAttackType.ICE;
        BossUtil.malkuthCannonShoot(level, visualType, spawnPos, look, 150.0);
        spawnPlayerChargeParticles(level, player, visualType);
        PlayerCannonProjectile.summonForPlayer(level, player, spawnPos, velocity, type, damage, true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                (SoundEvent) BossSounds.MALKUTH_VOLCANO_ERRUPTION.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().frequency(40.0F).amplitude(5.0F).inTime(2).stayTime(5).outTime(10).build(),
                player.position(), 64.0D);
        player.push(-look.x * 1.5, 0.4, -look.z * 1.5);
    }

    private void spawnPlayerChargeParticles(ServerLevel level, Player player, MalkuthAttackType type) {
        Vector3f col = MalkuthEntity.getMalkuthAttackPreparationParticleColor(type);
        BallParticleOptions options = BallParticleOptions.builder()
                .color(col.x, col.y, col.z).scalingOptions(0, 0, 15).size(0.25F).brightness(3).friction(0.8F).build();
        for (int i = 0; i < 20; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double offsetX = Math.cos(angle) * 1.5;
            double offsetZ = Math.sin(angle) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            level.sendParticles(options,
                    player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private void toggleMode(ItemStack stack, Player player) {
        int currentMode = stack.getOrDefault(ModDataComponents.MODE, 0);
        int newMode = (currentMode == 0) ? 1 : 0;
        stack.set(ModDataComponents.MODE, newMode);
        Component modeName;
        float pitch;
        if (newMode == 0) {
            modeName = Component.literal("Fire").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pitch = 0.8F;
            if (!player.level().isClientSide) {
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                        player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            }
        } else {
            modeName = Component.literal("Ice").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
            pitch = 1.2F;
            if (!player.level().isClientSide) {
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            }
        }
        player.playSound(BossSounds.BUTTON_CLICK.get(), 1.0F, pitch);
        player.displayClientMessage(Component.translatable("Mode Switched: %s", modeName), true);
    }

    private QAElements getElementFromStack(ItemStack stack) {
        int mode = stack.getOrDefault(ModDataComponents.MODE, 0);
        return (mode == 0) ? QAElements.FIRE : QAElements.ICE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (hasCore(stack)) {
            tooltip.add(Component.translatable("item.qliphoth_armaments.maximum_quietus.fuse.lore"));
        } else {
            tooltip.add(Component.translatable("item.qliphoth_armaments.maximum_quietus.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        tooltip.add(Component.empty());
        QAElements mode = getElementFromStack(stack);
        ChatFormatting color = (mode == QAElements.FIRE) ? ChatFormatting.GOLD : ChatFormatting.AQUA;
        tooltip.add(Component.translatable("Current Mode: ").append(Component.literal(mode.name()).withStyle(color)));
        tooltip.add(Component.empty());
        if (Screen.hasShiftDown()) {
            addPassiveSkill(tooltip,
                    "item.qliphoth_armaments.maximum_quietus.passive_1",
                    "item.qliphoth_armaments.maximum_quietus.passive_2");
            if (hasCore(stack)) {
                addRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse1",
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse2");
                addShiftRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill.fuse3",
                        "item.qliphoth_armaments.maximum_quietus.r_skill_2");
            } else {
                addRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.r_skill_1",
                        "item.qliphoth_armaments.maximum_quietus.r_skill_2");
                addShiftRightClickSkill(tooltip,
                        "item.qliphoth_armaments.maximum_quietus.sr_skill_1",
                        "item.qliphoth_armaments.maximum_quietus.sr_skill_2");
            }
            if (!hasCore(stack)) {
                addFuseHint(tooltip, "item.fdbosses.fire_and_ice_core");
            }
        } else {
            addPressShiftHint(tooltip);
        }
    }
}