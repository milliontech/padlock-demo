package com.example.padlockdemo.model;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command {
    private byte head;
    private byte cmd;
    private byte[] data1;
    private byte[] data2;
    private byte[] token;

    public Command(byte head, byte cmd) {
        this.head = head;
        this.cmd = cmd;
    }

    public Command(byte head, byte cmd, byte[] data1, byte[] data2) {
        this.head = head;
        this.cmd = cmd;
        this.data1 = data1;
        this.data2 = data2;
    }

    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte getLen() {
        int length = (this.data1 != null ? this.data1.length : 0) + (this.data2 != null ? this.data2.length : 0);
        return new Integer(length).byteValue();
    }

    public byte[] getData1() {
        return data1;
    }

    public void setData1(byte[] data1) {
        this.data1 = data1;
    }

    public byte[] getData2() {
        return data2;
    }

    public void setData2(byte[] data2) {
        this.data2 = data2;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public byte[] getCommandData() {
        List<Byte> bytes = new ArrayList<>();
        bytes.add(head);
        bytes.add(cmd);
        bytes.add(getLen());
        for (byte b: data1) {
            bytes.add(b);
        }
        for (byte b: data2) {
            bytes.add(b);
        }
        for (byte b: token) {
            bytes.add(b);
        }
        return Bytes.toArray(bytes);
    }

    public String getCommandString() {
        byte[] bytes = getCommandData();
        return BaseEncoding.base16().encode(bytes);
    }

    public static final byte HEAD_REQUEST = 0x00;
    public static final byte HEAD_RESPONSE = 0x01;

    public static final byte CMD_UNLOCK = 0x00;
    // C102 only
    public static final byte CMD_LOOK_UP_NOTIFICATION = 0x01;
    public static final byte CMD_SET_WORK_MODE = 0x02;
    //
    public static final byte CMD_QUERY_UNLOCK_TIMES = 0x05;
    public static final byte CMD_QUERY_SW_VERSION = 0x08;
    public static final byte CMD_QUERY_LOCK_BEAM_STATUS = 0x09;
    public static final byte CMD_QUERY_POWER_PERCENTAGE = 0x0A;
    public static final byte CMD_CHANGE_TOKEN = 0x0B;
    public static final byte CMD_CHANGE_KEY = 0x0C;

    public static final byte RESULT_SUCCESS = 0x00;
    public static final byte RESULT_FAIL = 0x01;

    public static final byte BYTE_ZERO = 0x00;
    public static final byte BYTE_ONE = 0x01;
    public static final byte[] BYTES_EMPTY = new byte[] { };
    public static final byte[] BYTES_ZERO = new byte[] { BYTE_ZERO };
    public static final byte[] BYTES_ONE = new byte[] { BYTE_ONE };

    public static final byte DATA_IDLE_MODE = 0x00;
    public static final byte DATA_ENERGY_MODE = 0x01;
    public static final byte DATA_NORMAL_MODE = 0x02;

    public static Command unlock = new Command(
            HEAD_REQUEST,
            CMD_UNLOCK,
            ByteBuffer.allocate(4).putInt((int)(System.currentTimeMillis() / 1000L)).array(),
            BYTES_ONE);
    public static Command setWorkMode = new Command(
            HEAD_REQUEST,
            CMD_UNLOCK,
            BYTES_ZERO,
            BYTES_EMPTY);
    public static Command queryUnlockTimes = new Command(
            HEAD_REQUEST,
            CMD_QUERY_UNLOCK_TIMES,
            BYTES_ZERO,
            BYTES_ZERO);
    public static Command querySoftwareVersion = new Command(
            HEAD_REQUEST,
            CMD_QUERY_SW_VERSION,
            BYTES_ZERO,
            BYTES_EMPTY);
    public static Command queryLockStatus = new Command(
            HEAD_REQUEST,
            CMD_QUERY_LOCK_BEAM_STATUS,
            BYTES_ONE,
            BYTES_EMPTY);
    public static Command queryPowerPercentage = new Command(
            HEAD_REQUEST,
            CMD_QUERY_POWER_PERCENTAGE,
            BYTES_ZERO,
            BYTES_EMPTY);
    public static Command changeToken = new Command(
            HEAD_REQUEST,
            CMD_CHANGE_TOKEN,
            BYTES_EMPTY,
            BYTES_EMPTY);
    public static Command changeKey = new Command(
            HEAD_REQUEST,
            CMD_CHANGE_KEY,
            BYTES_EMPTY,
            BYTES_EMPTY);

    public static byte[] getResponseData(byte[] data) {
        byte[] data1 = Arrays.copyOfRange(data, 3, data.length - 4);
        return data1;
    }

    public static boolean isRequestSuccess(byte[] data) {
        if (data[0] == HEAD_RESPONSE) {
            switch (data[1]) {
                case CMD_UNLOCK:
                    return data[4] == RESULT_SUCCESS;
                case CMD_QUERY_LOCK_BEAM_STATUS:
                    return data[3] == RESULT_SUCCESS;
                default:
                    return false;
            }
        }

        return false;
    }

    public static boolean isLocked(byte[] data) {
        return data[0] == HEAD_RESPONSE &&
                data[1] == CMD_QUERY_LOCK_BEAM_STATUS &&
                data[3] == RESULT_FAIL;

    }
}
