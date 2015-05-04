package de.devmil.parrotzik2supercharge.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ZikAppPackageWatchReceiver extends BroadcastReceiver {
    public ZikAppPackageWatchReceiver() {
    }

    /**
     * This one gets called on each package change in Android and is used to detect an
     * installation or update of the Zik app.
     * Whenever this happens the registration with the Zik app has to be repeated
     * (The Zik app stores the registered clients but the service for handling client requests
     * and watching the Zik parameters only gets started when a client send the first intent)
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {

        if(intent == null)
            return;
        if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_RESTARTED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction()))
        {
            Integer uid = intent.getIntExtra(Intent.EXTRA_UID, Integer.MIN_VALUE);
            String packageName = context.getPackageManager().getNameForUid(uid);

            if(ApiResponseReceiver.PARROT_APP_PACKAGE_NAME.equals(packageName)) {
                Log.d("ApiServiceWidget", "detected a Parrot app change, re-registering");
                //delay the re-registration so that the Zik App has some time to start
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        //Force a re-registration on the Zik App
                        ApiResponseReceiver.refresh(context);
                    }
                }).start();
            }
        }
    }
}
