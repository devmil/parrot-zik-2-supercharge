package de.devmil.parrotzik2supercharge.widget.common;

import android.support.annotation.IntDef;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

public class CommandData {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COMMAND_TOGGLE_NOISE_CANCELLATION})
    public @interface Commands {}

    public static final int COMMAND_TOGGLE_NOISE_CANCELLATION = 1;

    private @Commands int command;
    private int param;

    public CommandData(@Commands int command, int param) {
        this.command = command;
        this.param = param;
    }

    public CommandData(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        //noinspection ResourceType
        this.command = bb.getInt(0);
        this.param = bb.getInt(1);
    }

    @Commands
    public int getCommand() {
        return command;
    }

    public int getParam() {
        return param;
    }

    public byte[] toBytes() {
        //TODO: check why the ByteBuffer didn't work
        byte[] result = new byte[8];
        result[0] = (byte)(command << 24);
        result[1] = (byte)(command << 16);
        result[2] = (byte)(command << 8);
        result[3] = (byte)(command/* << 0*/);
        result[4] = (byte)(param << 24);
        result[5] = (byte)(param << 16);
        result[6] = (byte)(param << 8);
        result[7] = (byte)(param/* << 0*/);
        return result;
    }


    @Override
    public String toString() {
        return String.format("%d, %d", command, param);
    }
}
