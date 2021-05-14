package com.example.padlockdemo.model;

import com.example.padlockdemo.util.StringUtil;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

    public Command setData1(byte[] data1) {
        this.data1 = data1;
        return this;
    }

    public byte[] getData2() {
        return data2;
    }

    public Command setData2(byte[] data2) {
        this.data2 = data2;
        return this;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public byte[] getCommandData() {
        switch (cmd) {
            case CMD_UNLOCK:
            case CMD_SET_BUILTIN_CLOCK:
                setData1(Command.getBytesFromMilliseconds(System.currentTimeMillis()));
                break;
            default:
                break;
        }

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

    public static final String B101 = "B101";
    public static final String C102 = "C102";

    public static final byte HEAD_REQUEST = 0x00;
    public static final byte HEAD_RESPONSE = 0x01;

    public static final byte CMD_UNLOCK = 0x00;
    public static final byte CMD_LOOK_UP_NOTIFICATION = 0x01; // C102 only
    public static final byte CMD_SET_WORK_MODE = 0x02; // C102 only
    public static final byte CMD_SET_BUILTIN_CLOCK = 0x03; // C102 only
    public static final byte CMD_QUERY_CURRENT_ALL_STATUS_INFO = 0x04; // C102 only
    public static final byte CMD_QUERY_UNLOCK_TIMES = 0x05;
    public static final byte CMD_QUERY_TIME_INFO = 0x06; // C102 only
    public static final byte CMD_QUERY_SIM_CARD_INFO = 0x07; // C102 only
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

    public static byte[] getBytesFromMilliseconds(long millisecond) {
        return ByteBuffer.allocate(4).putInt((int)(millisecond / 1000L)).array();
    }
    public static long getMillisecondsFromBytes(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }
    public static Calendar getCalendarFromMilliseconds(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar;
    }

    public static Command unlock = new Command(
            HEAD_REQUEST,
            CMD_UNLOCK,
            getBytesFromMilliseconds(System.currentTimeMillis()),
            BYTES_ONE);
    public static Command setWorkMode = new Command(
            HEAD_REQUEST,
            CMD_UNLOCK,
            BYTES_ZERO, // 0x00=Idle mode, 0x01=Energy mode, 0x02=Normal mode
            BYTES_EMPTY);
    public static Command setBuiltinClock = new Command(
            HEAD_REQUEST,
            CMD_SET_BUILTIN_CLOCK,
            getBytesFromMilliseconds(System.currentTimeMillis()),
            BYTES_EMPTY);
    public static Command queryCurrentAllStatusInfo = new Command(
            HEAD_REQUEST,
            CMD_QUERY_CURRENT_ALL_STATUS_INFO,
            BYTES_ZERO,
            BYTES_EMPTY);
    public static Command queryUnlockTimes = new Command(
            HEAD_REQUEST,
            CMD_QUERY_UNLOCK_TIMES,
            BYTES_ZERO,
            BYTES_ZERO);
    public static Command queryTimeInfo = new Command(
            HEAD_REQUEST,
            CMD_QUERY_TIME_INFO,
            BYTES_EMPTY, // 0x00=Last locking time, 0x01=Clock current time
            BYTES_EMPTY);
    public static Command querySimCardInfo = new Command(
            HEAD_REQUEST,
            CMD_QUERY_SIM_CARD_INFO,
            BYTES_EMPTY, // 0x00=ICCID Code, 0x01=IMEI Code
            BYTES_EMPTY);
    public static Command querySoftwareVersion = new Command(
            HEAD_REQUEST,
            CMD_QUERY_SW_VERSION,
            BYTES_ZERO,
            BYTES_EMPTY);
    public static Command queryLockStatus = new Command(
            HEAD_REQUEST,
            CMD_QUERY_LOCK_BEAM_STATUS,
            BYTES_ZERO,
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
