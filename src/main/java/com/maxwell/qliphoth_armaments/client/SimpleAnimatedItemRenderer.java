package com.maxwell.qliphoth_armaments.client;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDItemModelOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDModelItemRenderer;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDModelItemRendererOptions;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import com.finderfeed.fdlib.util.FDColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleAnimatedItemRenderer {
    public enum TextureAnimationMode {
        LOOP,
        PING_PONG
    }

    private final Item item;
    private final Supplier<FDModelInfo> modelInfoSupplier;
    private final String modid;
    private final String baseTextureName;
    private String emissiveTextureName = null;
    private boolean isBaseTranslucent = false;
    private boolean isPulsating = false;
    private float pulseSpeed = 1.0f;
    private float minAlpha = 1.0f;
    private boolean isTextureAnimated = false;
    private int frameCount = 1;
    private int ticksPerFrame = 1;
    private TextureAnimationMode animMode = TextureAnimationMode.LOOP;
    private float globalScale = 1.0f;
    private final Map<ItemDisplayContext, Vector3f> translations = new HashMap<>();
    private final Map<ItemDisplayContext, Vector3f> rotations = new HashMap<>();
    private final Map<ItemDisplayContext, Float> scales = new HashMap<>();

    public SimpleAnimatedItemRenderer(Supplier<? extends Item> item, Supplier<FDModelInfo> modelInfo, String modid, String textureName) {
        this.item = item.get();
        this.modelInfoSupplier = modelInfo;
        this.modid = modid;
        this.baseTextureName = textureName;
    }

    public SimpleAnimatedItemRenderer setEmissive(String textureName) {
        this.emissiveTextureName = textureName;
        return this;
    }

    public SimpleAnimatedItemRenderer setBaseTransparent() {
        this.isBaseTranslucent = true;
        return this;
    }

    public SimpleAnimatedItemRenderer setPulsatingGlow(float speed, float minAlpha) {
        this.isPulsating = true;
        this.pulseSpeed = speed;
        this.minAlpha = minAlpha;
        return this;
    }

    public SimpleAnimatedItemRenderer setTextureAnimation(int frameCount, int ticksPerFrame) {
        return setTextureAnimation(frameCount, ticksPerFrame, TextureAnimationMode.LOOP);
    }

    public SimpleAnimatedItemRenderer setTextureAnimation(int frameCount, int ticksPerFrame, TextureAnimationMode mode) {
        this.isTextureAnimated = true;
        this.frameCount = Math.max(1, frameCount);
        this.ticksPerFrame = Math.max(1, ticksPerFrame);
        this.animMode = mode;
        return this;
    }

    public SimpleAnimatedItemRenderer setScale(float scale) {
        this.globalScale = scale;
        return this;
    }

    public SimpleAnimatedItemRenderer setScale(ItemDisplayContext ctx, float scale) {
        this.scales.put(ctx, scale);
        return this;
    }

    public SimpleAnimatedItemRenderer setGui(float x, float y, float z, float scale) {
        translations.put(ItemDisplayContext.GUI, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.GUI, new Vector3f(30.0f, 225.0f, 0.0f));
        scales.put(ItemDisplayContext.GUI, scale);
        return this;
    }

    public SimpleAnimatedItemRenderer setFirstPersonRight(float x, float y, float z, float rotX, float rotY, float rotZ) {
        translations.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(rotX, rotY, rotZ));
        return this;
    }

    public SimpleAnimatedItemRenderer setThirdPersonRight(float x, float y, float z, float rotX, float rotY, float rotZ) {
        translations.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(rotX, rotY, rotZ));
        return this;
    }

    public SimpleAnimatedItemRenderer setVanillaTransform(ItemDisplayContext ctx, float tx, float ty, float tz) {
        float pixelToBlock = 1.0f / 16.0f;
        translations.put(ctx, new Vector3f(tx * pixelToBlock, ty * pixelToBlock, tz * pixelToBlock));
        return this;
    }

    private ResourceLocation getCurrentTexture(String baseName) {
        if (!isTextureAnimated || frameCount <= 1) {
            return ResourceLocation.tryBuild(modid, "textures/item/" + baseName + ".png");
        }
        Level level = Minecraft.getInstance().level;
        long gameTime = (level != null) ? level.getGameTime() : 0;
        long totalFramesPassed = gameTime / ticksPerFrame;
        int currentFrame = 0;
        switch (animMode) {
            case LOOP -> currentFrame = (int) (totalFramesPassed % frameCount);
            case PING_PONG -> {
                int cycleLength = (frameCount - 1) * 2;
                if (cycleLength <= 0) cycleLength = 1;
                int cyclePos = (int) (totalFramesPassed % cycleLength);
                if (cyclePos < frameCount) {
                    currentFrame = cyclePos;
                } else {
                    currentFrame = cycleLength - cyclePos;
                }
            }
        }
        return ResourceLocation.tryBuild(modid, "textures/item/" + baseName + "_" + currentFrame + ".png");
    }

    private FDColor calculatePulseColor() {
        float time = (float) System.currentTimeMillis() / 1000.0f;
        float sinVal = (float) Math.sin(time * 10.0f * this.pulseSpeed);
        float normalized = (sinVal + 1.0f) / 2.0f;
        float alpha = this.minAlpha + (normalized * (1.0f - this.minAlpha));
        return new FDColor(1.0f, 1.0f, 1.0f, alpha);
    }

    public void register(RegisterClientExtensionsEvent event) {
        var options = FDModelItemRendererOptions.create();
        options.addModel(FDItemModelOptions.builder()
                .modelInfo(this.modelInfoSupplier)
                .renderType((ctx, stack) -> {
                    ResourceLocation tex = getCurrentTexture(this.baseTextureName);
                    return this.isBaseTranslucent
                            ? RenderType.entityTranslucent(tex)
                            : RenderType.entityCutoutNoCull(tex);
                })
                .build()
        );
        if (this.emissiveTextureName != null) {
            var emissiveBuilder = FDItemModelOptions.builder()
                    .modelInfo(this.modelInfoSupplier)
                    .renderType((ctx, stack) -> {
                        ResourceLocation tex = getCurrentTexture(this.emissiveTextureName);
                        return RenderType.entityTranslucentEmissive(tex);
                    });
            if (this.isPulsating) {
                emissiveBuilder.itemColor((ctx, stack) -> calculatePulseColor());
            }
            options.addModel(emissiveBuilder.build());
        }
        options.setScale((ctx) -> this.scales.getOrDefault(ctx, this.globalScale))
                .addRotation3((ctx) -> this.rotations.getOrDefault(ctx, new Vector3f()))
                .addTranslation((ctx) -> this.translations.getOrDefault(ctx, new Vector3f()));
        event.registerItem(
                FDModelItemRenderer.createExtensions(options),
                new Item[]{this.item}
        );
    }
}