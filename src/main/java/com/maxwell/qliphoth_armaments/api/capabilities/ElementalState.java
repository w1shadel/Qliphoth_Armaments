package com.maxwell.qliphoth_armaments.api.capabilities;

import com.maxwell.qliphoth_armaments.api.QAElements;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ElementalState implements IElementalState {

    private static final String TAG_ELEMENT = "element";
    private static final String TAG_EXPIRE_TICK = "expire_tick";

    private QAElements currentElement = null;
    private long expireTick = 0;

    @Nullable
    @Override
    public QAElements getElement(long currentTime) {
        if (this.currentElement != null && currentTime <= this.expireTick) {
            return this.currentElement;
        }
        return null;
    }

    @Override
    public void setElement(QAElements element, int duration, long currentTime) {
        this.currentElement = element;
        this.expireTick = currentTime + duration;
    }

    @Override
    public void clearElement() {
        this.currentElement = null;
        this.expireTick = 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.currentElement != null) {
            tag.putString(TAG_ELEMENT, this.currentElement.name());
            tag.putLong(TAG_EXPIRE_TICK, this.expireTick);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(TAG_ELEMENT)) {
            this.currentElement = QAElements.valueOf(nbt.getString(TAG_ELEMENT));
            this.expireTick = nbt.getLong(TAG_EXPIRE_TICK);
        } else {
            clearElement();
        }
    }
}