package com.blues.gifloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class PicDispatcher extends Thread {

    private final String TAG = this.getClass().getSimpleName();
    //线程切换
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private PicHandler picHandler;
    private Bitmap bitmap;
    private int currentIndex = 0;
    private int maxIndex = 0;
    private ImageView imageView;
    private ScheduledExecutorService executors = Executors.newScheduledThreadPool(4);
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0xffff) {
                //加载普通图片
                String filePath = (String) msg.obj;
                imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                return;
            }
            if (!executors.isShutdown()) {
                Log.i(TAG, "obtain message--> 1");
                long delay_time = picHandler.renderFrame(bitmap, currentIndex);
                currentIndex++;
                if (currentIndex >= maxIndex) {
                    currentIndex = 0;
                }
                imageView.setImageBitmap(bitmap);
                sendEmptyMessageDelayed(1, delay_time);
            } else {
                Log.i(TAG, "obtain message--> removeCallbacksAndMessages");
                removeCallbacksAndMessages(null);
            }
        }
    };

    //阻塞队列进行存储请求
    private LinkedBlockingQueue<PicRequest> linkedBlockingQueue;

    public PicDispatcher(LinkedBlockingQueue<PicRequest> linkedBlockingQueue) {
        this.linkedBlockingQueue = linkedBlockingQueue;
    }

    @Override
    public void run() {
        //线程没有被中断
        while (!isInterrupted()) {
            try {
                PicRequest picRequest = linkedBlockingQueue.take();
                //占位图的加载
                loadPlaceHolder(picRequest);
                //从服务器/缓存加载图片
                downloadGif(picRequest);
                //将bitmap加载到image view
                loadIntoImageView(picRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将gif加载进入image view
     *
     * @param picRequest
     */
    private void loadIntoImageView(final PicRequest picRequest) {
        Log.i(TAG, "loadIntoImageView");
        if (!TextUtils.isEmpty(picRequest.getUrl()) && picRequest.getView() != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showGifImpl(picRequest.getContext(), picRequest.getView(), picRequest.getUrl());
                }
            });
        }
    }

    /**
     * 加载真正的gif
     *
     * @param picRequest
     * @return
     */
    private void downloadGif(PicRequest picRequest) {
        Log.i(TAG, "downloadGif");
        String uri = picRequest.getUrl();
        if (FileUtil.fileExits(picRequest.getContext(), MD5Utils.md5(uri))) {
            Log.i(TAG, MD5Utils.md5(uri) + " is already exits!");
            return;
        }
        FileOutputStream fos = null;
        InputStream is = null;
        if (!TextUtils.isEmpty(uri)) {
            try {
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                connection.connect();

                is = connection.getInputStream();
                if (picRequest.getListener() != null) {
                    if (is != null) {
                        picRequest.getListener().onSuccess();
                    } else {
                        picRequest.getListener().onFailure();
                    }
                }

                int len = 0;
                StringBuffer child = new StringBuffer();
                child.append(MD5Utils.md5(uri));
                //TODO 去做content-type类型判断
                if (TextUtils.equals(connection.getContentType(), "image/gif")) {
                    //使用NDK加载
                    //进行本地缓存
                    //缓存目录是系统的缓存目录，清除应用缓存时会被清掉，gif名称 md5(url).gif
                    child.append(".gif");

                } else if (TextUtils.equals(connection.getContentType(), "image/jpeg")) {
                    //普通类型的直接用bitmap加载
                    child.append(".jpeg");
                } else if (TextUtils.equals(connection.getContentType(), "image/png")) {
                    //普通类型的直接用bitmap加载
                    child.append(".png");
                } else {
                    Log.i(TAG, "parse error, msg: unknown content-type");
                }
                File file = new File(FileUtil.getExternalCacheDir(picRequest.getContext()), child.toString());
                Log.i(TAG, "cached gif:->> " + child.toString());

                byte[] buf = new byte[128];
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                //刷新缓冲区
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭流，防止内存泄漏
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 加载占位图
     *
     * @param picRequest
     */
    private void loadPlaceHolder(final PicRequest picRequest) {
        Log.i(TAG, "loadPlaceHolder");
        if (!(picRequest.getLoadingResId() > 0) && picRequest.getView() != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    picRequest.getView().setImageResource(picRequest.getLoadingResId());
                }
            });

        }
    }

    /**
     * NDK加载gif的实现方法
     *
     * @param iv
     * @param url
     */
    private void showGifImpl(Context context, final ImageView iv, String url) {
        imageView = iv;
        iv.setImageBitmap(null);

        File gifFile = FileUtil.getFile(context, MD5Utils.md5(url));

        if (gifFile != null && gifFile.exists()) {
            final String filePath = gifFile.getAbsolutePath();
            Log.i(TAG, "filePath--->>" + filePath);
            //gif使用ndk加载
            if (filePath.endsWith(".gif")) {
                Log.i(TAG, "load gif");
                picHandler = new PicHandler(filePath);

                //获取gif图片宽高
                int width = picHandler.getWidth();
                int height = picHandler.getHeight();

                //获取gif图片帧数

                //创建空的bitmap
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                //渲染图片返回延迟
                currentIndex = 0;
                final long delay_time = picHandler.renderFrame(bitmap, currentIndex);
                maxIndex = picHandler.getLength();
//                iv.setImageBitmap(bitmap);

                //根据延时时长发送空的消息
                executors.schedule(new Runnable() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(1);
                    }
                }, delay_time, TimeUnit.MILLISECONDS);
            } else {
                while (!executors.isShutdown()) {
                    executors.shutdownNow();
                    handler.removeCallbacksAndMessages(null);
                    if (executors.isShutdown()) {
                        Message msg = Message.obtain();
                        msg.what = 0xffff;
                        msg.obj = filePath;
                        handler.sendMessage(msg);
//                        iv.setImageBitmap(BitmapFactory.decodeFile(filePath));
                    }
                }
            }
        } else {
            //TODO 如果图片下载失败后的处理
            Toast.makeText(context, "图片加载失败", Toast.LENGTH_SHORT).show();
        }
    }
}
