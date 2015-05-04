package de.devmil.parrotzik2supercharge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by michaellamers on 19/01/15.
 *
 * This class represents an API client and knows what data has been transmitted last
 * Therefore this class can decide if an update should be passed to the client or not
 */
public class ApiClient {

    private ComponentName mComponentName;
    private ApiData mLastSentData;

    public ApiClient(ComponentName componentName)
    {
        mComponentName = componentName;
        mLastSentData = null;
    }

    public ComponentName getComponentName()
    {
        return mComponentName;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof ApiClient))
            return false;
        ApiClient castedObject = (ApiClient)o;
        return mComponentName.equals(castedObject.mComponentName);
    }

    @Override
    public int hashCode() {
        return mComponentName.hashCode();
    }

    public void sendIfChanged(Context context, ApiData newData)
    {
        if(!newData.equals(mLastSentData))
        {
            send(context, newData);
            mLastSentData = newData;
        }
    }

    private void send(Context context, ApiData newData)
    {
        Log.d("ApiService", String.format("Sending new data to %s", mComponentName));

        Intent intent = new Intent(ApiService.ACTION_VALUE_CHANGED);
        intent.setComponent(mComponentName);
        intent.putExtra(ApiService.ACTION_VALUE_CHANGED_EXTRA_VALUES, newData.toBundle());
        context.startService(intent);
    }

    public void resend(Context context)
    {
        if(mLastSentData != null)
            send(context, mLastSentData);
    }
}
