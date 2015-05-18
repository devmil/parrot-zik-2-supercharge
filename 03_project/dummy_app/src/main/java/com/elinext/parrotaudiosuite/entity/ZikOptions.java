package com.elinext.parrotaudiosuite.entity;

import android.content.Context;

import com.elinext.parrotaudiosuite.xmlparser.ANCandAOC;

/**
 * Created by michaellamers on 22/12/14.
 *
 * This is only a dummy to make the compiler for de.devmil.parrotzik2supercharge happy
 */
public class ZikOptions {

    public static ZikOptions getInstance(Context context)
    {
        return null;
    }

    public int getmBatteryLevelInPercent()
    {
        return 0;
    }

    public boolean isConnected()
    {
        return false;
    }

    public void setNoiseControlEnable(boolean flag)
    {}

    public ANCandAOC getNoiseControlState()
    {
        return null;
    }

    public void setSoundEffect(boolean flag)
    {
    }

    public boolean isSoundEffect()
    {
        return false;
    }

    public int getAngleSoundEffect()
    {
        return 0;
    }

    public void setAngleSoundEffect(int i)
    {
    }

    public String getRoomSizeSoundEffect()
    {
        return "";
    }

    public void setRoomSizeSoundEffect(String s)
    {
    }
}
