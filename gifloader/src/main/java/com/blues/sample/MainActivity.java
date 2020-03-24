package com.blues.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blues.gifloader.FileUtil;
import com.blues.gifloader.PicLoader;
import com.blues.gifloader.PicRequest;
import com.blues.gifloader.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Blues";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
    }

    //gif https://ww1.sinaimg.cn/large/0073sXn7gy1g37vxsooo4g30fc0pzafw
    //jpeg https://ww1.sinaimg.cn/large/0073sXn7gy1g37vyr2tsaj308c0go3z0

    public void loadGifPic(View view) {
        String gifUrl = "https://ww1.sinaimg.cn/large/0073sXn7gy1g37vxsooo4g30fc0pzafw";

        PicLoader.getInstance().addBitmapRequest(new PicRequest(MainActivity.this).load(gifUrl)
                .loading(R.mipmap.ic_img_error)
                .into(imageView));

        Log.i(TAG, FileUtil.getExternalCacheDir(MainActivity.this));
    }

    public void loadNormalPic(View view) {
        String jpegUrl = "https://ww1.sinaimg.cn/large/0073sXn7gy1g37vyr2tsaj308c0go3z0";
        PicLoader.getInstance().addBitmapRequest(new PicRequest(MainActivity.this).load(jpegUrl)
                .loading(R.mipmap.ic_img_error)
                .into(imageView));

        Log.i(TAG, FileUtil.getExternalCacheDir(MainActivity.this));
    }
}
