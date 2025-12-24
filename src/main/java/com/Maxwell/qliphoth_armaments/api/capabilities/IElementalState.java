package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.api.QAElements;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public interface IElementalState {

    @Nullable
    QAElements getElement(long currentTime);

    void setElement(QAElements element, int duration, long currentTime);

    void clearElement();

    CompoundTag serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);
}