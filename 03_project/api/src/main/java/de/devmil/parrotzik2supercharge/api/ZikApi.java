package de.devmil.parrotzik2supercharge.api;

import android.content.Context;

/**
 * Created by michaellamers on 19/01/15.
 * A convenience wrapper so that the client doesn't have to use the Android Service directly
 */
public abstract class ZikApi {
    public static void start(Context context)
    {
        ApiResponseReceiver.start(context);
    }

    /**
     * add a listener for Parrot ZIK parameter changes
     * the listener gets called every time relevant data changes
     * @param listener
     */
    public static void addParrotZikListener(IParrotZikListener listener)
    {
        ApiResponseReceiver.addParrotListener(listener);
    }

    /**
     * removes a previously added listener
     * @param listener
     */
    public static void removeParrotZikListener(IParrotZikListener listener)
    {
        ApiResponseReceiver.removeParrotListener(listener);
    }

    /**
     * shuts down the API and stops receiving updates from the Parrot Zik app
     * @param context
     */
    public static void shutdown(Context context)
    {
        ApiResponseReceiver.shutdown(context);
    }

    /**
     * indicates if the Zik API is up and running
     * @return
     */
    public static boolean isRunning()
    {
        return ApiResponseReceiver.isListening();
    }

    /**
     * returns the battery level of a connected Parrot Zik in percent or -1 if there is no
     * data available
     * @return
     */
    public static int getBatteryPercentage()
    {
        ApiData data = ApiResponseReceiver.getLatestData();
        if(data != null)
            return data.getBatteryPercent();
        return -1;
    }

    /**
     * indicates if there is a Parrot Zik connected to the Parrot app
     * @return
     */
    public static boolean isConnected()
    {
        ApiData data = ApiResponseReceiver.getLatestData();
        if(data != null)
            return data.isConnected();
        return false;
    }

    /**
     * gets the currently active Noise Control mode or "Disabled" if there is no data available
     * @return
     */
    public static NoiseControlMode getNoiseControlMode()
    {
        ApiData data = ApiResponseReceiver.getLatestData();
        if(data != null)
            return data.getNoiseControlMode();
        return NoiseControlMode.Disabled;
    }

    /**
     * changes the Noise Control mode of a connected Parrot Zik 2
     * @param context
     * @param newMode
     */
    public static void setNoiseControlMode(Context context, NoiseControlMode newMode)
    {
        ApiResponseReceiver.sendNoiseControlMode(context, newMode);
    }

    /**
     * changes the Sound Effect settings for the connected Parrot Zik 2
     * @param context
     * @param soundEffect
     */
    public static void setSoundEffect(Context context, SoundEffect soundEffect)
    {
        ApiResponseReceiver.sendSoundEffect(context, soundEffect);
    }

    public static SoundEffect getSoundEffect()
    {
        ApiData data = ApiResponseReceiver.getLatestData();
        if(data != null)
            return data.getSoundEffect();
        return null;
    }

    /**
     * returns the current ApiData. This value can be null if the data hasn't been fetched yet
     * @return
     */
    public static ApiData getCurrentData()
    {
        return ApiResponseReceiver.getLatestData();
    }
}
