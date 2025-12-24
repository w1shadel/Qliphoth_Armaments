package com.Maxwell.qliphoth_armaments.init;

import com.Maxwell.qliphoth_armaments.QA;
import com.Maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QA.MOD_ID);

    public static final RegistryObject<CreativeModeTab> QLIPHOTH_ARMAMENTS =
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