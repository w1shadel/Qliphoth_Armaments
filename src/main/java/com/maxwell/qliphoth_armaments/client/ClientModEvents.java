package com.maxwell.qliphoth_armaments.client;

import com.finderfeed.fdbosses.FDBosses;
import com.finderfeed.fdbosses.init.BossModels;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRenderLayerOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRendererBuilder;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.client.gui.CurrentSinOverlay;
import com.maxwell.qliphoth_armaments.client.gui.PlayerSinsOverlay;
import com.maxwell.qliphoth_armaments.client.model.GeburahModel;
import com.maxwell.qliphoth_armaments.client.renderer.PlayerChainRenderer;
import com.maxwell.qliphoth_armaments.client.renderer.layers.PlayerHaloLayer;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GeburahModel.LAYER_LOCATION, GeburahModel::createBodyLayer);
    }

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
                com.maxwell.qliphoth_armaments.client.EmptyRenderer::new
        );
        event.registerEntityRenderer(ModEntities.PLAYER_CHANE.get(), PlayerChainRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "sins_overlay"), new PlayerSinsOverlay());
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "current_sin_overlay"), new CurrentSinOverlay());
    }

    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        PlayerRenderer defaultRenderer = event.getSkin(PlayerSkin.Model.WIDE);
        if (defaultRenderer != null) {
            defaultRenderer.addLayer(new PlayerHaloLayer(defaultRenderer));
        }
        PlayerRenderer slimRenderer = event.getSkin(PlayerSkin.Model.SLIM);
        if (slimRenderer != null) {
            slimRenderer.addLayer(new PlayerHaloLayer(slimRenderer));
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        new SimpleAnimatedItemRenderer(ModItems.SERAPHIM_RAILGUN, ModModels.SERAPHIM_RAILGUN, QA.MOD_ID, "seraphim_railgun")
                .setScale(1.0f)
                .setVanillaTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        0f, -0.2f, 0f)
                .setThirdPersonRight(
                        0.0f, -0.5f, 0.0f,
                        0.0f, 0.0f, 0.0f
                )
                .setGui(0.0f, -0.2f, 0.0f, 0.6f)
//                .setEmissive("seraphim_railgun_emissive")
                .register(event);
    }
}