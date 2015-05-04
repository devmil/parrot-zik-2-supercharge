package de.devmil.parrotzik2supercharge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.elinext.parrotaudiosuite.bluetooth.Connector;
import com.elinext.parrotaudiosuite.bluetooth.ZikAPI;
import com.elinext.parrotaudiosuite.entity.ZikOptions;
import com.elinext.parrotaudiosuite.xmlparser.ANCandAOC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service is the entry point for API clients.
 * It knows all registered clients and watches the model data of the Zik parameters.
 * It handles the data updates that get transmitted to the clients
 */
public class ApiService extends Service {

    private static String ACTION_REGISTER_RECEIVER = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER";
    private static String ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER.EXTRA_COMPONENTNAME";
    private static String ACTION_REGISTER_RECEIVER_EXTRA_FORCE = "de.devmil.parrotzik2supercharge.api.action.REGISTER_RECEIVER.EXTRA_FORCE";
    private static String ACTION_UNREGISTER_RECEIVER = "de.devmil.parrotzik2supercharge.api.action.UNREGISTER_RECEIVER";

    public static String ACTION_VALUE_CHANGED = "de.devmil.parrotzik2supercharge.api.action.VALUE_CHANGED";
    public static String ACTION_VALUE_CHANGED_EXTRA_VALUES = "de.devmil.parrotzik2supercharge.api.action.VALUE_CHANGED.EXTRA_VALUES";

    private static String ACTION_SET_VALUE = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE";
    private static String ACTION_SET_VALUE_EXTRA_NAME = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE.EXTRA_NAME";
    private static String ACTION_SET_VALUE_EXTRA_VALUE = "de.devmil.parrotzik2supercharge.api.action.SET_VALUE.EXTRA_VALUE";

    private static String ACTION_SET_VALUE_EXTRA_NAME_NOISE_CONTROL = "noiseControl";

    private static String KEY_API_CLIENTS = "apiclients";


    private static int CONNECTOR_STATE_NONE = 0;
    private static int CONNECTOR_STATE_CONNECTED = 2;

    private static Connector mConnector;
    private static ZikOptions mOptions;

    private static BroadcastReceiver mInternalReceiver;
    private static Timer mPollTimer;
    private static Timer mBatteryRequestTimer;

    private static Object mInstanceLock = new Object();

    private static Map<ComponentName, ApiClient> mApiClients = new HashMap<>();
    private NoiseControlMode noiseControl;

    private boolean mLoadedState = false;

    public ApiService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        //load the registered clients if this is the first run
        if(!mLoadedState)
        {
            loadApiClients();
            mLoadedState = true;
        }

        if(intent == null)
            return result;

        Log.d("ApiService", String.format("Received action '%s'", intent.getAction()));

        //ensure that the model is connected to the Zik
        ensureConnected(this);

        if(ACTION_REGISTER_RECEIVER.equals(intent.getAction()))
        {
            //a client wants to register for updates

            String cnString = intent.getStringExtra(ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME);
            boolean force = intent.getBooleanExtra(ACTION_REGISTER_RECEIVER_EXTRA_FORCE, false);
            Log.d("ApiService", String.format("Received registration from %s, force=%b", cnString, force));

            addClient(cnString, true);

            ComponentName cn = ComponentName.unflattenFromString(cnString);

            synchronized (mInstanceLock) {
                if(force)
                    mApiClients.get(cn).resend(this);
            }

            ensureListening(this);
        }
        else if(ACTION_UNREGISTER_RECEIVER.equals(intent.getAction()))
        {
            //a client wants to unregister itself

            String cnString = intent.getStringExtra(ACTION_REGISTER_RECEIVER_EXTRA_COMPONENTNAME);
            Log.d("ApiService", String.format("Received de-registration from %s", cnString));

            ComponentName cn = ComponentName.unflattenFromString(cnString);

            synchronized (mInstanceLock) {
                if(mApiClients.containsKey(cn)) {
                    mApiClients.remove(cn);
                    dumpApiClients();
                }
            }

            ensureListening(this);
        }
        else if(ACTION_SET_VALUE.equals(intent.getAction()))
        {
            //a client wants to set a value

            String valueName = intent.getExtras().getString(ACTION_SET_VALUE_EXTRA_NAME);
            String value = intent.getExtras().getString(ACTION_SET_VALUE_EXTRA_VALUE);

            Log.d("ApiService", String.format("Got 'SET_VALUE' with %s and %s", valueName, value));

            setData(valueName, value);
        }

