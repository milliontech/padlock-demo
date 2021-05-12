package com.example.padlockdemo.model;

import android.content.Context;

public class BleRequest {
    private Context context;
    private BlePadlock blePadlock;
    private Command command;
    private Runnable callback;

    public BleRequest(Context context, BlePadlock blePadlock, Command command, Runnable callback) {
        this.context = context;
        this.blePadlock = blePadlock;
        this.command = command;
        this.callback = callback;
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
}
