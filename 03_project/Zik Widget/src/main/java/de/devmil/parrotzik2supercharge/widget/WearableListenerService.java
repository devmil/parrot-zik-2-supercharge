package de.devmil.parrotzik2supercharge.widget;

import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import de.devmil.parrotzik2supercharge.widget.common.CommandData;
import de.devmil.parrotzik2supercharge.widget.common.DataInterface;

public class WearableListenerService extends com.google.android.gms.wearable.WearableListenerService {

    @SuppressWarnings("FieldCanBeLocal")
    private GoogleApiClient mGoogleApiClient;

    public WearableListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(DataInterface.PATH_COMMAND.equals(messageEvent.getPath())) {
            //TODO: somehow the data is only "0" here
            CommandData cmdData = new CommandData(messageEvent.getData());
            if(cmdData.getCommand() == CommandData.COMMAND_TOGGLE_NOISE_CANCELLATION) {
                Intent toggleIntent = WidgetUpdateService.createToggleNoiseCancellationIntent(this);
                startService(toggleIntent);
            }
        }
    }
}
