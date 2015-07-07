package de.devmil.parrotzik2supercharge.widget.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class CommandSender {

    private static final String TAG = CommandSender.class.getSimpleName();

    private GoogleApiClient mClient;
    private Context mContext;

    public CommandSender(Context context) {
        mContext = context;
        mClient = new GoogleApiClient.Builder(context)
        .addApi(Wearable.API)
                .build();
        mClient.connect();

    }

    public void sendMessageAsync(CommandData commandData) {
        Log.d(TAG, "Start sending command");
        new AsyncTask<CommandData, Object, Boolean>() {

            @Override
            protected Boolean doInBackground(CommandData... params) {
                if(!mClient.isConnected()) {
                    Log.d(TAG, "Google client not connected => waiting for connection");
                    ConnectionResult result = mClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
                    if (!result.isSuccess()) {
                        Log.d(TAG, "Google client not connectable => stopping");
                        return false;
                    }
                }
                Log.d(TAG, "sending command to nodes");
                NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mClient).await();
                for(Node node : nodesResult.getNodes()) {
                    for(CommandData cmdData : params) {
                        byte[] bytes = cmdData.toBytes();
                        Wearable.MessageApi.sendMessage(mClient, node.getId(), DataInterface.PATH_COMMAND, bytes);
                    }
                }
                return true;
            }
        }.execute(commandData);
    }
}
