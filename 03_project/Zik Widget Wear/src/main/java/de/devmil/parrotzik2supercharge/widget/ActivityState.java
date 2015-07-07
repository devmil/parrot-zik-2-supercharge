package de.devmil.parrotzik2supercharge.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class ActivityState {
    private static final String TAG = ActivityState.class.getSimpleName();

    private static final int TIMEOUT_MILLISECONDS = 2 /* min */ * 60 /* sec */ * 1000 /* msec */;
    private static final String PREF_ACTIVITY_ACTIVE = "activityActive";
    private static final String PREF_ACTIVITY_ACTIVE_VALID_FROM = "activityActiveValidFrom";

    private SharedPreferences mPreferences;

    public ActivityState(Context context) {
        mPreferences = context.getSharedPreferences("activityState", Context.MODE_PRIVATE);
    }

    public boolean isActivityActive() {
        if(!isDataValid()) {
            Log.d(TAG, "data is not valid, returning false");
            return false;
        }
        Log.d(TAG, "data is valid, reading");
        return mPreferences.getBoolean(PREF_ACTIVITY_ACTIVE, false);
    }

    public void setActivityActive(boolean isActive) {
        mPreferences.edit()
                .putBoolean(PREF_ACTIVITY_ACTIVE, isActive)
                .apply();
        notifyDataSet();
    }

    private void notifyDataSet() {
        long ms = Calendar.getInstance().getTime().getTime();
        mPreferences.edit()
                .putLong(PREF_ACTIVITY_ACTIVE_VALID_FROM, ms)
                .apply();
    }

    private boolean isDataValid() {
        long validFrom = mPreferences.getLong(PREF_ACTIVITY_ACTIVE_VALID_FROM, 0L);
        if(validFrom <= 0) {
            return false;
        }
        long diff = Calendar.getInstance().getTime().getTime() - validFrom;
        return diff < TIMEOUT_MILLISECONDS;
    }
}
