package com.blues.gifloader;

import android.content.Context;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

public class PicRequest {

    //load(url)
    private String url;

    //loading(id)
    private int loadingResId;

    //listener(RequestListener)
    private PicRequestListener listener;

    //into(target)
    private SoftReference<ImageView> softReference;

    private Context context;

    public PicRequest(Context context) {
        this.context = context;
    }

    public PicRequest load(String url) {
        this.url = url;
        return this;
    }

    public PicRequest loading(int loadingResId) {
        this.loadingResId = loadingResId;
        return this;
    }

    public PicRequest listener(PicRequestListener listener) {
        this.listener = listener;
        return this;
    }

    public PicRequest into(ImageView target) {
        this.softReference = new SoftReference<>(target);
        PicLoader.getInstance().addBitmapRequest(this);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public int getLoadingResId() {
        return loadingResId;
    }

    public PicRequestListener getListener() {
        return listener;
    }

    public ImageView getView() {
        return softReference.get();
    }

    public Context getContext() {
        return context;
    }
}
