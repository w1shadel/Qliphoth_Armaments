package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.entity.MalkuthPlayerAttackLogic;
import com.maxwell.qliphoth_armaments.common.entity.MinionElectricSphereEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, QA.MOD_ID);

    public static final RegistryObject<EntityType<ChesedCoreMinionEntity>> CHESED_CORE_MINION =
            ENTITIES.register("chesed_core_minion",
                    () -> EntityType.Builder.of(ChesedCoreMinionEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .build("chesed_core_minion"));
    public static final RegistryObject<EntityType<MinionElectricSphereEntity>> MINION_ELECTRIC_SPHERE =
            ENTITIES.register("minion_electric_sphere",
                    () -> EntityType.Builder.of(MinionElectricSphereEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("minion_electric_sphere"));
    public static final RegistryObject<EntityType<MalkuthPlayerAttackLogic>> MALKUTH_PLAYER_LOGIC =
            ENTITIES.register("malkuth_player_logic",
                    () -> EntityType.Builder.<MalkuthPlayerAttackLogic>of(MalkuthPlayerAttackLogic::new, MobCategory.MISC)
                            .sized(0.0F, 0.0F)
                            .noSave()
                            .build("malkuth_player_logic"));
}
