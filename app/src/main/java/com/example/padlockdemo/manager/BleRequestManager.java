package com.example.padlockdemo.manager;

import android.content.Intent;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.model.BleRequest;
import com.example.padlockdemo.model.PadlockReceiver;
import com.example.padlockdemo.util.PadlockUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BleRequestManager {
    private final static String TAG = MainActivity.class.getSimpleName();

    private static BleRequestManager instance;
    private Queue<BleRequest> queue;
    private BleRequest lastRequest;

    private boolean processing;
    private Timer timer;
    private Lock lock = new ReentrantLock();

    public BleRequestManager() {
        queue = new ArrayDeque<>();
        processing = false;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handle();
            }
        }, 0, 1000);
    }

    public static BleRequestManager getInstance() {
        if (instance == null)
            instance = new BleRequestManager();

        return instance;
    }

    public int add(BleRequest request) {
        queue.add(request);

        MainActivity.currentActivity
                .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                        .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));

        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    private void handle() {
        if (!processing && queue.size() > 0) {
            processing = true;

            lastRequest = queue.poll();
            MainActivity.currentActivity
                    .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                            .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));

            lastRequest.getBlePadlock().setProcessing(true);
            PadlockUtil.sendRequest(lastRequest, data -> {
                processing = false;
                lastRequest.getBlePadlock().handleResponse(lastRequest.getCommand(), data);
                lastRequest.getBlePadlock().setProcessing(false);
                return true;
            }, error -> {
                processing = false;
                lastRequest.getBlePadlock().setProcessing(false);
                return true;
            });
        }
    }
}
