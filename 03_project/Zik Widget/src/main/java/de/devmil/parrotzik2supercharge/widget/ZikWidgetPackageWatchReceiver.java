package de.devmil.parrotzik2supercharge.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This one gets called when any package gets changed in the system.
 * It is intended to detect that the widget has been installed/updated and then
 * triggers a Widget update sequence in order to synchronize the widget state
 */
public class ZikWidgetPackageWatchReceiver extends BroadcastReceiver {
    public ZikWidgetPackageWatchReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null)
            return;
        if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_RESTARTED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction()))
        {
            Integer uid = intent.getIntExtra(Intent.EXTRA_UID, Integer.MIN_VALUE);
            String packageName = context.getPackageManager().getNameForUid(uid);

            if(context.getPackageName().equals(packageName)) {
                WidgetUpdateService.triggerUpdate(context);
            }
        }

    }
}
