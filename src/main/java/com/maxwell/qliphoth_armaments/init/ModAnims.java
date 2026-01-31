package com.maxwell.qliphoth_armaments.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModAnims {
    public static final DeferredRegister<Animation> ANIMATIONS = DeferredRegister.create(FDRegistries.ANIMATIONS_KEY, QA.MOD_ID);
    public static final RegistryObject<Animation> SERAPHIM_IDLE = ANIMATIONS.register("seraphim_idle",
            () -> new Animation(new ResourceLocation(QA.MOD_ID, "seraphim_idle")));
    public static final RegistryObject<Animation> SERAPHIM_CHARGE = ANIMATIONS.register("seraphim_charge",
            () -> new Animation(new ResourceLocation(QA.MOD_ID, "seraphim_charge")));
    public static final RegistryObject<Animation> SERAPHIM_SHOOT = ANIMATIONS.register("seraphim_shoot",
            () -> new Animation(new ResourceLocation(QA.MOD_ID, "seraphim_shoot")));
}