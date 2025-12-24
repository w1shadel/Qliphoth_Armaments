package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.maxwell.qliphoth_armaments.common.entity.MinionElectricSphereEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, QA.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ChesedCoreMinionEntity>> CHESED_CORE_MINION =
            ENTITIES.register("chesed_core_minion",
                    () -> EntityType.Builder.of(ChesedCoreMinionEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .build("chesed_core_minion"));

    public static final DeferredHolder<EntityType<?>, EntityType<MinionElectricSphereEntity>> MINION_ELECTRIC_SPHERE =
            ENTITIES.register("minion_electric_sphere",
                    () -> EntityType.Builder.of(MinionElectricSphereEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("minion_electric_sphere"));

    public static final DeferredHolder<EntityType<?>, EntityType<MalkuthPlayerAttackLogic>> MALKUTH_PLAYER_LOGIC =
            ENTITIES.register("malkuth_player_logic",
                    () -> EntityType.Builder.<MalkuthPlayerAttackLogic>of(MalkuthPlayerAttackLogic::new, MobCategory.MISC)
                            .sized(0.0F, 0.0F)
                            .noSave()
                            .build("malkuth_player_logic"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
