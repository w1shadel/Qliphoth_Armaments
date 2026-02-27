package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.AnimatedItem;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDItemAnimationHandler;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDItemAnimationSystem;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDServerItemAnimations;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.animated_item.AnimatedItemStackContext;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.maxwell.qliphoth_armaments.api.ElementalReactionManager;
import com.maxwell.qliphoth_armaments.api.QAElements;
import com.maxwell.qliphoth_armaments.common.util.ClientSafeAccess;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import com.maxwell.qliphoth_armaments.init.ModAnims;
import net.minecraft.ChatFormatting;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;

public class SeraphimRailGunItem extends SwordItem implements QAModWeapon, AnimatedItem {
    private static final int FIRE_TICK = 80;
    private static final int END_TICK = 220;
    private static final int RAILGUN_COOLDOWN = 100;

    public SeraphimRailGunItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier,
            Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color color1 = new Color(0, 200, 255);
        Color color2 = Color.WHITE;
        Color color3 = new Color(0, 255, 255);
        return GradientTextUtil.createAnimatedGradient(translatedName, 120, color1, color2, color3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            FDServerItemAnimations.startItemAnimation(
                    player,
                    "ACTION",
                    AnimationTicker.builder(ModAnims.SERAPHIM_CHARGE)
                            .build(),
                    hand);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    com.finderfeed.fdbosses.init.BossSounds.CHESED_FINAL_ATTACK_CHARGE.get(), SoundSource.PLAYERS, 1.0F,
                    1.0F);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player))
            return;
        int ticksUsed = this.getUseDuration(stack) - count;
        if (level.isClientSide)
            return;
        ServerLevel serverLevel = (ServerLevel) level;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false));
        player.setDeltaMovement(0, Math.min(player.getDeltaMovement().y, 0), 0);
        if (ticksUsed < FIRE_TICK) {
            if (ticksUsed % 2 == 0) {
                spawnGatheringParticles(serverLevel, player, 1.5, 0.2F, 0.8F, 1.0F);
            }
            if (ticksUsed > 40 && ticksUsed % 10 == 0) {
                spawnArcLightning(serverLevel, player);
            }
            if (ticksUsed % 10 == 0) {
                float pitch = 0.5F + ((float) ticksUsed / FIRE_TICK) * 1.5F;
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5F, pitch);
            }
        }
        if (ticksUsed == FIRE_TICK) {
            FDServerItemAnimations.startItemAnimation(
                    player,
                    "ACTION",
                    AnimationTicker.builder(ModAnims.SERAPHIM_SHOOT)
                            .build(),
                    player.getUsedItemHand());
            performLaserAttack(serverLevel, player, stack, true);
            player.getCooldowns().addCooldown(this, RAILGUN_COOLDOWN);
            spawnBurstParticles(serverLevel, player, 0.0F, 1.0F, 1.0F);
        }
        if (ticksUsed >= END_TICK) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
    }

    @Override
    public void animatedItemTick(AnimatedItemStackContext ctx) {
        FDItemAnimationSystem animSystem = FDItemAnimationHandler.getItemAnimationSystem(ctx);
        if (animSystem != null) {
            animSystem.startAnimation("IDLE",
                    AnimationTicker.builder(ModAnims.SERAPHIM_IDLE)
                            .build());
        }
    }

    private void performLaserAttack(ServerLevel level, Player owner, ItemStack stack, boolean isOvercharge) {
        Vec3 startPos = owner.getEyePosition();
        Vec3 lookDir = owner.getLookAngle().normalize();
        double maxRange = 350.0D;
        Vec3 endPos = startPos.add(lookDir.scale(maxRange));
        float width = 35.0F;
        float r = 1.0F;
        float g = 0.4F;
        float b = 0.4F;
        com.finderfeed.fdbosses.client.particles.chesed_attack_ray.ChesedRayOptions options = com.finderfeed.fdbosses.client.particles.chesed_attack_ray.ChesedRayOptions
                .builder()
                .time(20, 30, 15)
                .width(width)
                .color(r, g, b)
                .lightningColor(1.0F, 0.8F, 0.2F)
                .end(endPos)
                .build();
        com.finderfeed.fdlib.FDLibCalls.sendParticles(level, options, startPos, 256.0D);
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                com.finderfeed.fdbosses.init.BossSounds.CHESED_FINAL_ATTACK_RAY.get(), SoundSource.PLAYERS, 3.0F, 0.6F);
        com.finderfeed.fdlib.systems.impact_frames.ImpactFrame baseFrame = new com.finderfeed.fdlib.systems.impact_frames.ImpactFrame(
                5.0F, 0.2F, 20, false);
        com.finderfeed.fdlib.FDLibCalls.sendImpactFrames(level, owner.position(), 256.0F, baseFrame);
        PositionedScreenShakePacket.send(level,
                FDShakeData.builder().frequency(40.0F).amplitude(5.5F).inTime(0).stayTime(10).outTime(20).build(),
                owner.position(), 64.0D);
        float recoil = 3.5F;
        owner.push(-lookDir.x * recoil, 0.5, -lookDir.z * recoil);
        owner.hurtMarked = true;
        com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions blast = com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions
                .builder()
                .color(r, g, b)
                .scalingOptions(0, 5, 20).size(1.0F).brightness(10).build();
        level.sendParticles(blast, startPos.x + lookDir.x, startPos.y + lookDir.y, startPos.z + lookDir.z, 1, 0, 0, 0,
                0);
        float damage = getScaledDamage(owner, 12.0F);
        double hitRadius = 18.0D;
        List<Entity> hitEntities = com.finderfeed.fdlib.FDHelpers.traceEntities(level, startPos, endPos, hitRadius,
                (entity) -> !(entity instanceof Player));
        for (Entity entity : hitEntities) {
            if (entity instanceof LivingEntity living) {
                living.invulnerableTime = 0;
                ElementalReactionManager.applyState(living, QAElements.LIGHTNING, 300);
                living.hurt(com.finderfeed.fdbosses.init.BossDamageSources.chesedAttack(owner), damage);
                living.push(lookDir.x * recoil, 0.8, lookDir.z * recoil);
                com.finderfeed.fdlib.util.client.particles.lightning_particle.LightningParticleOptions lightning = com.finderfeed.fdlib.util.client.particles.lightning_particle.LightningParticleOptions
                        .builder()
                        .color(1, 1, 1)
                        .lifetime(10).quadSize(0.5F).randomRoll(true).build();
                level.sendParticles(lightning, living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(),
                        5, 0.5, 0.5, 0.5, 0.0);
            }
        }
        net.minecraft.world.phys.BlockHitResult rayTrace = level.clip(new net.minecraft.world.level.ClipContext(
                startPos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, owner));
        Vec3 hitPos = rayTrace.getLocation();
        summonStonesAfterRayAttack(level, 30, lookDir.reverse(), hitPos, owner);
        destroyBlocksInPath(level, startPos, lookDir, (int) maxRange, owner);
    }

    private void destroyBlocksInPath(ServerLevel level, Vec3 start, Vec3 dir, int steps, Player owner) {
        int destructionRadius = 7;
        Vec3 currentPos = start;
        for (int i = 0; i < steps; i++) {
            currentPos = currentPos.add(dir);
            net.minecraft.core.BlockPos centerPos = net.minecraft.core.BlockPos.containing(currentPos);
            if (!level.getBlockState(centerPos).isAir()) {
                for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                        centerPos.offset(-destructionRadius, -destructionRadius, -destructionRadius),
                        centerPos.offset(destructionRadius, destructionRadius, destructionRadius))) {
                    if (pos.distSqr(centerPos) <= destructionRadius * destructionRadius) {
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0
                                && state.getDestroySpeed(level, pos) < 100.0f) {
                            level.destroyBlock(pos, false, owner);
                        }
                    }
                }
            }
        }
    }

    private void summonStonesAfterRayAttack(ServerLevel level, int count, Vec3 direction, Vec3 pos, Player owner) {
        Vector3f v = (new Vector3f(0.0F, 1.0F, 0.0F)).cross((float) direction.x, (float) direction.y,
                (float) direction.z);
        float damage = getScaledDamage(owner, 1.5F);
        for (int i = 0; i < count; ++i) {
            BlockState state = level.random.nextFloat() > 0.5F ? Blocks.BLACKSTONE.defaultBlockState()
                    : Blocks.SCULK.defaultBlockState();
            Vector3f add = new Vector3f(v);
            add.rotateAxis(((float) Math.PI * 2F) * level.random.nextFloat(), (float) direction.x, (float) direction.y,
                    (float) direction.z);
            float rd = level.random.nextFloat() * 0.5F;
            com.finderfeed.fdbosses.content.entities.chesed_boss.falling_block.ChesedFallingBlock block = com.finderfeed.fdbosses.content.entities.chesed_boss.falling_block.ChesedFallingBlock
                    .summon(level, state, pos, damage);
            block.setDeltaMovement(
                    direction.add((double) (add.x * rd * 2.0F), (double) (add.y * rd), (double) (add.z * rd * 2.0F))
                            .normalize().multiply(0.5, 2.4 - rd, 0.5));
            block.setOwner(owner);
        }
    }

    private void spawnGatheringParticles(ServerLevel level, Player player, double radius, float r, float g, float b) {
        com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions options = com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions
                .builder()
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

    private void spawnArcLightning(ServerLevel level, Player player) {
        com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions arc = com.finderfeed.fdbosses.client.particles.arc_lightning.ArcLightningOptions
                .builder(
                        (net.minecraft.core.particles.ParticleType) com.finderfeed.fdbosses.client.BossParticles.ARC_LIGHTNING
                                .get())
                .end(player.getX() + (level.random.nextDouble() - 0.5), player.getY() + 1.2,
                        player.getZ() + (level.random.nextDouble() - 0.5))
                .lifetime(2).color(255, 100, 50).width(0.1F).build();
        com.finderfeed.fdlib.FDLibCalls.sendParticles(level, arc, player.position().add(0, 3, 0), 64.0D);
    }

    private void spawnBurstParticles(ServerLevel level, Player player, float r, float g, float b) {
        com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions burst = com.finderfeed.fdlib.util.client.particles.ball_particle.BallParticleOptions
                .builder()
                .color(r, g, b).scalingOptions(0, 0, 15).size(0.5F).brightness(10).build();
        level.sendParticles(burst, player.getX(), player.getY() + 1.5, player.getZ(), 20, 0.1, 0.1, 0.1, 0.1);
    }

    private float getScaledDamage(Player owner, float multiplier) {
        double playerAttack = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return Math.max(1.0f, (float) (playerAttack * multiplier));
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.lore")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.charge_over")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        if (ClientSafeAccess.hasShiftDown()) {
            tooltip.add(Component.empty());
            addRightClickSkill(tooltip,
                    "item.qliphoth_armaments.seraphim_railgun.r_skill_1",
                    "item.qliphoth_armaments.seraphim_railgun.r_skill_2");
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.qliphoth_armaments.seraphim_railgun.r_skill_3")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        } else {
            addPressShiftHint(tooltip);
        }
    }

    @Override
    public void initializeClient(
            java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.maxwell.qliphoth_armaments.client.render.SeraphimRailGunRenderer.register(consumer));
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }
}
