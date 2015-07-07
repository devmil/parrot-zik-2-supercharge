package de.devmil.parrotzik2supercharge.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.devmil.parrotzik2supercharge.widget.common.DataInterface;
import de.devmil.parrotzik2supercharge.widget.common.DataProtocol;
import de.devmil.parrotzik2supercharge.widget.common.StateTelegram;
import de.devmil.parrotzik2supercharge.widget.events.ZikDataChangedEvent;
import de.greenrobot.event.EventBus;

public class NotificationListenerService extends WearableListenerService {

    private static final String TAG = NotificationListenerService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1001;

    private static final String PREF_IS_NOTIFICATION_SHOWN = "isNotificationShown";
    private static final String PREF_LAST_NOTIFICATION_WHEN = "lastNotificationWhen";

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mPreferences;

    public NotificationListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = getSharedPreferences("service", Context.MODE_PRIVATE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
//        EventBus.getDefault().register(this);
    }

    private String getByteString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(byte b : bytes) {
            if(!first) {
                result.append("-");
            }
            result.append(Byte.toString(b));
            first = false;
        }
        return result.toString();
    }

    @Override
    public void onDestroy() {
//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        for(DataEvent event : events) {
            if(event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if(DataInterface.PATH_NOTIFICATION.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    updateOrShowNotification(dataMapItem);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if(DataInterface.PATH_DISMISS.equals(messageEvent.getPath())) {
            dismissNotification();
        }

    }

    private void updateOrShowNotification(DataMapItem dataMapItem) {
        StateTelegram telegram = DataProtocol.telegramFromData(dataMapItem.getDataMap());

        ActivityState activityState = new ActivityState(this);
        if(activityState.isActivityActive()) {
            EventBus.getDefault().post(new ZikDataChangedEvent(telegram));
            return;
        }

        PendingIntent pi =
                NotificationActivity.createShowPendingIntent(
                        this,
                        telegram.isConnected(),
                        telegram.isAncActive(),
                        telegram.getBatteryLevel(),
                        telegram.isSoundEffectActive());

        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .setContentInfo("Parrot Zik control")
                        .setContentText("Parrot Zik control")
                        .extend(new Notification.WearableExtender()
                                .setCustomSizePreset(Notification.WearableExtender.SIZE_FULL_SCREEN)
                                .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.parrot_logo_background))
                                .setDisplayIntent(pi));


        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Notification n = notificationBuilder.build();

        long lastWhen = mPreferences.getLong(PREF_LAST_NOTIFICATION_WHEN, 0L);

        if(lastWhen > 0) {
            n.when = lastWhen;
        }
        notificationManager.notify(NOTIFICATION_ID, n);
        mPreferences.edit()
                .putBoolean(PREF_IS_NOTIFICATION_SHOWN, true)
                .putLong(PREF_LAST_NOTIFICATION_WHEN, n.when)
                .apply();
    }

    private void dismissNotification() {
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        mPreferences.edit()
                .putBoolean(PREF_IS_NOTIFICATION_SHOWN, false)
                .putLong(PREF_LAST_NOTIFICATION_WHEN, 0L)
                .apply();
    }
}
