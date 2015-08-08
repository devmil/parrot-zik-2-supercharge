package de.devmil.parrotzik2supercharge.widget.common;

import android.net.NetworkInfo;

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

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!(o instanceof StateTelegram)) {
            return false;
        }
        StateTelegram castedObj = (StateTelegram)o;
        return isConnected == castedObj.isConnected
                && isAncActive == castedObj.isAncActive
                && batteryLevel == castedObj.batteryLevel
                && isSoundEffectActive == castedObj.isSoundEffectActive;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result |= Boolean.valueOf(isConnected).hashCode();
        result |= Boolean.valueOf(isAncActive).hashCode();
        result |= Integer.valueOf(batteryLevel).hashCode();
        result |= Boolean.valueOf(isSoundEffectActive).hashCode();
        return result;
    }
}
