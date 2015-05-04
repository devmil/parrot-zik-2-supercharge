package de.devmil.parrotzik2supercharge.api;

/**
 * Created by michaellamers on 19/01/15.
 */
public enum NoiseControlMode
{
    NoiseCancelling2(-2),
    NoiseCancelling1(-1),
    Disabled(0),
    Street1(1),
    Street2(2);

    private int mVal;

    NoiseControlMode(int val)
    {
        mVal = val;
    }

    public int getVal()
    {
        return mVal;
    }

    public static NoiseControlMode fromVal(int val) throws Exception {
        switch (val)
        {
            case -2:
                return NoiseCancelling2;
            case -1:
                return NoiseCancelling1;
            case 0:
                return Disabled;
            case 1:
                return Street1;
            case 2:
                return Street2;
        }
        throw new Exception("Invalid value for NoiseControlMode");
    }
}