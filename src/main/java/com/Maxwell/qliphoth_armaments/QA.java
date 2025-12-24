package com.Maxwell.qliphoth_armaments;

import com.Maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.Maxwell.qliphoth_armaments.common.network.PacketHandler;
import com.Maxwell.qliphoth_armaments.init.ModCreativeTabs;
import com.Maxwell.qliphoth_armaments.init.ModEntities;
import com.Maxwell.qliphoth_armaments.init.ModItems;
import com.Maxwell.qliphoth_armaments.init.ModRecipes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QA.MOD_ID)
public class QA {

    public static final String MOD_ID = "qliphoth_armaments";

    public QA(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        PacketHandler.register();
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(this::addEntityAttributes);
    }

    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CHESED_CORE_MINION.get(), ChesedCoreMinionEntity.createAttributes().build());
        event.put(ModEntities.MINION_ELECTRIC_SPHERE.get(), ChesedCoreMinionEntity.createAttributes().build());
    }
}
