package com.Maxwell.qliphoth_armaments.api.capabilities;

import com.Maxwell.qliphoth_armaments.api.QAElements;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public interface IElementalState extends INBTSerializable<CompoundTag> {

    /**
     * 現在付与されているエレメントを取得します。
     *
     * @param currentTime 現在のワールドtick
     * @return 付与されているエレメント。なければ null
     */
    @Nullable
    QAElements getElement(long currentTime);

    /**
     * 新しいエレメントを付与します。
     *
     * @param element     付与するエレメント
     * @param duration    持続時間 (tick)
     * @param currentTime 現在のワールドtick
     */
    void setElement(QAElements element, int duration, long currentTime);

    /**
     * 付与されているエレメントをクリアします。
     */
    void clearElement();
}