        return result;
    }

    /**
     * adds a client to the list of clients (if not already registered) and saves all registered clients
     * to disk (so that they are still available on a restart of the app)
     * @param componentName
     * @param dump
     */
    private void addClient(String componentName, boolean dump) {
        ComponentName cn = ComponentName.unflattenFromString(componentName);
        synchronized (mInstanceLock) {
            if (!mApiClients.containsKey(cn)) {
                ApiClient newClient = new ApiClient(cn);
                mApiClients.put(cn, newClient);
                if(dump)
                    dumpApiClients();
            }
        }
    }

    private void dumpApiClients() {
        SharedPreferences prefs = getSharedPreferences("ApiClients", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> clientSet = new HashSet<>();
        for(ComponentName clientName : mApiClients.keySet())
            clientSet.add(clientName.flattenToString());
        editor.putStringSet(KEY_API_CLIENTS, clientSet);
    }

    private void loadApiClients()
    {
        SharedPreferences prefs = getSharedPreferences("ApiClients", Context.MODE_PRIVATE);
        if(prefs.contains(KEY_API_CLIENTS))
        {
            Set<String> clientNames = prefs.getStringSet(KEY_API_CLIENTS, new HashSet<String>());
            for(String clientName : clientNames)
            {
                addClient(clientName, false);
            }
        }
    }

    /**
     * Method for setting model data upon client request
     * currently only Noise Control mode is supported
     * @param valueName
     * @param value
     */
    private void setData(String valueName, String value) {
        if (ACTION_SET_VALUE_EXTRA_NAME_NOISE_CONTROL.equals(valueName)) {
            try {
                setNoiseControl(NoiseControlMode.fromVal(Integer.parseInt(value)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        checkAsync(100);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ensureConnected(Context context)
    {
        synchronized (mInstanceLock) {
            if (mConnector == null) {
                mConnector = Connector.getInstance(context);
            }
            if (mOptions == null) {
                mOptions = ZikOptions.getInstance(context);
            }
            if (!mConnector.isConnected() && mConnector.getState() == CONNECTOR_STATE_NONE) {
                mConnector.connect();
            }
        }
    }

    /**
     * Ensures that the service is listening (or polling) for model changes
     * @param context
     */
    private void ensureListening(Context context)
    {
        synchronized (mInstanceLock) {
            if (!mApiClients.isEmpty() && mInternalReceiver == null) {
                //this one hooks into the Parrot Zik app communication mechanism to get triggered
                //whenever data changes

                Log.d("ApiService", "Registering internal receiver");
                IntentFilter filter = new IntentFilter();
                filter.addAction(ZikAPI.ACCOUNT_USERNAME_GET);
                filter.addAction(ZikAPI.AUDIO_NOISE_GET);
                filter.addAction(ZikAPI.AUDIO_PRESET_BYPASS_GET);
                filter.addAction(ZikAPI.AUDIO_PRESET_COUNTER_GET);
                filter.addAction(ZikAPI.AUDIO_PRESET_CURRENT_GET);
                filter.addAction(ZikAPI.AUDIO_SMART_TUNE_GET);

                filter.addAction(ZikAPI.AUDIO_SOURCE_GET);
                filter.addAction(ZikAPI.AUDIO_TRACK_METADATA_GET);
                filter.addAction(ZikAPI.BATTERY_GET);
                filter.addAction(ZikAPI.CONCERT_HALL_ANGLE_GET);
                filter.addAction(ZikAPI.CONCERT_HALL_ENABLED_GET);
                filter.addAction(ZikAPI.CONCERT_HALL_GET);
                filter.addAction(ZikAPI.CONCERT_HALL_ROOM_GET);
                filter.addAction(ZikAPI.EQUALIZER_ENABLED_GET);
                filter.addAction(ZikAPI.FRIENDLY_NAME_GET);
                filter.addAction(ZikAPI.NOISE_CONTROL_ENABLED_GET);
                filter.addAction(ZikAPI.NOISE_CONTROL_GET);
                filter.addAction(ZikAPI.SOFTWARE_TTS_GET);
                filter.addAction(ZikAPI.SOFTWARE_VERSION_SIP6_GET);
                filter.addAction(ZikAPI.SYSTEM_ANC_PHONE_MODE_GET);
                filter.addAction(ZikAPI.SYSTEM_AUTO_CONNECTION_GET);
                filter.addAction(ZikAPI.SYSTEM_AUTO_POWER_OFF_GET);
                filter.addAction(ZikAPI.SYSTEM_AUTO_POWER_OFF_LIST_GET);
                filter.addAction(ZikAPI.SYSTEM_BT_ADDRESS_GET);
                filter.addAction(ZikAPI.SYSTEM_COLOR_GET);
                filter.addAction(ZikAPI.SYSTEM_FLIGHT_MODE_GET);
                filter.addAction(ZikAPI.SYSTEM_HEAD_DETECTION_ENABLED_GET);
                filter.addAction(ZikAPI.SYSTEM__DEVICE_TYPE_GET);
                filter.addAction(ZikAPI.THUMB_EQUALIZER_VALUE_GET);

                filter.addAction(ZikAPI.NOISE_CONTROL_SET);
                filter.addAction(ZikAPI.THUMB_EQUALIZER_VALUE_SET);
                filter.addAction(ZikAPI.CONCERT_HALL_ANGLE_SET);
                filter.addAction(ZikAPI.CONCERT_HALL_ENABLED_SET);

                filter.addAction("com.elinext.parrotaudiosuite.service.PARROT_ACTION_CHANGE_STATE");

                Log.d("ApiService", "Got Filter, building internal receiver instance");

                mInternalReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("ApiService::InternalReceiver", String.format("Received action %s", intent.getAction()));
                        checkAsync(100);
                        ensurePolling();
                    }
                };

                context.getApplicationContext().registerReceiver(mInternalReceiver, filter);

                //because not every change of data triggers a broadcast the service has to poll additionally
                ensurePolling();
            } else if (mApiClients.isEmpty() && mInternalReceiver != null) {
                Log.d("ApiService", "No more API listener, shutting down internal receiver");
                context.getApplicationContext().unregisterReceiver(mInternalReceiver);
                mInternalReceiver = null;
                stopPolling();
            }
        }
    }

    private void stopPolling()
    {
        if(mPollTimer != null) {
            mPollTimer.cancel();
            mPollTimer.purge();
            mPollTimer = null;
        }
        if(mBatteryRequestTimer != null) {
            mBatteryRequestTimer.cancel();
            mBatteryRequestTimer.purge();
            mBatteryRequestTimer = null;
        }
    }

    private void ensurePolling()
    {
        synchronized (mInstanceLock) {
            if(mConnector == null || !mConnector.isConnected())
            {
                Log.d("ApiService", "shutting down polling because of connection loss");
                checkAsync(100);
                stopPolling();
            }
            else if(mPollTimer == null)
            {
                Log.d("ApiService", "starting polling");
                startPolling();
            }
        }
    }

    /**
     * starts the polling for data that doesn't get propagated by events
     */
    private void startPolling() {
        synchronized (mInstanceLock)
        {
            if(!mConnector.isConnected())
            {
                return;
            }
            //this timer polls directly the model.
            //this is needed when the Parrot Zik app changes data that doesn't get propagated
            //by an event (like NoiseControl Mode)
            mPollTimer = new Timer(false);
            mPollTimer.scheduleAtFixedRate(new TimerTask() {
                                               @Override
                                               public void run() {
                                                   Log.d("ApiService::PollTimer", "starting check...");
                                                   check();
                                               }
                                           },
                    0, 10000);

            //this timer triggers an battery update directly from the Zik
            //the result then gets propagated through the broadcast event system and
            //therefore leads to a data check and (if the data has been changed) to a
            //listener notification
            mBatteryRequestTimer = new Timer(false);
            mBatteryRequestTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.d("ApiService::BatteryRequestTimer", "starting battery request...");
                    if(mConnector != null
                            && mConnector.isConnected())
                        //send Battery request
                        mConnector.sendData(ZikAPI.BATTERY_GET);
                }
            },
            0, 30000);
        }
    }

    private void checkAsync(final int delayMs)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                }
                check();
            }
        }).start();
    }

    /**
     * Gets an APIData instance from the Zik model by extracting all relevant data
     * @param options
     * @return
     */
    private static ApiData dataFrom(ZikOptions options) {
        int batteryPercent = options.getmBatteryLevelInPercent();

        ANCandAOC ncs = options.getNoiseControlState();
        NoiseControlMode noiseControlMode = getNoiseControlModeFromTypeAndLevel(ncs.getType(), ncs.getValue());

        boolean isConnected = mConnector.isConnected();

        ApiData result = new ApiData(batteryPercent, noiseControlMode, isConnected);

        return result;
    }

    private static NoiseControlMode getNoiseControlModeFromTypeAndLevel(String type, int value) {
        if("off".equals(type))
            return NoiseControlMode.Disabled;
        if("anc".equals(type))
        {
            if(1 == value)
                return NoiseControlMode.NoiseCancelling1;
            return NoiseControlMode.NoiseCancelling2;
        }
        //type == "aoc"
        if(1 == value)
            return NoiseControlMode.Street1;
        return NoiseControlMode.Street2;
    }

    /**
     * Triggers a check for each client.
     * The check then compares the current ApiData with the last one that got sent to this client
     * and sends an update if necessary
     */
    private void check()
    {
        if(mConnector == null)
            return;

        synchronized (mInstanceLock) {
            ApiData currentData = dataFrom(mOptions);

            for (ApiClient client : mApiClients.values()) {
                client.sendIfChanged(this, currentData);
            }
            Log.d("ApiService", String.format("Current data: Battery=%d, NoiseControl=%s, Connected=%b", currentData.getBatteryPercent(), currentData.getNoiseControlMode(), currentData.isConnected()));
        }
    }

    /**
     * sets the Noise Control Mode based on the Api enum
     * @param noiseControl
     */
    public void setNoiseControl(NoiseControlMode noiseControl) {
        String type = "off";
        int value = 0;
        boolean enabled = false;
        switch(noiseControl)
        {
            case NoiseCancelling2:
                type = "anc";
                value = 2;
                enabled = true;
                break;
            case NoiseCancelling1:
                type = "anc";
                value = 1;
                enabled = true;
                break;
            case Disabled:
                type = "off";
                value = 0;
                enabled = false;
                break;
            case Street1:
                type = "aoc";
                value = 1;
                enabled = true;
                break;
            case Street2:
                type = "aoc";
                value = 2;
                enabled = true;
                break;
        }
        mConnector.sendData(ZikAPI.NOISE_CONTROL_SET, String.format("?arg=%s&value=%d", type, value));
        mOptions.setNoiseControlEnable(enabled);
        ANCandAOC anCandAOC = mOptions.getNoiseControlState();
        anCandAOC.setType(type);
        anCandAOC.setValue(value);
    }
}
