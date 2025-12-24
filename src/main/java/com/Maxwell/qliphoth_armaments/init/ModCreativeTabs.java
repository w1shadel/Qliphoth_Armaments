package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.awt.*;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, QA.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> QLIPHOTH_ARMAMENTS =
            TABS.register("qliphoth_armaments", () ->
                    CreativeModeTab.builder()
                            .title(GradientTextUtil.createGradient(
                                    "Qliphoth Armaments",
                                    new Color(0xFF4500),
                                    new Color(0x9400D3),
                                    new Color(0x00FFFF),
                                    new Color(0x00BFFF),
                                    Color.WHITE
                            ))
                            .icon(() -> new ItemStack(ModItems.CONDUCTORS_REQUIEM.get()))
                            .displayItems((params, output) -> {
                                ModItems.ITEMS.getEntries().forEach(item ->
                                        output.accept(item.get())
                                );
                            })
                            .build()
            );
}