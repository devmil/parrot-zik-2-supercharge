package de.devmil.parrotzik2supercharge.api;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApiResponseReceiver extends IntentService {
    private static String ACTION_REGISTER_RECEIVER = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER";
    private static String ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER.EXTRA_COMPONENTNAME";
    private static String ACTION_REGISTER_RECEIVER_EXTRA_FORCE = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER.EXTRA_FORCE";
    private static String ACTION_UNREGISTER_RECEIVER = "de.devmil.parrotzik2supercharge.api.action.UNREGISTER_RECEIVER";

    private static String ACTION_VALUE_CHANGED = "de.devmil.parrotzik2supercharge.api.action.VALUE_CHANGED";
    private static String ACTION_VALUE_CHANGED_EXTRA_VALUES = "de.devmil.parrotzik2supercharge.api.action.VALUE_CHANGED.EXTRA_VALUES";

    private static String ACTION_SET_VALUE = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE";
    private static String ACTION_SET_VALUE_EXTRA_NAME = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE.EXTRA_NAME";
    private static String ACTION_SET_VALUE_EXTRA_VALUE = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE.EXTRA_VALUE";

    private static String ACTION_SET_VALUE_EXTRA_NAME_NOISE_CONTROL = "noiseControl";

    private static int FORCE_REFRESH_TIMEOUT_MS = 1 * 60 * 1000; //1 min

    public static final String PARROT_APP_PACKAGE_NAME = "com.parrot.zik2";
    public static final String PARROT_APP_API_SERVICE_CLASS_NAME = "de.devmil.parrotzik2supercharge.ApiService";

    public ApiResponseReceiver() {
        super("Parrot Zik API Receiver");
    }

    private static boolean mIsListening = false;
    private static List<IParrotZikListener> mListener = new ArrayList<>();
    private static ApiData mLastSentData = null;
    private static Object mLock = new Object();

    private static long mLastRegistrationTimeMS = -1;

    /**
     * ensures that the API is listening to the Parrot Zik app
     *
     * @param context
     * @param force this parameter forces a re-registration. This should be used if the listening state on the Zik app side is not sure
     */
    private static void ensureListening(Context context, boolean force)
    {
        synchronized (mLock) {
            Calendar calendar = Calendar.getInstance();
            if(mLastRegistrationTimeMS < 0
                    || mLastRegistrationTimeMS + FORCE_REFRESH_TIMEOUT_MS < calendar.getTimeInMillis()) {
                force = true;
            }

            if (!force && mIsListening)
                return;

            mIsListening = true;

            registerOnApi(context, true);
            mLastRegistrationTimeMS = calendar.getTimeInMillis();
        }
    }

    /**
     * Starts the API (registers with the Zik app if it didn't already)
     * Frequent calls to this method don't harm as the registration is only
     * done once
     * @param context
     */
    public static void start(Context context)
    {
        synchronized (mLock) {
            ensureListening(context, false);
        }
    }

    /**
     * refreshes the registration with the Zik app.
     * Every call to this method triggers an Intent for the Zik app with a request for
     * registration
     * @param context
     */
    public static void refresh(Context context)
    {
        synchronized (mLock) {
            if (mIsListening)
                ensureListening(context, true);
        }
    }

    /**
     * registers a listener to this API. Every time relevant data changes the listener is called
     * @param listener
     */
    public static void addParrotListener(IParrotZikListener listener)
    {
        synchronized (mLock) {
            if (!mListener.contains(listener)) {
                mListener.add(listener);
                if (mLastSentData != null)
                    listener.onDataChanged(mLastSentData);
            }
        }
    }

    /**
     * removes an already registered listener
     * @param listener
     */
    public static void removeParrotListener(IParrotZikListener listener)
    {
        synchronized (mLock) {
            if (mListener.contains(listener))
                mListener.remove(listener);
        }
    }

    /**
     * Stops the API and deregisters it in the Zik app
     * @param context
     */
    public static void shutdown(Context context)
    {
        synchronized (mLock) {
            Intent unRegisterIntent = new Intent(ACTION_UNREGISTER_RECEIVER);
            unRegisterIntent.setClassName(PARROT_APP_PACKAGE_NAME, PARROT_APP_API_SERVICE_CLASS_NAME);
            ComponentName cn = new ComponentName(context, ApiResponseReceiver.class);
            unRegisterIntent.putExtra(ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME, cn.flattenToString());

            context.startService(unRegisterIntent);

            mIsListening = false;
        }
    }

    private static void registerOnApi(Context context, boolean force) {
        synchronized (mLock) {
            Intent registerIntent = new Intent(ACTION_REGISTER_RECEIVER);
            registerIntent.setClassName(PARROT_APP_PACKAGE_NAME, PARROT_APP_API_SERVICE_CLASS_NAME);
            ComponentName cn = new ComponentName(context, ApiResponseReceiver.class);
            registerIntent.putExtra(ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME, cn.flattenToString());
            registerIntent.putExtra(ACTION_REGISTER_RECEIVER_EXTRA_FORCE, force);

            context.startService(registerIntent);
        }
    }

    public static boolean isListening() {
        return mIsListening;
    }

    public static ApiData getLatestData()
    {
        return mLastSentData;
    }

    /**
     * This method gets called when the Zik app send an Intent to its
     * listeners. The extras of the intent contain the ApiData
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_VALUE_CHANGED.equals(action)) {
                Bundle bundle = intent.getBundleExtra(ACTION_VALUE_CHANGED_EXTRA_VALUES);
                try {
                    ApiData data = ApiData.fromBundle(bundle);
                    handleValueChanged(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleValueChanged(ApiData data) {
        synchronized (mLock) {
            Log.d("ApiServiceReceiver", "getting new data");
            if (mLastSentData == null || !data.equals(mLastSentData)) {
                Log.d("ApiServiceReceiver", "propagating new data to listeners");
                mLastSentData = data;
                for (IParrotZikListener listener : mListener)
                    listener.onDataChanged(data);
            }
        }
    }

    /**
     * sends a new Noise Control Mode to the Zik app.
     * This call doesn't change the local state of the Noise Control Mode directly.
     * Only if the Zik app processes this command and as a result calls its listeners
     * the new value is present.
     * @param context
     * @param newMode
     */
    public static void sendNoiseControlMode(Context context, NoiseControlMode newMode) {
        sendData(context, ACTION_SET_VALUE_EXTRA_NAME_NOISE_CONTROL, newMode.getVal());
    }

    private static void sendData(Context context, String valueName, int value) {
        sendData(context, valueName, Integer.toString(value));
    }

    private static void sendData(Context context, String valueName, String value) {
        Intent setValueIntent = new Intent(ACTION_SET_VALUE);
        setValueIntent.setClassName(PARROT_APP_PACKAGE_NAME, PARROT_APP_API_SERVICE_CLASS_NAME);

        setValueIntent.putExtra(ACTION_SET_VALUE_EXTRA_NAME, valueName);
        setValueIntent.putExtra(ACTION_SET_VALUE_EXTRA_VALUE, value);

        context.startService(setValueIntent);

    }
}
