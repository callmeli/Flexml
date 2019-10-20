package com.guet.flexbox.preview;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.arch.core.util.Function;
import androidx.core.os.HandlerCompat;

import com.facebook.litho.LithoHandler;

public class SimpleLithoHandler
        extends Handler
        implements LithoHandler {

    SimpleLithoHandler(String name) {
        super(((Function<Void, Looper>) input -> {
            HandlerThread handlerThread = new HandlerThread(name);
            handlerThread.start();
            return handlerThread.getLooper();
        }).apply(null));
    }

    @Override
    public boolean isTracing() {
        return false;
    }

    @Override
    public void post(Runnable runnable, String tag) {
        HandlerCompat.postDelayed(this, () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, runnable, 0);
    }

    @Override
    public void remove(Runnable runnable) {
        removeCallbacksAndMessages(runnable);
    }
}
