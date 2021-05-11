package com.example.padlockdemo.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.padlockdemo.MainActivity;

public class PadlockReceiver extends BroadcastReceiver {

    public PadlockReceiver() {
    }

    public static String[] intentFilters = new String[] {
            "onConnectSuccess",
            "onDisConnected"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "onConnectSuccess":
            case "onDisConnected":
                MainActivity.padlocksAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
