package com.example.padlockdemo.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.util.AsyncUtil;

public class PadlockReceiver extends BroadcastReceiver {

    public PadlockReceiver() {
    }

    public static String[] intentFilters = new String[] {
            "onConnectSuccess",
            "onDisConnected",
            "onQueueUpdated"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "onConnectSuccess":
            case "onDisConnected":
                MainActivity.padlocksAdapter.notifyDataSetChanged();
                break;
            case "onQueueUpdated":
                int size = intent.getIntExtra("QueueSize", 0);
                AsyncUtil.postDelay(context, () -> {
                    TextView textView = MainActivity.currentActivity.findViewById(R.id.textview_queue_size);
                    textView.setText(String.valueOf(size));
                }, 0);
                break;
            default:
                break;
        }
    }
}
