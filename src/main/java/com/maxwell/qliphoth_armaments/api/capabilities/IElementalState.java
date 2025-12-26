package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.api.QAElements;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public interface IElementalState extends INBTSerializable<CompoundTag> {

    
    @Nullable
    QAElements getElement(long currentTime);

    
    void setElement(QAElements element, int duration, long currentTime);

    
    void clearElement();
}