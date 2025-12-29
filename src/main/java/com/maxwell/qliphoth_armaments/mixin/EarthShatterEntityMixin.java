package com.maxwell.qliphoth_armaments.mixin;

import com.finderfeed.fdbosses.content.entities.chesed_boss.earthshatter_entity.EarthShatterEntity;
import com.finderfeed.fdbosses.content.entities.chesed_boss.earthshatter_entity.EarthShatterSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EarthShatterEntity.class)
public abstract class EarthShatterEntityMixin extends Entity {

    @Shadow
    public EarthShatterSettings settings;

    public EarthShatterEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"), remap = false)
    private void qliphoth_armaments_ensureSettingsNotNullOnConstruct(EntityType<?> type, Level level, CallbackInfo ci) {
        if (this.settings == null) {
            this.settings = EarthShatterSettings.builder()
                    .upTime(2)
                    .stayTime(20)
                    .downTime(10)
                    .build();
        }
    }
}