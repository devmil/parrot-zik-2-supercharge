package com.elinext.parrotaudiosuite.bluetooth;

import android.content.Context;

/**
 * Created by michaellamers on 22/12/14.
 *
 * This is only a dummy to make the compiler for de.devmil.parrotzik2supercharge happy
 */
public class Connector {

    public static Connector getInstance(Context context)
    {
        return null;
    }

    public boolean isConnected()
    {
        return false;
    }

    public void connect()
    {}

    public int getState()
    {
        return 0;
    }

    public void sendData(String s)
    {}
    public void sendData(String s, String s1)
    {}
}
