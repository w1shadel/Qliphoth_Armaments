package com.maxwell.qliphoth_armaments.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAnims {
    public static final DeferredRegister<Animation> ANIMATIONS = DeferredRegister.create(FDRegistries.ANIMATIONS, QA.MOD_ID);
    public static final DeferredHolder<Animation, Animation> SERAPHIM_IDLE = ANIMATIONS.register("seraphim_idle",
            () -> new Animation(ResourceLocation.tryBuild(QA.MOD_ID, "seraphim_idle")));
    public static final DeferredHolder<Animation, Animation> SERAPHIM_CHARGE = ANIMATIONS.register("seraphim_charge",
            () -> new Animation(ResourceLocation.tryBuild(QA.MOD_ID, "seraphim_charge")));
    public static final DeferredHolder<Animation, Animation> SERAPHIM_SHOOT = ANIMATIONS.register("seraphim_shoot",
            () -> new Animation(ResourceLocation.tryBuild(QA.MOD_ID, "seraphim_shoot")));
}