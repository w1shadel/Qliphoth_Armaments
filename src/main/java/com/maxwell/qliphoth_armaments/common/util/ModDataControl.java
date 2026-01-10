package com.maxwell.qliphoth_armaments.common.util;

public class ModDataControl {
    private int targetMinionCount;
    private boolean isAwakened;

    private int recoilTimer;
    private int modeToggleDelay = 0;

    public ModDataControl() {
        this.targetMinionCount = 0;
        this.isAwakened = false;
        this.recoilTimer = 0;
        this.modeToggleDelay = 0;
    }

    public int getTargetMinionCount() {
        return targetMinionCount;
    }

    public void setTargetMinionCount(int count) {
        this.targetMinionCount = count;
    }

    public boolean isAwakened() {
        return isAwakened;
    }

    public void setAwakened(boolean awakened) {
        this.isAwakened = awakened;
    }

    public void resetMinionData() {
        this.targetMinionCount = 0;
        this.isAwakened = false;
    }

    public int getModeToggleDelay() {
        return modeToggleDelay;
    }

    public void setModeToggleDelay(int delay) {
        this.modeToggleDelay = delay;
    }

    public void decrementModeToggleDelay() {
        if (this.modeToggleDelay > 0) this.modeToggleDelay--;
    }

    public int getRecoilTimer() {
        return recoilTimer;
    }

    public void setRecoilTimer(int timer) {
        this.recoilTimer = timer;
    }

    public void decrementRecoil() {
        if (this.recoilTimer > 0) this.recoilTimer--;
    }
}