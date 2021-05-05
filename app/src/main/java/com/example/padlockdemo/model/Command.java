package com.example.padlockdemo.model;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Command {
    private byte head;
    private byte cmd;
    private byte len;
    private byte[] data1;
    private byte[] data2;
    private byte[] token;

    public static Command unlockCmd = new Command((byte)0x00, (byte)0x00, (byte)0x05, ByteBuffer.allocate(4).putInt((int)(System.currentTimeMillis() / 1000L)).array(), new byte[] { (byte)0x01 });

    public Command(byte head, byte cmd, byte len) {
        this.head = head;
        this.cmd = cmd;
        this.len = len;
    }

    public Command(byte head, byte cmd, byte len, byte[] data1, byte[] data2) {
        this.head = head;
        this.cmd = cmd;
        this.len = len;
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
        return len;
    }

    public void setLen(byte len) {
        this.len = len;
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
        bytes.add(len);
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
}
