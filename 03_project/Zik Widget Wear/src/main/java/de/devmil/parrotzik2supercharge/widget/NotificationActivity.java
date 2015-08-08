package de.devmil.parrotzik2supercharge.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.devmil.parrotzik2supercharge.widget.common.CommandData;
import de.devmil.parrotzik2supercharge.widget.common.CommandSender;
import de.devmil.parrotzik2supercharge.widget.common.ImageHelper;
import de.devmil.parrotzik2supercharge.widget.events.ZikDataChangedConsumedEvent;
import de.devmil.parrotzik2supercharge.widget.events.ZikDataChangedEvent;
import de.greenrobot.event.EventBus;

public class NotificationActivity extends Activity {

    private static final String TAG = NotificationActivity.class.getSimpleName();

    private static final String EXTRA_CONNECTIONSTATE = "de.devmil.parrotzik2supercharge.widget.wear.EXTRA_CONNECTIONSTATE";
    private static final String EXTRA_NOISECANCELLATION_STATE = "de.devmil.parrotzik2supercharge.widget.wear.EXTRA_NOISECANCELLATION_STATE";
    private static final String EXTRA_BATTERYLEVEL_STATE = "de.devmil.parrotzik2supercharge.widget.wear.EXTRA_BATTERYLEVEL_STATE";
    private static final String EXTRA_SOUNDEFFECT_STATE = "de.devmil.parrotzik2supercharge.widget.wear.EXTRA_SOUNDEFFECT_STATE";

    public static Intent createShowIntent(Context context, boolean connected, boolean noiseCancellationState, int batteryLevelState, boolean soundEffectState) {
        Intent result = new Intent(context, NotificationActivity.class);
        result.putExtra(EXTRA_CONNECTIONSTATE, connected);
        result.putExtra(EXTRA_NOISECANCELLATION_STATE, noiseCancellationState);
        result.putExtra(EXTRA_BATTERYLEVEL_STATE, batteryLevelState);
        result.putExtra(EXTRA_SOUNDEFFECT_STATE, soundEffectState);

        return result;
    }

    public static PendingIntent createShowPendingIntent(Context context, boolean connected, boolean noiseCancellationState, int batteryLevelState, boolean soundEffectState) {
        Intent i = createShowIntent(context, connected, noiseCancellationState, batteryLevelState, soundEffectState);

        return PendingIntent.getActivity(
                context,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private ImageView mImgNoiseCancellation;
    private ImageView mImgBattery;
    private TextView mTxtBattery;
    private ImageView mImgSoundEffect;
    private CommandSender mCommandSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating activity");
        setContentView(R.layout.notification_activity);

        mCommandSender = new CommandSender(this);

        mImgNoiseCancellation = (ImageView)findViewById(R.id.wear_notification_activity_noise_cancellation_image);
        mImgBattery = (ImageView)findViewById(R.id.wear_notification_activity_battery_status_image);
        mTxtBattery = (TextView)findViewById(R.id.wear_notification_activity_battery_status_text);
        mImgSoundEffect = (ImageView)findViewById(R.id.wear_notification_activity_soundeffect_state_image);

        Intent intent = getIntent();
        if(intent != null) {
            boolean isConnected = intent.getBooleanExtra(EXTRA_CONNECTIONSTATE, false);
            boolean ancActive = intent.getBooleanExtra(EXTRA_NOISECANCELLATION_STATE, false);
            int batteryLevel = intent.getIntExtra(EXTRA_BATTERYLEVEL_STATE, 0);
            boolean soundEffectActive = intent.getBooleanExtra(EXTRA_SOUNDEFFECT_STATE, false);

            setData(isConnected, ancActive, batteryLevel, soundEffectActive);
        }

        mImgNoiseCancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommandSender.sendMessageAsync(new CommandData(CommandData.COMMAND_TOGGLE_NOISE_CANCELLATION, 0));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming activity");
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing activity");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressWarnings("unused") //this thing gets used via EventBus
    public void onEventMainThread(ZikDataChangedEvent e) {
        Log.d(TAG, "Got data update");
        setData(e.isConnected(), e.isNoiseCancellationActive(), e.getBatteryLevel(), e.isSoundEffectActive());
        EventBus.getDefault().post(new ZikDataChangedConsumedEvent());
    }

    @SuppressWarnings("UnusedParameters")
    private void setData(boolean isConnected, boolean ancActive, int batteryLevel, boolean soundEffectActive) {
        mImgNoiseCancellation.setImageResource(ImageHelper.getNoiseControlImage(ancActive));

        mImgBattery.setImageResource(ImageHelper.getBatteryImage(batteryLevel));
        mTxtBattery.setText(Integer.toString(batteryLevel) + "%");

        mImgSoundEffect.setImageResource(ImageHelper.getSoundEffectImage(soundEffectActive));
    }
}
