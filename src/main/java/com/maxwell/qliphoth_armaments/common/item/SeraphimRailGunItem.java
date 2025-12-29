package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.client.BossParticles;
import com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions;
import com.finderfeed.fdbosses.client.particles.chesed_attack_ray.ChesedRayOptions;
import com.finderfeed.fdbosses.content.entities.chesed_boss.falling_block.ChesedFallingBlock;
import com.finderfeed.fdbosses.init.BossDamageSources;
import com.finderfeed.fdbosses.init.BossSounds;
import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.impact_frames.ImpactFrame;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions;
import com.finderfeed.fdlib.util.client.particles.lightning_particle.LightningParticleOptions;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class SeraphimRailGunItem extends SwordItem implements QAModWeapon {

    private static final int MAX_CHARGE_TIME = 72000;
    private static final int MIN_CHARGE_TIME = 40;
    private static final int OVER_CHARGE_TIME = 100;

    private static final int DASH_COOLDOWN = 30;
    private static final int RAILGUN_COOLDOWN = 100;
    private static final int OVERDRIVE_COOLDOWN = 300;

    public SeraphimRailGunItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // ★ 修正点 1: コンストラクタを正しい形式に修正
    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color color1 = new Color(0, 200, 255);
        Color color2 = Color.WHITE;
        Color color3 = new Color(0, 255, 255);
        return GradientTextUtil.createAnimatedGradient(translatedName, 120, color1, color2, color3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                BossSounds.CHESED_FINAL_ATTACK_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.consume(itemstack);
    }

    // ★ 修正点 2: getUseDurationの引数を修正
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player)) return;
        int duration = this.getUseDuration(stack) - count;
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false));
        }
        player.setDeltaMovement(0, Math.min(player.getDeltaMovement().y, 0), 0);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            // コアのチェックを削除
            if (duration < MIN_CHARGE_TIME) {
                if (duration % 2 == 0) {
                    spawnGatheringParticles(serverLevel, player, 1.5, 0.2F, 0.8F, 1.0F);
                }
                if (duration % 10 == 0) {
                    float pitch = 0.5F + ((float) duration / MIN_CHARGE_TIME) * 1.5F;
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5F, pitch);
                }
            }
            if (duration == MIN_CHARGE_TIME) {
                playChargeCompleteSound(level, player, 1.0F);
                spawnBurstParticles(serverLevel, player, 0.0F, 1.0F, 1.0F);
                PositionedScreenShakePacket.send(serverLevel,
                        FDShakeData.builder().amplitude(1.0F).inTime(2).stayTime(5).outTime(5).build(),
                        player.position(), 32.0D);
            }
            if (duration > MIN_CHARGE_TIME && duration < OVER_CHARGE_TIME) {
                if (duration % 3 == 0) {
                    spawnGatheringParticles(serverLevel, player, 2.0, 1.0F, 0.5F, 0.0F);
                    ArcLightningOptions arc = ArcLightningOptions.builder((ParticleType) BossParticles.ARC_LIGHTNING.get())
                            .end(player.getX() + (level.random.nextDouble() - 0.5), player.getY() + 1.2, player.getZ() + (level.random.nextDouble() - 0.5))
                            .lifetime(2).color(255, 100, 50).width(0.1F).build();
                    FDLibCalls.sendParticles(serverLevel, arc, player.position().add(0, 3, 0), 64.0D);
                }
                if (duration % 5 == 0) {
                    float pitch = 1.0F + ((float) (duration - MIN_CHARGE_TIME) / (OVER_CHARGE_TIME - MIN_CHARGE_TIME));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 0.2F, pitch);
                }
            }
            if (duration == OVER_CHARGE_TIME) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        BossSounds.CHESED_OPEN.get(), SoundSource.PLAYERS, 1.0F, 1.5F);
                spawnBurstParticles(serverLevel, player, 1.0F, 0.2F, 0.2F);
                PositionedScreenShakePacket.send(serverLevel,
                        FDShakeData.builder().amplitude(2.5F).inTime(2).stayTime(10).outTime(10).build(),
                        player.position(), 32.0D);
                player.displayClientMessage(Component.literal("LIMIT BREAKER READY").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }
        int chargeTime = this.getUseDuration(pStack) - pTimeLeft;
        if (chargeTime < MIN_CHARGE_TIME) {
            if (!pLevel.isClientSide) {
                performDashAttack((ServerLevel) pLevel, player);
            }
            player.getCooldowns().addCooldown(this, DASH_COOLDOWN);
        } else if (chargeTime < OVER_CHARGE_TIME) {
            if (!pLevel.isClientSide) {
                performLaserAttack((ServerLevel) pLevel, player, false);
            }
            player.getCooldowns().addCooldown(this, RAILGUN_COOLDOWN);
            player.swing(InteractionHand.MAIN_HAND);
        } else {
            if (!pLevel.isClientSide) {
                performLaserAttack((ServerLevel) pLevel, player, true);
            }
            player.getCooldowns().addCooldown(this, OVERDRIVE_COOLDOWN);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void performDashAttack(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        double speed = 4.0;
        Vec3 dashVec = look.scale(speed);
        player.push(dashVec.x, 0.5, dashVec.z);
        player.hurtMarked = true;
        player.invulnerableTime = 20;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 2.0F);
        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(dashVec.scale(3.0));
        // ★ 火力調整
        float damage = getScaledDamage(player, 4.0F);
        List<Entity> targets = FDHelpers.traceEntities(level, startPos, endPos, 3.0, e -> e != player && e instanceof LivingEntity);
        for (Entity e : targets) {
            if (e instanceof LivingEntity target) {
                target.hurt(BossDamageSources.chesedAttack(player), damage);
                ElementalReactionManager.applyState(target, QAElements.LIGHTNING, 200);
                Vec3 knockback = target.position().subtract(player.position()).normalize().scale(1.5);
                target.push(knockback.x, 0.5, knockback.z);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.2);
            }
        }
        for (int i = 0; i < 15; i++) {
            Vec3 p = startPos.lerp(endPos, i / 15.0);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 1, p.z, 2, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private void performLaserAttack(ServerLevel level, Player owner, boolean isOvercharge) {
        Vec3 startPos = owner.getEyePosition();
        Vec3 lookDir = owner.getLookAngle().normalize();
        double maxRange = isOvercharge ? 350.0D : 256.0D;
        Vec3 endPos = startPos.add(lookDir.scale(maxRange));
        float width = isOvercharge ? 35.0F : 25.0F;
        Color laserColor, lightningColor;
        if (isOvercharge) {
            laserColor = new Color(255, 100, 100);
            lightningColor = new Color(255, 200, 50);
        } else {
            laserColor = new Color(255, 255, 100);
            lightningColor = new Color(255, 255, 220);
        }
        ChesedRayOptions options = ChesedRayOptions.builder()
                .time(20, 30, 15).width(width).color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue())
                .lightningColor(lightningColor.getRed(), lightningColor.getGreen(), lightningColor.getBlue()).end(endPos).build();
        FDLibCalls.sendParticles(level, options, startPos, 256.0D);
        float pitch = isOvercharge ? 0.6F : 0.7F;
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), BossSounds.CHESED_FINAL_ATTACK_RAY.get(), SoundSource.PLAYERS, 3.0F, pitch);
        ImpactFrame baseFrame = new ImpactFrame(isOvercharge ? 5.0F : 3.0F, 0.2F, 20, false);
        FDLibCalls.sendImpactFrames(level, owner.position(), 256.0F, baseFrame);
        float recoil = isOvercharge ? 3.5F : 2.0F;
        PositionedScreenShakePacket.send(level, FDShakeData.builder().frequency(40.0F).amplitude(recoil + 2.0F).inTime(0).stayTime(10).outTime(20).build(), owner.position(), 64.0D);
        owner.push(-lookDir.x * recoil, 0.5, -lookDir.z * recoil);
        owner.hurtMarked = true;
        BallParticleOptions blast = BallParticleOptions.builder()
                .color(laserColor.getRed() / 255f, laserColor.getGreen() / 255f, laserColor.getBlue() / 255f)
                .scalingOptions(0, 5, 20).size(1.0F).brightness(10).build();
        level.sendParticles(blast, startPos.x + lookDir.x, startPos.y + lookDir.y, startPos.z + lookDir.z, 1, 0, 0, 0, 0);
        // ★ 火力調整
        float baseMult = isOvercharge ? 25.0F : 15.0F;
        double hitRadius = isOvercharge ? 18.0D : 12.0D;
        float damage = getScaledDamage(owner, baseMult);
        List<Entity> hitEntities = FDHelpers.traceEntities(level, startPos, endPos, hitRadius, (entity) -> !(entity instanceof Player));
        for (Entity entity : hitEntities) {
            if (entity instanceof LivingEntity living) {
                living.invulnerableTime = 0;
                ElementalReactionManager.applyState(living, QAElements.LIGHTNING, 300);
                living.hurt(BossDamageSources.chesedAttack(owner), damage);
                living.push(lookDir.x * recoil, 0.8, lookDir.z * recoil);
                LightningParticleOptions lightning = LightningParticleOptions.builder()
                        .color(lightningColor.getRed(), lightningColor.getGreen(), lightningColor.getBlue())
                        .lifetime(10).quadSize(0.5F).randomRoll(true).build();
                level.sendParticles(lightning, living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            }
        }
        net.minecraft.world.phys.BlockHitResult rayTrace = level.clip(new net.minecraft.world.level.ClipContext(startPos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, owner));
        Vec3 hitPos = rayTrace.getLocation();
        int stoneCount = isOvercharge ? 30 : 20;
        summonStonesAfterRayAttack(level, stoneCount, lookDir.reverse(), hitPos, owner);
        int destructionRadius = isOvercharge ? 7 : 5;
        Vec3 stepVec = lookDir;
        int steps = (int) maxRange;
        Vec3 currentPos = startPos;
        for (int i = 0; i < steps; i++) {
            currentPos = currentPos.add(stepVec);
            BlockPos centerPos = BlockPos.containing(currentPos);
            if (!level.getBlockState(centerPos).isAir()) {
                for (int x = -destructionRadius; x <= destructionRadius; x++) {
                    for (int y = -destructionRadius; y <= destructionRadius; y++) {
                        for (int z = -destructionRadius; z <= destructionRadius; z++) {
                            if (x * x + y * y + z * z <= destructionRadius * destructionRadius) {
                                BlockPos targetPos = centerPos.offset(x, y, z);
                                BlockState state = level.getBlockState(targetPos);
                                if (!state.isAir() && state.getDestroySpeed(level, targetPos) >= 0 && state.getDestroySpeed(level, targetPos) < 100.0f) {
                                    level.destroyBlock(targetPos, false, owner);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void summonStonesAfterRayAttack(ServerLevel level, int count, Vec3 direction, Vec3 pos, Player owner) {
        Vector3f v = (new Vector3f(0.0F, 1.0F, 0.0F)).cross((float) direction.x, (float) direction.y, (float) direction.z);
        // ★ 火力調整
        float damage = getScaledDamage(owner, 1.5F);
        for (int i = 0; i < count; ++i) {
            BlockState state = level.random.nextFloat() > 0.5F ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.SCULK.defaultBlockState();
            Vector3f add = v.rotateAxis(((float) Math.PI * 2F) * level.random.nextFloat(), (float) direction.x, (float) direction.y, (float) direction.z, new Vector3f());
            float rd = level.random.nextFloat() * 0.5F;
            ChesedFallingBlock block = ChesedFallingBlock.summon(level, state, pos, damage);
            block.setDeltaMovement(direction.add((double) (add.x * rd * 2.0F), (double) (add.y * rd), (double) (add.z * rd * 2.0F)).normalize().multiply(0.5, 2.4 - rd, 0.5));
            block.setOwner(owner);
        }
    }

    private void spawnGatheringParticles(ServerLevel level, Player player, double radius, float r, float g, float b) {
        BallParticleOptions options = BallParticleOptions.builder()
                .color(r, g, b).scalingOptions(0, 0, 5).size(0.15F).brightness(3).build();
        double angle = (level.getGameTime() * 0.5);
        double x = player.getX() + Math.cos(angle) * radius;
        double z = player.getZ() + Math.sin(angle) * radius;
        double y = player.getY() + 1.2;
        double dx = (player.getX() - x) * 0.2;
        double dy = (player.getY() + 1.2 - y) * 0.2;
        double dz = (player.getZ() - z) * 0.2;
        level.sendParticles(options, x, y, z, 0, dx, dy, dz, 1.0);
    }

    private void playChargeCompleteSound(Level level, Player player, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.0F, pitch * 2.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), BossSounds.CHESED_RAY_CHARGE.get(), SoundSource.PLAYERS, 1.0F, pitch);
    }

    private void spawnBurstParticles(ServerLevel level, Player player, float r, float g, float b) {
        BallParticleOptions burst = BallParticleOptions.builder()
                .color(r, g, b).scalingOptions(0, 0, 15).size(0.5F).brightness(10).build();
        level.sendParticles(burst, player.getX(), player.getY() + 1.5, player.getZ(), 20, 0.1, 0.1, 0.1, 0.1);
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return Math.max(1.0f, (float) (playerAttack * multiplier));
    }

    // ★ 修正点 3: getUseDurationのシグネチャを修正
    @Override
    public int getUseDuration(ItemStack pStack) {
        return MAX_CHARGE_TIME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // ★ 修正点 5: コア関連の分岐を削除
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.lore_fuse").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.charge_short").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.charge_full").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.charge_over").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.empty());
            addRightClickSkill(tooltip, "item.qliphoth_armaments.seraphim_railgun.r_skill_1", "item.qliphoth_armaments.seraphim_railgun.r_skill_2");
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.r_skill_3").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        } else {
            addPressShiftHint(tooltip);
        }
    }
}