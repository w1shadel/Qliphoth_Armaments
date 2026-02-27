package com.maxwell.qliphoth_armaments.client.render;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.client.QliphothItemRenderer;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModModels;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class TheSovereigntyRenderer {
    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new QliphothItemRenderer(
                ModItems.THE_SOVEREIGNTY,
                ModModels.THE_SOVEREIGNTY,
                QA.MOD_ID,
                "the_sovereignty/the_sovereignty")
                .setEmissive("the_sovereignty/the_sovereignty_emissive")
                .setBaseTransparent()
                .setTextureAnimation(10, 3,
                        QliphothItemRenderer.TextureAnimationMode.PING_PONG)
                .setScale(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 1.0f)
                .setVanillaTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        0f, 0.4f, 0f)
                .setThirdPersonRight(
                        0.0f, 0.3f, 0.0f,
                        0.0f, 0.0f, 0.0f)
                .setPulsatingGlow(0.2f, 0.4f)
                .setGui(0.3f, 0.0f, 0.0f, 0.6f)
                .createExtensions());
    }
}
