package de.devmil.parrotzik2supercharge;

/**
 * Created by michaellamers on 18.05.15.
 */
public enum SoundEffectRoom {

    Unknown(""),
    SilentRoom("silent"),
    LivingRoom("living"),
    JazzClub("jazz"),
    ConcertHall("concert");

    private String mId;

    public String getId()
    {
        return mId;
    }

    SoundEffectRoom(String id)
    {
        mId = id;
    }

    public static SoundEffectRoom fromId(String id)
    {
        for(SoundEffectRoom room : SoundEffectRoom.values())
        {
            if(room.mId.equals(id))
                return room;
        }
        return Unknown;
    }
}