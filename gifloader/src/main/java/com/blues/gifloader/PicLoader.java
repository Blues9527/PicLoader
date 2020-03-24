package com.blues.gifloader;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class PicLoader {

    private final String TAG = this.getClass().getSimpleName();

    private static volatile PicLoader instance;
    private LinkedBlockingQueue<PicRequest> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private PicDispatcher[] picDispatchers;

    private PicLoader() {
        start();
    }

    public static PicLoader getInstance() {
        if (instance == null) {
            synchronized (PicLoader.class) {
                if (instance == null) {
                    instance = new PicLoader();
                }
            }
        }
        return instance;
    }

    public void addBitmapRequest(PicRequest picRequest) {
        Log.i(TAG, "addBitmapRequest");
        if (picRequest == null) {
            return;
        }
        if (!linkedBlockingQueue.contains(picRequest)) {
            linkedBlockingQueue.add(picRequest);
        }
    }

    private void start() {
        Log.i(TAG, "start");
        stop();
        startAllDispatcher();
    }

    public void stop() {
        Log.i(TAG, "stop");
        if (picDispatchers != null && picDispatchers.length > 0) {
            for (PicDispatcher dispatcher : picDispatchers) {
                if (!dispatcher.isInterrupted()) {
                    dispatcher.interrupt();
                }
            }
        }
    }

    private void startAllDispatcher() {
        Log.i(TAG, "startAllDispatcher");
        int threadCount = Runtime.getRuntime().availableProcessors();
        picDispatchers = new PicDispatcher[threadCount];
        for (int i = 0; i < threadCount; i++) {
            PicDispatcher dispatcher = new PicDispatcher(linkedBlockingQueue);
            dispatcher.start();
            picDispatchers[i] = dispatcher;
        }
    }
}
