package com.maxwell.qliphoth_armaments.client;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDItemModelOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDModelItemRenderer;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.item.FDModelItemRendererOptions;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleAnimatedItemRenderer {
    private final Item item;
    private final Supplier<FDModelInfo> modelInfoSupplier;
    private final ResourceLocation baseTexture;
    private final String modid;
    private float globalScale = 1.0f;
    private final Map<ItemDisplayContext, Vector3f> translations = new HashMap<>();
    private final Map<ItemDisplayContext, Vector3f> rotations = new HashMap<>();
    private final Map<ItemDisplayContext, Float> scales = new HashMap<>();
    private ResourceLocation emissiveTexture;

    public SimpleAnimatedItemRenderer(Supplier<? extends Item> item, Supplier<FDModelInfo> modelInfo, String modid, String textureName) {
        this.item = item.get();
        this.modelInfoSupplier = modelInfo;
        this.modid = modid;
        this.baseTexture = ResourceLocation.tryBuild(modid, "textures/item/" + textureName + ".png");
    }

    public SimpleAnimatedItemRenderer setScale(float scale) {
        this.globalScale = scale;
        return this;
    }

    public SimpleAnimatedItemRenderer setEmissive(String textureName) {
        this.emissiveTexture = ResourceLocation.tryBuild(this.modid, "textures/item/" + textureName + ".png");
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

    public SimpleAnimatedItemRenderer setGui(float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
        translations.put(ItemDisplayContext.GUI, new Vector3f(x, y, z));
        rotations.put(ItemDisplayContext.GUI, new Vector3f(rotX, rotY, rotZ));
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

    public void register(RegisterClientExtensionsEvent event) {
        // オプションビルダーを作成
        var options = FDModelItemRendererOptions.create();
        // 1層目: ベーステクスチャ (通常)
        options.addModel(FDItemModelOptions.builder()
                .modelInfo(this.modelInfoSupplier)
                .renderType((ctx, stack) -> RenderType.entityCutoutNoCull(this.baseTexture))
                .build()
        );
        // ▼ 追加: エミッシブテクスチャがある場合、2層目として重ねる
        if (this.emissiveTexture != null) {
            options.addModel(FDItemModelOptions.builder()
                    .modelInfo(this.modelInfoSupplier)
                    .renderType((ctx, stack) -> RenderType.entityTranslucentEmissive(this.emissiveTexture))
                    .build()
            );
        }
        // 共通のトランスフォーム設定
        options.setScale((ctx) -> this.scales.getOrDefault(ctx, this.globalScale))
                .addRotation3((ctx) -> this.rotations.getOrDefault(ctx, new Vector3f()))
                .addTranslation((ctx) -> this.translations.getOrDefault(ctx, new Vector3f()));
        event.registerItem(
                FDModelItemRenderer.createExtensions(options),
                new Item[]{this.item}
        );
    }
}