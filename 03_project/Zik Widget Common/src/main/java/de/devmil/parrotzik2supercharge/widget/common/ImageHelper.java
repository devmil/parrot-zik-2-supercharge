package de.devmil.parrotzik2supercharge.widget.common;

public final class ImageHelper {

    public static int getBatteryImage(int batteryPercentage) {
        if(batteryPercentage > 90)
            return R.drawable.ic_battery_full_white_48dp;
        if(batteryPercentage > 80)
            return R.drawable.ic_battery_90_white_48dp;
        else if(batteryPercentage > 60)
            return R.drawable.ic_battery_80_white_48dp;
        else if(batteryPercentage > 50)
            return R.drawable.ic_battery_60_white_48dp;
        else if(batteryPercentage > 30)
            return R.drawable.ic_battery_50_white_48dp;
        else if(batteryPercentage > 20)
            return R.drawable.ic_battery_30_white_48dp;
        else if(batteryPercentage > 10)
            return R.drawable.ic_battery_20_white_48dp;
        else
            return R.drawable.ic_battery_alert_white_48dp;
    }

    public static int getNoiseControlImage(boolean streetModeActive) {
        if(streetModeActive) {
            return R.drawable.noisecancellation_aoc;
        }
        return R.drawable.noisecancellation_anc;
    }

    public static int getSoundEffectImage(boolean soundEffectActive)
    {
        if(soundEffectActive)
            return R.drawable.soundeffect_on;
        return R.drawable.soundeffect_off;
    }
}
