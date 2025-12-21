package com.Maxwell.qliphoth_armaments;

import com.Maxwell.qliphoth_armaments.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QA.MOD_ID)
public class QA {

    public static final String MOD_ID = "qliphoth_armaments";

    public QA(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.ITEMS.register(modEventBus);
    }
}
