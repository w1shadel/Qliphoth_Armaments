package com.maxwell.qliphoth_armaments.client;

import com.finderfeed.fdbosses.client.particles.stripe_particle.StripeParticleOptions;
import com.finderfeed.fdlib.util.FDColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientProxy {

    private static final float RED_R = 0.8f;
    private static final float RED_G = 0.1f;
    private static final float RED_B = 0.1f;

    public static void spawnJudgmentStripe(LivingEntity entity) {
        if (Minecraft.getInstance().level == null)
            return;
        Vec3 pos = entity.position();
        FDColor startColor = new FDColor(RED_R, RED_G, RED_B, 1.0F);
        FDColor endColor = new FDColor(RED_R, 0.0F, 0.0F, 0.0F);
        StripeParticleOptions stripe = StripeParticleOptions.builder()
                .startColor(startColor)
                .endColor(endColor)
                .lifetime(20)
                .scale(0.5F)
                .stripePercentLength(1.0F)
                .offsets(new Vec3[] {
                        new Vec3(0, -2, 0),
                        new Vec3(0, 5, 0),
                        new Vec3(0.5, 8, 0.5)
                })
                .build();
        Minecraft.getInstance().level.addParticle(stripe, pos.x, pos.y, pos.z, 0, 0, 0);
    }
}
