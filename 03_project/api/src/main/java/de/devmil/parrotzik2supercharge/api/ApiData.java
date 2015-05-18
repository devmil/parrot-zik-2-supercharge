package de.devmil.parrotzik2supercharge.api;

import android.os.Bundle;

/**
 * Created by michaellamers on 13/01/15.
 *
 * This class represents all data that is transmitted by the API
 */
public class ApiData {

    private static String KEY_BATTERYPERCENT = "batteryPercent";
    private static String KEY_NOISE_CONTROL = "noiseControl";
    private static String KEY_IS_CONNECTED = "isConnected";

    public ApiData(int batteryPercent,
                   NoiseControlMode noiseControlMode,
                   boolean isConnected,
                   SoundEffect soundEffect)
    {
        mBatteryPercent = batteryPercent;
        mNoiseControlMode = noiseControlMode;
        mIsConnected = isConnected;
        mSoundEffect = soundEffect;
    }

    public static ApiData fromBundle(Bundle bundle) throws Exception {
        int batteryPercent = bundle.getInt(KEY_BATTERYPERCENT);
        NoiseControlMode noiseControlMode = NoiseControlMode.fromVal(bundle.getInt(KEY_NOISE_CONTROL));
        boolean isConnected = bundle.getBoolean(KEY_IS_CONNECTED);

        SoundEffect se = SoundEffect.fromBundle(bundle);

        return new ApiData(batteryPercent, noiseControlMode, isConnected, se);
    }

    public Bundle toBundle()
    {
        Bundle result = new Bundle();

        result.putInt(KEY_BATTERYPERCENT, mBatteryPercent);
        result.putInt(KEY_NOISE_CONTROL, mNoiseControlMode.getVal());
        result.putBoolean(KEY_IS_CONNECTED, mIsConnected);

        Bundle soundEffectBundle = mSoundEffect.toBundle();
        result.putAll(soundEffectBundle);

        return result;
    }

    private int mBatteryPercent;
    private NoiseControlMode mNoiseControlMode;
    private boolean mIsConnected;
    private SoundEffect mSoundEffect;


    public int getBatteryPercent()
    {
        return mBatteryPercent;
    }

    public NoiseControlMode getNoiseControlMode()
    {
        return mNoiseControlMode;
    }

    public boolean isConnected()
    {
        return mIsConnected;
    }

    public SoundEffect getSoundEffect()
    {
        return mSoundEffect;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof ApiData))
            return false;
        ApiData castedObj = (ApiData)o;
        return castedObj.mBatteryPercent == mBatteryPercent
                && castedObj.mNoiseControlMode == mNoiseControlMode
                && castedObj.mIsConnected == mIsConnected
                && castedObj.mSoundEffect.equals(mSoundEffect);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result |= mBatteryPercent;
        result |= mNoiseControlMode.getVal();
        result |= new Boolean(mIsConnected).hashCode();
        result |= mSoundEffect.hashCode();
        return result;
    }
}
