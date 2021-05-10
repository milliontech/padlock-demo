package com.example.padlockdemo.model;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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

    // Unlock padlock
    public static final byte CMD_UNLOCK = 0x00;
    public static final byte CMD_SET_WORK_MODE = 0x02;
    public static final byte CMD_QUERY_UNLOCK_TIMES = 0x05;
    public static final byte CMD_QUERY_SW_VERSION = 0x08;
    // Check lock status
    public static final byte CMD_QUERY_LOCK_BEAM_STATUS = 0x09;
    public static final byte CMD_QUERY_POWER_PERCENTAGE = 0x0A;
    public static final byte CMD_CHANGE_TOKEN = 0x0B;
    public static final byte CMD_CHANGE_KEY = 0x0C;

    public static final byte RESULT_SUCCESS = 0x00;
    public static final byte RESULT_FAIL = 0x01;

    public static Command unlockRequest = new Command(
            HEAD_REQUEST,
            CMD_UNLOCK,
            ByteBuffer.allocate(4).putInt((int)(System.currentTimeMillis() / 1000L)).array(),
            new byte[] { (byte)0x01 });
    public static Command querylockStatusRequest = new Command(
            HEAD_REQUEST,
            CMD_QUERY_LOCK_BEAM_STATUS,
            new byte[] { (byte)0x01 },
            new byte[] { });

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
