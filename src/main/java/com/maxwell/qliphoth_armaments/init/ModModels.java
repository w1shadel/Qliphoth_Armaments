package com.maxwell.qliphoth_armaments.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModModels {
    public static final DeferredRegister<FDModelInfo> INFOS = DeferredRegister.create(FDRegistries.BEDROCK_MODEL_INFOS_KEY, QA.MOD_ID);
    public static final RegistryObject<FDModelInfo> SERAPHIM_RAILGUN = INFOS.register("seraphim_railgun",
            () -> new FDModelInfo(new ResourceLocation(QA.MOD_ID, "seraphim_railgun"), 1.0F));
    public static final RegistryObject<FDModelInfo> THE_SOVEREIGNTY = INFOS.register("the_sovereignty",
            () -> new FDModelInfo(new ResourceLocation(QA.MOD_ID, "the_sovereignty"), 1.0F));
}