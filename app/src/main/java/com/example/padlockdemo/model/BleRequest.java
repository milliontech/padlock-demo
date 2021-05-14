package com.example.padlockdemo.model;

import android.content.Context;

import java.util.Date;

public class BleRequest {
    private Context context;
    private BlePadlock blePadlock;
    private Command command;
    private Runnable callback;
    private Date createTime;
    private Date sendTime;
    private int retry;

    public BleRequest(Context context, BlePadlock blePadlock, Command command, Runnable callback) {
        this.context = context;
        this.blePadlock = blePadlock;
        this.command = command;
        this.callback = callback;
        this.createTime = new Date();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BlePadlock getBlePadlock() {
        return blePadlock;
    }

    public void setBlePadlock(BlePadlock blePadlock) {
        this.blePadlock = blePadlock;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Runnable getCallback() {
        return callback;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }
}
