package com.example.padlockdemo.manager;

import android.content.Intent;

import com.clj.fastble.BleManager;
import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.model.BleRequest;
import com.example.padlockdemo.model.PadlockReceiver;
import com.example.padlockdemo.util.PadlockUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BleRequestManager {
    private final static String TAG = MainActivity.class.getSimpleName();

    private static BleRequestManager instance;
    private List<BleRequest> queue;
    private List<BleRequest> failedRequests;

    private boolean processing;
    private Timer timer;
    private Lock lock = new ReentrantLock();

    private static final int SINGLE = 1;
    private static final int MULTIPLE = 2;
    private int mode = SINGLE;
    private int timeout = 10;
    private int retry = 0;

    public BleRequestManager() {
        queue = new ArrayList<>();
        failedRequests = new ArrayList<>();
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
        lock.lock();
        queue.add(request);
        lock.unlock();

        MainActivity.currentActivity
                .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                        .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));

        return queue.size();
    }

    public void remove(BleRequest request) {
        lock.lock();
        queue.remove(request);
        lock.unlock();

        MainActivity.currentActivity
                .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                        .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));
    }

    public void clear() {
        lock.lock();
        queue.clear();
        lock.unlock();

        processing = false;

        MainActivity.currentActivity
                .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                        .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));
    }

    private void handle() {
        lock.lock();
        List<BleRequest> pendingRequests = queue.stream().filter(i -> i.getSendTime() == null)
                .collect(Collectors.toList());
        lock.unlock();

        if ((mode == SINGLE && !processing || mode == MULTIPLE) && pendingRequests.size() > 0) {
            processing = true;

            BleRequest request = pendingRequests.get(0);
            request.setSendTime(new Date());

            MainActivity.currentActivity
                    .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                            .setAction("onQueueUpdated").putExtra("QueueSize", queue.size()));

            request.getBlePadlock().setProcessing(true);
            PadlockUtil.sendRequest(request, data -> {
                request.getBlePadlock().handleResponse(request.getCommand(), data);
                request.getBlePadlock().setProcessing(false);
                remove(request);
                if (!hasOtherRequest(request))
                    BleManager.getInstance().disconnect(request.getBlePadlock().getDevice());
                processing = false;
                return true;
            }, error -> {
                if (request.getRetry() < retry) {
                    request.setRetry(request.getRetry() + 1);
                    request.setSendTime(null);
                } else {
                    request.getBlePadlock().setProcessing(false);
                    remove(request);
                    if (!hasOtherRequest(request))
                        BleManager.getInstance().disconnect(request.getBlePadlock().getDevice());
                    failedRequests.add(request);
                }
                processing = false;
                return true;
            });
        }

        lock.lock();
        List<BleRequest> timeoutRequests = queue.stream().filter(i -> {
            return i.getSendTime() != null
                    && i.getSendTime().compareTo(new Date(System.currentTimeMillis() - timeout * 1000L)) <= 0;
        }).collect(Collectors.toList());
        lock.unlock();
        timeoutRequests.forEach(i -> {
            remove(i);
            failedRequests.add(i);
            processing = false;
        });
    }

    private boolean hasOtherRequest(BleRequest request) {
        return queue.stream().anyMatch(i -> i.getBlePadlock().getMacAddress().equals(request.getBlePadlock().getMacAddress()));
    }
}
