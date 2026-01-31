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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class QliphothItemRenderer {
    public enum TextureAnimationMode {
        LOOP, PING_PONG
    }

    private final RegistryObject<Item> item;
    private final Supplier<FDModelInfo> modelInfoSupplier;
    private final String modid;
    private final String baseTextureName;
    private String emissiveTextureName = null;
    private boolean isBaseTranslucent = false;
    private boolean isTextureAnimated = false;
    private int animFrameCount = 1;
    private int animTicksPerFrame = 1;
    private TextureAnimationMode animMode = TextureAnimationMode.LOOP;
    private boolean isPulsating = false;
    private float pulseSpeed = 1.0f;
    private float minAlpha = 1.0f;
    private float globalScale = 1.0f;
    private final Map<ItemDisplayContext, Vector3f> translations = new HashMap<>();
    private final Map<ItemDisplayContext, Vector3f> rotations = new HashMap<>();
    private final Map<ItemDisplayContext, Float> scales = new HashMap<>();

    public QliphothItemRenderer(RegistryObject<Item> item, Supplier<FDModelInfo> modelInfo, String modid, String textureName) {
        this.item = item;
        this.modelInfoSupplier = modelInfo;
        this.modid = modid;
        this.baseTextureName = textureName;
    }

    public QliphothItemRenderer setEmissive(String textureName) {
        this.emissiveTextureName = textureName;
        return this;
    }

    public QliphothItemRenderer setBaseTransparent() {
        this.isBaseTranslucent = true;
        return this;
    }

    public QliphothItemRenderer setTextureAnimation(int frameCount, int ticksPerFrame, TextureAnimationMode mode) {
        this.isTextureAnimated = true;
        this.animFrameCount = Math.max(1, frameCount);
        this.animTicksPerFrame = Math.max(1, ticksPerFrame);
        this.animMode = mode;
        return this;
    }

    public QliphothItemRenderer setPulsatingGlow(float speed, float minAlpha) {
        this.isPulsating = true;
        this.pulseSpeed = speed;
        this.minAlpha = minAlpha;
        return this;
    }

    public QliphothItemRenderer setScale(float scale) {
        this.globalScale = scale;
        return this;
    }

    public QliphothItemRenderer setScale(ItemDisplayContext ctx, float scale) {
        this.scales.put(ctx, scale);
        return this;
    }

    public QliphothItemRenderer setGui(float x, float y, float z, float scale) {
        translations.put(ItemDisplayContext.GUI, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.GUI, new Vector3f(30.0f, 225.0f, 0.0f));
        scales.put(ItemDisplayContext.GUI, scale);
        return this;
    }

    public QliphothItemRenderer setThirdPersonRight(float x, float y, float z, float rotX, float rotY, float rotZ) {
        translations.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(rotX, rotY, rotZ));
        return this;
    }

    public QliphothItemRenderer setFirstPersonRight(float x, float y, float z, float rotX, float rotY, float rotZ) {
        translations.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(rotX, rotY, rotZ));
        return this;
    }

    public QliphothItemRenderer setVanillaTransform(ItemDisplayContext ctx, float tx, float ty, float tz) {
        float p = 1.0f / 16.0f;
        translations.put(ctx, new Vector3f(tx * p, ty * p, tz * p));
        return this;
    }

    private ResourceLocation getCurrentTexture(String baseName) {
        if (!isTextureAnimated || animFrameCount <= 1) {
            return new ResourceLocation(modid, "textures/item/" + baseName + ".png");
        }
        Level level = Minecraft.getInstance().level;
        long gameTime = (level != null) ? level.getGameTime() : 0;
        long totalFramesPassed = gameTime / animTicksPerFrame;
        int currentFrame = 0;
        switch (animMode) {
            case LOOP:
                currentFrame = (int) (totalFramesPassed % animFrameCount);
                break;
            case PING_PONG:
                int cycleLength = (animFrameCount - 1) * 2;
                if (cycleLength <= 0) cycleLength = 1;
                int cyclePos = (int) (totalFramesPassed % cycleLength);
                if (cyclePos < animFrameCount) {
                    currentFrame = cyclePos;
                } else {
                    currentFrame = cycleLength - cyclePos;
                }
                break;
        }
        return new ResourceLocation(modid, "textures/item/" + baseName + "_" + currentFrame + ".png");
    }

    private FDColor calculatePulseColor() {
        float time = (float) System.currentTimeMillis() / 1000.0f;
        float sinVal = (float) Math.sin(time * 10.0f * this.pulseSpeed);
        float normalized = (sinVal + 1.0f) / 2.0f;
        float alpha = this.minAlpha + (normalized * (1.0f - this.minAlpha));
        return new FDColor(1.0f, 1.0f, 1.0f, alpha);
    }

    public IClientItemExtensions createExtensions() {
        FDModelItemRendererOptions options = FDModelItemRendererOptions.create();
        options.addModel(FDItemModelOptions.builder()
                .modelInfo(this.modelInfoSupplier)
                .renderType((ctx, itemStack) -> {
                    ResourceLocation tex = getCurrentTexture(baseTextureName);
                    return isBaseTranslucent
                            ? RenderType.entityTranslucent(tex)
                            : RenderType.entityCutoutNoCull(tex);
                })
                .build()
        );
        if (this.emissiveTextureName != null) {
            var emissiveBuilder = FDItemModelOptions.builder()
                    .modelInfo(this.modelInfoSupplier)
                    .renderType((ctx, itemStack) -> {
                        ResourceLocation tex = getCurrentTexture(emissiveTextureName);
                        return RenderType.entityTranslucent(tex);
                    });
            if (this.isPulsating) {
                emissiveBuilder.itemColor((ctx, itemStack) -> calculatePulseColor());
            }
            options.addModel(emissiveBuilder.build());
        }
        options.setScale(ctx -> this.scales.getOrDefault(ctx, this.globalScale));
        options.addRotation3(ctx -> {
            Vector3f rot = this.rotations.get(ctx);
            return rot != null ? rot : new Vector3f(0, 0, 0);
        });
        options.addTranslation(ctx -> {
            Vector3f trans = this.translations.get(ctx);
            return trans != null ? trans : new Vector3f(0, 0, 0);
        });
        return FDModelItemRenderer.createExtensions(options);
    }
}