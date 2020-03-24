package com.blues.gifloader;

import android.graphics.Bitmap;

public class PicHandler {

    private volatile long gifInfo;

    static {
        System.loadLibrary("native-lib");
    }


    public PicHandler(String path) {
        gifInfo = openFileNative(path);
    }

    //获取宽
    public synchronized int getWidth() {
        return getWidthNative(gifInfo);
    }

    private native int getWidthNative(long gifInfo);

    //获取高
    public synchronized int getHeight() {
        return getHeightNative(gifInfo);
    }

    private native int getHeightNative(long gifInfo);

    //获取帧数
    public synchronized int getLength() {
        return getLengthNative(gifInfo);
    }

    private native int getLengthNative(long gifInfo);

    //c拿对象只拿地址
    private native long openFileNative(String msg);

    //渲染
    public long renderFrame(Bitmap bitmap, int index) {
        return renderFrameNative(gifInfo, bitmap, index);
    }

    private native long renderFrameNative(long gifInfo, Bitmap bitmap, int index);

}
