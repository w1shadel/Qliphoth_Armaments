package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {
    public static final Capability<IElementalState> ELEMENTAL_STATE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<EntityMiscData> MISC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation(QA.MOD_ID, "elemental_state"), new SimpleProvider<>(ELEMENTAL_STATE_CAPABILITY, ElementalState::new));
            event.addCapability(new ResourceLocation(QA.MOD_ID, "misc_data"), new SimpleProvider<>(MISC_DATA_CAPABILITY, EntityMiscData::new));
        }
    }

    @Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
            event.register(IElementalState.class);
            event.register(EntityMiscData.class);
        }
    }

    public static class SimpleProvider<T extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {
        private final Capability<T> capability;
        private final LazyOptional<T> instance;

        public SimpleProvider(Capability<T> capability, NonNullSupplier<T> factory) {
            this.capability = capability;
            this.instance = LazyOptional.of(factory);
        }

        @Nonnull
        @Override
        public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> cap, @Nullable Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(() -> new IllegalArgumentException("Instance missing")).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(() -> new IllegalArgumentException("Instance missing")).deserializeNBT(nbt);
        }
    }
}