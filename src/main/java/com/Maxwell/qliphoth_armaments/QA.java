package com.maxwell.qliphoth_armaments;

import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.init.ModCreativeTabs;
import com.maxwell.qliphoth_armaments.init.ModEntities;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(QA.MOD_ID)
public class QA {

    public static final String MOD_ID = "qliphoth_armaments";

    public QA(IEventBus modEventBus) {
        CapabilityHandler.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(this::addEntityAttributes);
    }

    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CHESED_CORE_MINION.get(), ChesedCoreMinionEntity.createAttributes().build());
        event.put(ModEntities.MINION_ELECTRIC_SPHERE.get(), ChesedCoreMinionEntity.createAttributes().build());
    }
}