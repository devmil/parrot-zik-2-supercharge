package de.devmil.parrotzik2supercharge.widget.common;

import com.google.android.gms.wearable.DataMap;

public final class DataProtocol {

    private DataProtocol() {

    }

    public static StateTelegram telegramFromData(DataMap dm) {
        boolean connected = dm.getBoolean(DataInterface.CONNECTION_STATE_KEY);
        int batteryLevel = dm.getInt(DataInterface.BATTERY_LEVEL_STATE_KEY);
        boolean ancState = dm.getBoolean(DataInterface.NOISE_CANCELLATION_STATE_KEY);
        boolean soundEffectState = dm.getBoolean(DataInterface.SOUND_EFFECT_STATE_KEY);

        return new StateTelegram(connected, ancState, batteryLevel, soundEffectState);
    }

    public static void addTelegramToData(StateTelegram telegram, DataMap dm) {
        dm.putBoolean(DataInterface.CONNECTION_STATE_KEY, telegram.isConnected());
        dm.putInt(DataInterface.BATTERY_LEVEL_STATE_KEY, telegram.getBatteryLevel());
        dm.putBoolean(DataInterface.NOISE_CANCELLATION_STATE_KEY, telegram.isAncActive());
        dm.putBoolean(DataInterface.SOUND_EFFECT_STATE_KEY, telegram.isSoundEffectActive());
    }
}
