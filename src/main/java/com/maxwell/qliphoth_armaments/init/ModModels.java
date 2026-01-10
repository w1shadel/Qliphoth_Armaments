package com.maxwell.qliphoth_armaments.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModModels {
    public static final DeferredRegister<FDModelInfo> INFOS = DeferredRegister.create(FDRegistries.MODELS, QA.MOD_ID);
    public static final Supplier<FDModelInfo> SERAPHIM_RAILGUN = INFOS.register("seraphim_railgun",
            () -> new FDModelInfo(ResourceLocation.tryBuild(QA.MOD_ID, "seraphim_railgun"), 1.0F));
}