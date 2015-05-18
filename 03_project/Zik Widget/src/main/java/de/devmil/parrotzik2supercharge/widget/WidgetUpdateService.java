package de.devmil.parrotzik2supercharge.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import de.devmil.parrotzik2supercharge.api.ApiData;
import de.devmil.parrotzik2supercharge.api.NoiseControlMode;
import de.devmil.parrotzik2supercharge.api.IParrotZikListener;
import de.devmil.parrotzik2supercharge.api.ZikApi;

public class WidgetUpdateService extends Service {

    private static String ACTION_UPDATE = "com.elinext.zikwidget.UPDATE";
    private static String ACTION_STOP = "com.elinext.zikwidget.STOP";

    //Commands
    private static String ACTION_REFRESH = "de.devmil.parrotzik2supercharge.widget.REFRESH";
    private static String ACTION_TOGGLE_NOISE_CANCELLATION = "com.elinext.zikwidget.TOGGLE_NOISE_CANCELLATION";

    private static int NOTIFICATION_ID_STICKY = 1000;

    public WidgetUpdateService() {
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

    private static boolean showNotification(Context context)
    {
        return true;
    }

    private static boolean widgetsExist(Context context)
    {
        if(showNotification(context))
            return true;
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, BigWidget.class));
        if(appWidgetIds.length > 0)
            return true;
        return false;
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
        Intent openParrotIntent = new Intent(Intent.ACTION_MAIN);
        openParrotIntent.setComponent(new ComponentName("com.parrot.zik2", "com.elinext.parrotaudiosuite.activities.SplashActivity"));

        PendingIntent openParrotPendingIntent = PendingIntent.getActivity(this, 0, openParrotIntent, 0);

        remoteViews.setOnClickPendingIntent(parrotImageId, openParrotPendingIntent);
    }

    private void configureNoiseControlImage(RemoteViews remoteViews, int ncImageId)
    {
        remoteViews.setImageViewResource(ncImageId, getNoiseControlImage());
        Intent toggleNCIntent = new Intent(ACTION_TOGGLE_NOISE_CANCELLATION);
        toggleNCIntent.setClass(this, WidgetUpdateService.class);

        PendingIntent toggleNCPendingIntent = PendingIntent.getService(this, 0, toggleNCIntent, 0);
        remoteViews.setOnClickPendingIntent(ncImageId, toggleNCPendingIntent);
    }

    private void configureSoundEffectImage(RemoteViews remoteViews, int seImageId)
    {
        remoteViews.setImageViewResource(seImageId, getSoundEffectImage());
    }

    private void configureBatteryText(RemoteViews remoteViews, int batteryTextViewId)
    {
        int batteryPercentage = ZikApi.getBatteryPercentage();
        remoteViews.setTextViewText(batteryTextViewId, String.format("%d", batteryPercentage) + "%");
    }

    private void configureBatteryImage(RemoteViews remoteViews, int batteryImageId)
    {
        int batteryPercentage = ZikApi.getBatteryPercentage();
        remoteViews.setImageViewResource(batteryImageId, getBatteryImage(batteryPercentage));
    }

    private void configureRefreshButton(RemoteViews remoteViews, int refreshImageId)
    {
        Intent refreshIntent = new Intent(ACTION_REFRESH);
        refreshIntent.setClass(this, this.getClass());

        PendingIntent refreshPendingIntent = PendingIntent.getService(this, 0, refreshIntent, 0);
        remoteViews.setOnClickPendingIntent(refreshImageId, refreshPendingIntent);
    }

    private void configureLayouts(RemoteViews remoteViews, int llConnectedId, int llDisconnectedId)
    {
        boolean isConnected = ZikApi.isConnected();
        remoteViews.setViewVisibility(llConnectedId, isConnected ? View.VISIBLE : View.GONE);
        remoteViews.setViewVisibility(llDisconnectedId, !isConnected ? View.VISIBLE : View.GONE);
    }

    private int getBatteryImage(int batteryPercentage) {
        if(batteryPercentage > 90)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_full_white_48dp;
        if(batteryPercentage > 80)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_90_white_48dp;
        else if(batteryPercentage > 60)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_80_white_48dp;
        else if(batteryPercentage > 50)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_60_white_48dp;
        else if(batteryPercentage > 30)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_50_white_48dp;
        else if(batteryPercentage > 20)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_30_white_48dp;
        else if(batteryPercentage > 10)
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_20_white_48dp;
        else
            return de.devmil.parrotzik2supercharge.widget.R.drawable.ic_battery_alert_white_48dp;
    }

    public int getNoiseControlImage() {
        switch (ZikApi.getNoiseControlMode()) {
            case Street2:
                return de.devmil.parrotzik2supercharge.widget.R.drawable.noisecancellation_aoc;
            case NoiseCancelling2:
                return de.devmil.parrotzik2supercharge.widget.R.drawable.noisecancellation_anc;
            default:
                return 0;
        }
    }

    public int getSoundEffectImage()
    {
        if(ZikApi.getSoundEffect() != null && ZikApi.getSoundEffect().isEnabled())
            return R.drawable.soundeffect_on;
        return R.drawable.soundeffect_off;
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
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(!showNotification(this)
                || !ZikApi.isConnected())
        {
            nm.cancel(NOTIFICATION_ID_STICKY);
            mLastUpdatedNotificationData = null;
            return;
        }
        if(!force
                && mLastUpdatedNotificationData != null
                && mLastUpdatedNotificationData.equals(ZikApi.getCurrentData()))
            return;
        mLastUpdatedNotificationData = ZikApi.getCurrentData();
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Parrot Zik 2.0")
                .setContent(widgetContent)
                .setOngoing(true)
                .setSmallIcon(de.devmil.parrotzik2supercharge.widget.R.drawable.empty);

        Notification n;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MIN);
            n = builder.build();
        } else {
            n = builder.getNotification();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
        {
            n.flags |= Notification.FLAG_LOCAL_ONLY;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            n.category = Notification.CATEGORY_SERVICE;
        }

        n.flags |= Notification.FLAG_NO_CLEAR;

        if(mLastNotification != null)
            n.when = mLastNotification.when;

        nm.notify(NOTIFICATION_ID_STICKY, n);
        mLastNotification = n;
    }
}
