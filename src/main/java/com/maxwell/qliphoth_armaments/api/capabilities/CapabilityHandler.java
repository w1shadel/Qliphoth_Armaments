package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.QA;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    public static final Capability<IElementalState> ELEMENTAL_STATE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation(QA.MOD_ID, "elemental_state"), new ElementalStateProvider());
        }
    }

    public static class ElementalStateProvider implements ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<IElementalState> instance = LazyOptional.of(ElementalState::new);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == ELEMENTAL_STATE_CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(() -> new IllegalArgumentException("Capability instance is not present!")).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(() -> new IllegalArgumentException("Capability instance is not present!")).deserializeNBT(nbt);
        }
    }

    @Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
            event.register(IElementalState.class);
        }
    }
}