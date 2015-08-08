package de.devmil.parrotzik2supercharge.widget;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import de.devmil.parrotzik2supercharge.api.ApiData;
import de.devmil.parrotzik2supercharge.api.IParrotZikListener;
import de.devmil.parrotzik2supercharge.api.NoiseControlMode;
import de.devmil.parrotzik2supercharge.api.SoundEffect;
import de.devmil.parrotzik2supercharge.api.ZikApi;
import de.devmil.parrotzik2supercharge.widget.common.DataInterface;
import de.devmil.parrotzik2supercharge.widget.common.DataProtocol;
import de.devmil.parrotzik2supercharge.widget.common.ImageHelper;
import de.devmil.parrotzik2supercharge.widget.common.StateTelegram;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WidgetUpdateService extends Service
        implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String ACTION_UPDATE = "com.elinext.zikwidget.UPDATE";
    private static String ACTION_STOP = "com.elinext.zikwidget.STOP";

    //Commands
    private static String ACTION_REFRESH = "de.devmil.parrotzik2supercharge.widget.REFRESH";
    private static String ACTION_TOGGLE_NOISE_CANCELLATION = "com.elinext.zikwidget.TOGGLE_NOISE_CANCELLATION";

    @SuppressWarnings("FieldCanBeLocal")
    private static int NOTIFICATION_ID_STICKY = 1000;

    private GoogleApiClient mGoogleApiClient;

    public WidgetUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        if(mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    private IParrotZikListener mListener = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null)
        {
            if(ACTION_STOP.equals(intent.getAction()))
            {
                stopListening();
                stopSelf();
            }
            else if(ACTION_UPDATE.equals(intent.getAction())) {
                ensureListening();
                updateWidgets(false);
            }
            else if(ACTION_TOGGLE_NOISE_CANCELLATION.equals(intent.getAction()))
            {
                toggleNoiseCancellation();
            }
            else if(ACTION_REFRESH.equals(intent.getAction()))
            {
                ensureListening();
                updateWidgets(true);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressWarnings("UnusedParameters")
    private static boolean showNotification(Context context)
    {
        return true;
    }

    private static boolean widgetsExist(Context context)
    {
        //noinspection ConstantConditions
        if(showNotification(context))
            return true;
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, BigWidget.class));
        return appWidgetIds.length > 0;
    }

    private void updateWidgets(boolean force) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(this, BigWidget.class));
        for(int widgetId : appWidgetIds)
        {
            Log.d("ApiServiceWidget", String.format("Updating widget %d", widgetId));
            RemoteViews rv = getBigWidgetUpdate();
            widgetManager.updateAppWidget(widgetId, rv);
        }
        showNotification(getNotificationUpdate(), force);
    }

    private RemoteViews getBigWidgetUpdate() {
        RemoteViews rv = new RemoteViews(getPackageName(), de.devmil.parrotzik2supercharge.widget.R.layout.big_widget);

        configureParrotImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_imgParrotLogo);

        configureNoiseControlImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_image_noisecancellation);
        configureSoundEffectImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_imgSoundEffect);

        configureBatteryText(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_text_battery);
        configureBatteryImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_imgBattery);

        configureRefreshButton(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_imgRefresh);

        configureLayouts(rv, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_ll, de.devmil.parrotzik2supercharge.widget.R.id.big_widget_llDisconnected);

        return rv;
    }

    private RemoteViews getNotificationUpdate()
    {
        RemoteViews rv = new RemoteViews(getPackageName(), de.devmil.parrotzik2supercharge.widget.R.layout.notification);

        configureParrotImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_imgParrotLogo);

        configureNoiseControlImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_image_noisecancellation);
        configureSoundEffectImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_imgSoundEffect);

        configureBatteryText(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_text_battery);
        configureBatteryImage(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_imgBattery);

        configureLayouts(rv, de.devmil.parrotzik2supercharge.widget.R.id.notification_ll, de.devmil.parrotzik2supercharge.widget.R.id.notification_llDisconnected);

        return rv;
    }

    private void configureParrotImage(RemoteViews remoteViews, int parrotImageId)
    {
        remoteViews.setOnClickPendingIntent(parrotImageId, createOpenParrotAppPendingIntent());
    }

    private PendingIntent createOpenParrotAppPendingIntent() {
        Intent openParrotIntent = new Intent(Intent.ACTION_MAIN);
        openParrotIntent.setComponent(new ComponentName("com.parrot.zik2", "com.elinext.parrotaudiosuite.activities.SplashActivity"));

        return PendingIntent.getActivity(this, 0, openParrotIntent, 0);
    }

    private void configureNoiseControlImage(RemoteViews remoteViews, int ncImageId)
    {
        remoteViews.setImageViewResource(ncImageId, ImageHelper.getNoiseControlImage(ZikApi.getNoiseControlMode() == NoiseControlMode.Street2));
        remoteViews.setOnClickPendingIntent(ncImageId, createToggleNoiseCancellationPendingIntent(this));
    }

    public static Intent createToggleNoiseCancellationIntent(Context context) {
        Intent toggleNCIntent = new Intent(ACTION_TOGGLE_NOISE_CANCELLATION);
        toggleNCIntent.setClass(context, WidgetUpdateService.class);

        return toggleNCIntent;
    }

    public static PendingIntent createToggleNoiseCancellationPendingIntent(Context context) {

        return PendingIntent.getService(context, 0, createToggleNoiseCancellationIntent(context), 0);
    }

    private void configureSoundEffectImage(RemoteViews remoteViews, int seImageId)
    {
        SoundEffect soundEffect = ZikApi.getSoundEffect();
        boolean isActive = soundEffect != null && soundEffect.isEnabled();
        remoteViews.setImageViewResource(seImageId, ImageHelper.getSoundEffectImage(isActive));
    }

    private void configureBatteryText(RemoteViews remoteViews, int batteryTextViewId)
    {
        int batteryPercentage = ZikApi.getBatteryPercentage();
        remoteViews.setTextViewText(batteryTextViewId, String.format("%d", batteryPercentage) + "%");
    }

    private void configureBatteryImage(RemoteViews remoteViews, int batteryImageId)
    {
        int batteryPercentage = ZikApi.getBatteryPercentage();
        remoteViews.setImageViewResource(batteryImageId, ImageHelper.getBatteryImage(batteryPercentage));
    }

    private void configureRefreshButton(RemoteViews remoteViews, int refreshImageId)
    {
        remoteViews.setOnClickPendingIntent(refreshImageId, createRefreshPendingIntent());
    }

    private PendingIntent createRefreshPendingIntent() {
        Intent refreshIntent = new Intent(ACTION_REFRESH);
        refreshIntent.setClass(this, this.getClass());

        return PendingIntent.getService(this, 0, refreshIntent, 0);
    }

    private void configureLayouts(RemoteViews remoteViews, int llConnectedId, int llDisconnectedId)
    {
        boolean isConnected = ZikApi.isConnected();
        remoteViews.setViewVisibility(llConnectedId, isConnected ? View.VISIBLE : View.GONE);
        remoteViews.setViewVisibility(llDisconnectedId, !isConnected ? View.VISIBLE : View.GONE);
    }

    private void stopListening()
    {
        if(mListener == null)
            return;
        ZikApi.removeParrotZikListener(mListener);
        ZikApi.shutdown(this);
        mListener = null;
    }

    private void ensureListening() {
        if(mListener != null)
            return;
        mListener = new IParrotZikListener()
        {
            @Override
            public void onDataChanged(ApiData newData) {
                Log.d("ApiServiceWidget", "got new data. Now triggering widget update");
                triggerUpdate(WidgetUpdateService.this);
            }
        };
        ZikApi.addParrotZikListener(mListener);
        ZikApi.start(this);
    }

    private void toggleNoiseCancellation()
    {
        NoiseControlMode ncMode = ZikApi.getNoiseControlMode();
        if(ncMode == NoiseControlMode.Street2)
            ncMode = NoiseControlMode.NoiseCancelling2;
        else
            ncMode = NoiseControlMode.Street2;
        ZikApi.setNoiseControlMode(this, ncMode);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void triggerUpdate(Context context)
    {
        if(!widgetsExist(context)) {
            stop(context);
            return;
        }
        ZikApi.start(context);
        Log.d("ApiServiceWidget", "Triggering widget update");
        Intent startServiceIntent = new Intent(ACTION_UPDATE);
        startServiceIntent.setClass(context, WidgetUpdateService.class);
        context.startService(startServiceIntent);
    }

    public static void stop(Context context)
    {
        Intent startServiceIntent = new Intent(ACTION_STOP);
        startServiceIntent.setClass(context, WidgetUpdateService.class);
        context.startService(startServiceIntent);
    }

    private ApiData mLastUpdatedNotificationData = null;
    private Notification mLastNotification = null;

    private void showNotification(RemoteViews widgetContent, boolean force)
    {
        sendTelegramToWear(createTelegramFromCurrentState());
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        //noinspection ConstantConditions
        if(!showNotification(this)
                || !ZikApi.isConnected())
        {
            nm.cancel(NOTIFICATION_ID_STICKY);
            mLastUpdatedNotificationData = null;
            hideNotificationFromWear();
            return;
        }
        if(!force
                && mLastUpdatedNotificationData != null
                && mLastUpdatedNotificationData.equals(ZikApi.getCurrentData()))
            return;
        mLastUpdatedNotificationData = ZikApi.getCurrentData();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Parrot Zik 2.0")
                .setContentText("Parrot Zik 2.0 Control")
                .setContent(widgetContent)
                .setOngoing(true)
                .setLocalOnly(false)
                .setSmallIcon(de.devmil.parrotzik2supercharge.widget.R.drawable.empty);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MIN);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder.setCategory(Notification.CATEGORY_SERVICE);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification n = builder.build();

        if(mLastNotification != null)
            n.when = mLastNotification.when;

        nm.notify(NOTIFICATION_ID_STICKY, n);
        mLastNotification = n;
    }

    private StateTelegram mLastSentTelegram = null;

    private StateTelegram createTelegramFromCurrentState() {
        boolean noiseControlActive = ZikApi.getNoiseControlMode() == NoiseControlMode.Street2;
        boolean connected = ZikApi.isConnected();
        int batteryLevel = ZikApi.getBatteryPercentage();
        SoundEffect soundEffect = ZikApi.getSoundEffect();
        boolean soundEffectActive = ZikApi.getSoundEffect() != null && soundEffect != null && soundEffect.isEnabled();

        return new StateTelegram(connected, noiseControlActive, batteryLevel, soundEffectActive);
    }

    private void sendTelegramToWear(StateTelegram telegram) {
        Log.d("ApiServiceWidget", "Sending data to wear");
        if(mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {

            Log.d("ApiServiceWidget", "Wear connected....");
            //just to be sure not to use more power than needed
            if (telegram.equals(mLastSentTelegram)) {
                Log.d("ApiServiceWidget", "Skipping Wear update as the data didn't change");
                return;
            }
            PutDataMapRequest mapRequest = PutDataMapRequest.create(DataInterface.PATH_NOTIFICATION);

            DataProtocol.addTelegramToData(telegram, mapRequest.getDataMap());

            PutDataRequest request = mapRequest.asPutDataRequest();
            Log.d("ApiServiceWidget", "Posting data");
            Wearable.DataApi.putDataItem(mGoogleApiClient, request);

            mLastSentTelegram = telegram;
        }
    }

    private void hideNotificationFromWear() {
        if(mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {
            Observable<Node> nodesObservable = Observable.create(new Observable.OnSubscribe<Node>() {
                @Override
                public void call(Subscriber<? super Node> subscriber) {
                    subscriber.onStart();
                    NodeApi.GetConnectedNodesResult nodes =
                            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        subscriber.onNext(node);
                    }
                    subscriber.unsubscribe();
                }
            });
            nodesObservable.
                    map(new Func1<Node, MessageApi.SendMessageResult>() {
                        @Override
                        public MessageApi.SendMessageResult call(Node node) {
                            if(mGoogleApiClient != null
                                    && mGoogleApiClient.isConnected()) {
                                return Wearable.MessageApi.sendMessage(
                                        mGoogleApiClient, node.getId(), DataInterface.PATH_DISMISS, null).await();
                            }
                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Action1<MessageApi.SendMessageResult>() {
                        @Override
                        public void call(MessageApi.SendMessageResult result) {
                            if (result == null || !result.getStatus().isSuccess()) {
                                Log.e("ApiServiceWidget", "ERROR: failed to send Message: " + (result == null ? "NULL" : result.getStatus().toString()));
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateWidgets(false);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
