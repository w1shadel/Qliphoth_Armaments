package com.maxwell.qliphoth_armaments.api;

import com.finderfeed.fdbosses.packets.SlamParticlesPacket;
import com.finderfeed.fdlib.network.FDPacketHandler;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.api.capabilities.EntityMiscData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.Random;

public class SinVisualManager {
        private static final Random random = new Random();
        private static final Vector3f RED_COLOR = new Vector3f(0.8f, 0.0f, 0.1f);

        public static void spawnAwakeningAura(LivingEntity entity) {
                if (entity.level().isClientSide)
                        return;
                ServerLevel level = (ServerLevel) entity.level();
                DustParticleOptions redDust = new DustParticleOptions(RED_COLOR, 1.0F);
                for (int i = 0; i < 2; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * entity.getBbWidth() * 1.5;
                        double offsetY = random.nextDouble() * entity.getBbHeight();
                        double offsetZ = (random.nextDouble() - 0.5) * entity.getBbWidth() * 1.5;
                        level.sendParticles(redDust,
                                        entity.getX() + offsetX,
                                        entity.getY() + offsetY,
                                        entity.getZ() + offsetZ,
                                        0,
                                        0.0, 0.1, 0.0, 1.0);
                }
                if (entity.tickCount % 5 == 0) {
                        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                                        1, 0.3, 0.5, 0.3, 0.05);
                }
                if (entity.tickCount % 40 == 0) {
                        SlamParticlesPacket packet = new SlamParticlesPacket(
                                        new SlamParticlesPacket.SlamData(
                                                        entity.blockPosition(),
                                                        entity.position().add(0, 0.1, 0),
                                                        new Vec3(1.0, 0.0, 0.0))
                                                        .maxAngle((float) Math.PI * 2)
                                                        .maxSpeed(0.3F)
                                                        .collectRadius(2)
                                                        .maxParticleLifetime(20)
                                                        .count(15)
                                                        .maxVerticalSpeedCenter(0.2F));
                        FDPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
                }
        }

        public static void spawnEyeTrail(Player player) {
                if (!player.level().isClientSide)
                        return;
                int sins = player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY)
                                .map(EntityMiscData::getSin)
                                .orElse(0);
                if (sins < 7)
                        return;
                Vec3 eyePos = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 rightEyeOffset = look.cross(new Vec3(0, 1, 0)).scale(0.15);
                Vec3 particlePos = eyePos.add(rightEyeOffset);
                if (player.getDeltaMovement().lengthSqr() > 0.001 || player.yHeadRot != player.yHeadRotO) {
                        player.level().addParticle(
                                        new DustParticleOptions(RED_COLOR, 0.8F),
                                        particlePos.x, particlePos.y, particlePos.z,
                                        0.0, 0.0, 0.0);
                }
        }

        public static void sendAwakeningShockwave(Entity entity) {
                if (entity.level().isClientSide)
                        return;
                SlamParticlesPacket packet = new SlamParticlesPacket(
                                new SlamParticlesPacket.SlamData(
                                                entity.blockPosition(),
                                                entity.position().add(0, 0.1, 0),
                                                new Vec3(1.0, 0.0, 0.0))
                                                .maxAngle((float) Math.PI * 2)
                                                .maxSpeed(0.5F)
                                                .collectRadius(3)
                                                .maxParticleLifetime(40)
                                                .count(30)
                                                .maxVerticalSpeedCenter(0.5F));
                FDPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
        }

        public static void sendJudgmentEffect(Entity entity) {
                if (entity.level().isClientSide)
                        return;
                if (entity instanceof LivingEntity living) {
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                                        () -> () -> com.maxwell.qliphoth_armaments.client.ClientProxy
                                                        .spawnJudgmentStripe(living));
                }
                sendAwakeningShockwave(entity);
        }
}