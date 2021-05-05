package com.example.padlockdemo.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class AsyncUtil {

    public static void postDelay(Context context, Runnable runnable, int delayMillis) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (runnable != null)
                runnable.run();
        }, delayMillis);
    }
}
