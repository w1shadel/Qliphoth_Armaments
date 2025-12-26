package com.maxwell.qliphoth_armaments.client;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.init.BossModels;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRenderLayerOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRendererBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CHESED_CORE_MINION.get(),
                FDEntityRendererBuilder.builder()
                        .addLayer(FDEntityRenderLayerOptions.builder()
                                .model(BossModels.CHESED)
                                .renderType(RenderType.entityCutout(FDBosses.location("textures/entities/chesed.png")))
                                .transformation((minion, poseStack, partialTicks) -> poseStack.scale(0.3f, 0.3f, 0.3f))
                                .build())
                        .addLayer(FDEntityRenderLayerOptions.builder()
                                .model(BossModels.CHESED_CRYSTAL_LAYER)
                                .renderType(RenderType.eyes(FDBosses.location("textures/entities/chesed_crystals.png")))
                                .transformation((minion, poseStack, partialTicks) -> poseStack.scale(0.3f, 0.3f, 0.3f))
                                .build())
                        .build());
        event.registerEntityRenderer(ModEntities.MINION_ELECTRIC_SPHERE.get(),
                FDEntityRendererBuilder.builder()
                        .addLayer(FDEntityRenderLayerOptions.builder()
                                .model(BossModels.CHESED_ELECTRIC_SPHERE)
                                .renderType(RenderType.entityTranslucentCull(FDBosses.location("textures/entities/electric_orb.png")))
                                .transformation((entity, matrices, pticks) -> {
                                    matrices.translate(0.0F, 0.5F, 0.0F);
                                    float time = (float) entity.tickCount + pticks;
                                    float md = 16.0F;
                                    float scale = (float) (Mth.clamp(time / 20.0F, 0.0F, 1.0F) * (Math.sin(time * 2.0F) / md + (1.0F - 1.0F / md)));
                                    matrices.scale(scale, scale, scale);
                                })
                                .build())
                        .build());
        event.registerEntityRenderer(
                ModEntities.MALKUTH_PLAYER_LOGIC.get(),
                EmptyRenderer::new
        );
    }
}