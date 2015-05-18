package de.devmil.parrotzik2supercharge;

/**
 * Created by michaellamers on 18.05.15.
 */
public enum SoundEffectAngle {
    Angle_030(30),
    Angle_060(60),
    Angle_090(90),
    Angle_120(120),
    Angle_150(150),
    Angle_180(180);

    private int mAngle;

    public int getAngle()
    {
        return mAngle;
    }

    SoundEffectAngle(int angle)
    {
        mAngle = angle;
    }

    public static SoundEffectAngle fromAngle(int angle)
    {
        if(angle < Angle_030.getAngle())
            return Angle_030;
        if(angle < Angle_060.getAngle())
            return Angle_060;
        if(angle < Angle_090.getAngle())
            return Angle_090;
        if(angle < Angle_120.getAngle())
            return Angle_120;
        if(angle < Angle_150.getAngle())
            return Angle_150;
        return Angle_180;
    }
}