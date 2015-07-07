package de.devmil.parrotzik2supercharge.widget.events;

import de.devmil.parrotzik2supercharge.widget.common.StateTelegram;

public class ZikDataChangedEvent {
    private boolean isConnected;
    private boolean isNoiseCancellationActive;
    private int batteryLevel;
    private boolean isSoundEffectActive;

    public ZikDataChangedEvent(boolean isConnected, boolean isNoiseCancellationActive, int batteryLevel, boolean isSoundEffectActive) {
        this.isConnected = isConnected;
        this.isNoiseCancellationActive = isNoiseCancellationActive;
        this.batteryLevel = batteryLevel;
        this.isSoundEffectActive = isSoundEffectActive;
    }

    public ZikDataChangedEvent(StateTelegram stateTelegram) {
        this.isConnected = stateTelegram.isConnected();
        this.isNoiseCancellationActive = stateTelegram.isAncActive();
        this.batteryLevel = stateTelegram.getBatteryLevel();
        this.isSoundEffectActive = stateTelegram.isSoundEffectActive();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isNoiseCancellationActive() {
        return isNoiseCancellationActive;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public boolean isSoundEffectActive() {
        return isSoundEffectActive;
    }
}
