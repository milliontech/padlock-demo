package com.example.padlockdemo.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.util.AsyncUtil;
import com.example.padlockdemo.util.StringUtil;

public class PadlockReceiver extends BroadcastReceiver {
    private final static String TAG = PadlockReceiver.class.getSimpleName();

    public PadlockReceiver() {
    }

    public static String[] intentFilters = new String[] {
            "onConnectSuccess",
            "onDisConnected",
            "onSendCommand",
            "onQueueUpdated",
            "onMessage"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, String.format("onReceive: %s", intent.getAction()));

        switch (intent.getAction()) {
            case "onConnectSuccess":
                AsyncUtil.postDelay(context, () -> {
                    MainActivity.blePadlockArrayList.stream().filter(i -> i.getMacAddress().equals(intent.getStringExtra("Device"))).findFirst().get()
                            .setConnected(true);
                    MainActivity.padlocksAdapter.notifyDataSetChanged();
                }, 0);
                break;
            case "onDisConnected":
                AsyncUtil.postDelay(context, () -> {
                    MainActivity.blePadlockArrayList.stream().filter(i -> i.getMacAddress().equals(intent.getStringExtra("Device"))).findFirst().get()
                            .setConnected(false);
                    MainActivity.padlocksAdapter.notifyDataSetChanged();
                }, 0);
                break;
            case "onSendCommand":
                String command = intent.getStringExtra("Command");
                AsyncUtil.postDelay(context, () -> {
                    ((TextView) MainActivity.currentActivity.findViewById(R.id.textview_last_command))
                            .setText(command);
                }, 0);
                break;
            case "onQueueUpdated":
                int size = intent.getIntExtra("QueueSize", 0);
                AsyncUtil.postDelay(context, () -> {
                    TextView textView = MainActivity.currentActivity.findViewById(R.id.textview_queue_size);
                    textView.setText(String.valueOf(size));
                    View topBar = MainActivity.currentActivity.findViewById(R.id.layout_top_bar);
                    int color = size > 0 ? Color.YELLOW : Color.WHITE;
                    topBar.setBackgroundColor(color);
                    ((TextView) MainActivity.currentActivity.findViewById(R.id.textview_last_command))
                            .setText("");
                }, 0);
                break;
            case "onMessage":
                String message = intent.getStringExtra("Message");
                MainActivity.showMessage(message);
            default:
                break;
        }
    }
}
