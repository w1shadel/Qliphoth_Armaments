package com.maxwell.qliphoth_armaments.api.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityMiscData implements INBTSerializable<CompoundTag> {
    private int stillTimer = 0;
    private int sprintTimer = 0;
    private long lastCombatTime = 0L;
    private int sinReductionTimer = 0;
    private int sin = 0;
    private long lastDamageTime = 0L;
    private long lastEatTime = 0L;
    private int sinTimer = 0;
    private int flightTimer = 0;
    private int lastSelectedSlot = -1;

    public int getStillTimer() {
        return stillTimer;
    }

    public void setStillTimer(int val) {
        this.stillTimer = val;
    }

    public int getSprintTimer() {
        return sprintTimer;
    }

    public void setSprintTimer(int val) {
        this.sprintTimer = val;
    }

    public long getLastCombatTime() {
        return lastCombatTime;
    }

    public void setLastCombatTime(long val) {
        this.lastCombatTime = val;
    }

    public int getSinReductionTimer() {
        return sinReductionTimer;
    }

    public void setSinReductionTimer(int val) {
        this.sinReductionTimer = val;
    }

    public int getSin() {
        return sin;
    }

    public void setSin(int val) {
        this.sin = val;
    }

    public long getLastDamageTime() {
        return lastDamageTime;
    }

    public void setLastDamageTime(long val) {
        this.lastDamageTime = val;
    }

    public long getLastEatTime() {
        return lastEatTime;
    }

    public void setLastEatTime(long val) {
        this.lastEatTime = val;
    }

    public int getSinTimer() {
        return sinTimer;
    }

    public void setSinTimer(int val) {
        this.sinTimer = val;
    }

    public int getFlightTimer() {
        return flightTimer;
    }

    public void setFlightTimer(int val) {
        this.flightTimer = val;
    }

    public int getLastSelectedSlot() {
        return lastSelectedSlot;
    }

    public void setLastSelectedSlot(int val) {
        this.lastSelectedSlot = val;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("StillTimer", stillTimer);
        tag.putInt("SprintTimer", sprintTimer);
        tag.putLong("LastCombatTime", lastCombatTime);
        tag.putInt("SinReductionTimer", sinReductionTimer);
        tag.putInt("Sin", sin);
        tag.putLong("LastDamageTime", lastDamageTime);
        tag.putLong("LastEatTime", lastEatTime);
        tag.putInt("SinTimer", sinTimer);
        tag.putInt("FlightTimer", flightTimer);
        tag.putInt("LastSelectedSlot", lastSelectedSlot);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("StillTimer")) stillTimer = nbt.getInt("StillTimer");
        if (nbt.contains("SprintTimer")) sprintTimer = nbt.getInt("SprintTimer");
        if (nbt.contains("LastCombatTime")) lastCombatTime = nbt.getLong("LastCombatTime");
        if (nbt.contains("SinReductionTimer")) sinReductionTimer = nbt.getInt("SinReductionTimer");
        if (nbt.contains("Sin")) sin = nbt.getInt("Sin");
        if (nbt.contains("LastDamageTime")) lastDamageTime = nbt.getLong("LastDamageTime");
        if (nbt.contains("LastEatTime")) lastEatTime = nbt.getLong("LastEatTime");
        if (nbt.contains("SinTimer")) sinTimer = nbt.getInt("SinTimer");
        if (nbt.contains("FlightTimer")) flightTimer = nbt.getInt("FlightTimer");
        if (nbt.contains("LastSelectedSlot")) lastSelectedSlot = nbt.getInt("LastSelectedSlot");
    }
}