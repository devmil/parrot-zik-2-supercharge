package de.devmil.parrotzik2supercharge.widget.common;

public class StateTelegram {
    private boolean isConnected;
    private boolean isAncActive;
    private int batteryLevel;
    private boolean isSoundEffectActive;

    public StateTelegram(boolean isConnected, boolean isAncActive, int batteryLevel, boolean isSoundEffectActive) {
        this.isConnected = isConnected;
        this.isAncActive = isAncActive;
        this.batteryLevel = batteryLevel;
        this.isSoundEffectActive = isSoundEffectActive;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isAncActive() {
        return isAncActive;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public boolean isSoundEffectActive() {
        return isSoundEffectActive;
    }
}
