package com.maxwell.qliphoth_armaments.client.render;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.client.QliphothItemRenderer;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModModels;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class SeraphimRailGunRenderer {
    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new QliphothItemRenderer(
                ModItems.SERAPHIM_RAILGUN,
                ModModels.SERAPHIM_RAILGUN,
                QA.MOD_ID,
                "seraphim_railgun")
                .setEmissive("seraphim_railgun_emissive")
                .setBaseTransparent()
                .setScale(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 1.0f)
                .setVanillaTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        0f, 0.4f, 0f)
                .setThirdPersonRight(
                        0.0f, -0.3f, 0.0f,
                        0.0f, 0.0f, 0.0f)
                .setPulsatingGlow(0.2f, 0.4f)
                .setGui(0.3f, 0.0f, 0.0f, 0.6f)
                .createExtensions());
    }
}
