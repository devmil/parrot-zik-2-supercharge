package de.devmil.parrotzik2supercharge;

import android.os.Bundle;

/**
 * Created by michaellamers on 18.05.15.
 */
public class SoundEffect {
    private static String KEY_IS_SOUND_EFFECT_ENABLED = "isSoundEffectEnabled";
    private static String KEY_SOUND_EFFECT_ROOM_SIZE = "soundEffectRoomSize";
    private static String KEY_SOUND_EFFECT_ANGLE = "soundEffectAngle";

    public SoundEffect(boolean isEnabled, SoundEffectRoom room, SoundEffectAngle angle)
    {
        mIsEnabled = isEnabled;
        mRoom = room;
        mAngle = angle;
    }

    private boolean mIsEnabled;
    private SoundEffectRoom mRoom;
    private SoundEffectAngle mAngle;

    public boolean isEnabled()
    {
        return mIsEnabled;
    }

    public SoundEffectRoom getRoom()
    {
        return mRoom;
    }

    public SoundEffectAngle getAngle()
    {
        return mAngle;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof SoundEffect))
            return false;
        SoundEffect castedObj = (SoundEffect)o;

        return castedObj.mAngle.getAngle() == mAngle.getAngle()
                && castedObj.mRoom.getId().equals(mRoom.getId())
                && castedObj.mIsEnabled == mIsEnabled;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result |= new Integer(mAngle.getAngle()).hashCode();
        result |= mRoom.getId().hashCode();
        result |= new Boolean(mIsEnabled).hashCode();
        return result;
    }

    public Bundle toBundle() {
        Bundle result = new Bundle();
        result.putInt(KEY_SOUND_EFFECT_ANGLE, mAngle.getAngle());
        result.putString(KEY_SOUND_EFFECT_ROOM_SIZE, mRoom.getId());
        result.putBoolean(KEY_IS_SOUND_EFFECT_ENABLED, mIsEnabled);

        return result;
    }

    public static SoundEffect fromBundle(Bundle bundle) {
        SoundEffectAngle angle = SoundEffectAngle.fromAngle(bundle.getInt(KEY_SOUND_EFFECT_ANGLE, SoundEffectAngle.Angle_120.getAngle()));
        SoundEffectRoom room = SoundEffectRoom.fromId(bundle.getString(KEY_SOUND_EFFECT_ROOM_SIZE, SoundEffectRoom.LivingRoom.getId()));
        boolean isEnabled = bundle.getBoolean(KEY_IS_SOUND_EFFECT_ENABLED, false);

        return new SoundEffect(isEnabled, room, angle);
    }

    public static SoundEffect fromString(String value) {
        if(value == null)
            return null;
        String[] values = value.split("|");
        if(values.length != 3)
            return null;
        return new SoundEffect(
                Boolean.parseBoolean(values[0]),
                SoundEffectRoom.fromId(values[1]),
                SoundEffectAngle.fromAngle(Integer.parseInt(values[2]))
        );
    }

    public String toString()
    {
        return String.format("%b|%s|%d", mIsEnabled, mRoom.getId(), mAngle.getAngle());
    }
}
