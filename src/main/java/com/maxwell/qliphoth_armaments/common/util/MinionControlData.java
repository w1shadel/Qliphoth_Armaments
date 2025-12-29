package com.maxwell.qliphoth_armaments.common.util;

public class MinionControlData {
    private int targetMinionCount;
    private boolean isAwakened;

    // 追加: リコイル制御用タイマー
    private int recoilTimer;

    public MinionControlData() {
        this.targetMinionCount = 0;
        this.isAwakened = false;
        this.recoilTimer = 0;
    }

    // --- Minion関係 (Tickごとにリセットされる一時データ) ---
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

    // --- Recoil関係 (Tickを跨いで維持されるデータ) ---
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