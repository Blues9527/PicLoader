package com.blues.gifloader;

import android.content.Context;

import java.io.File;

public class FileUtil {

    /**
     * 判断缓存文件夹是否存在对应文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static boolean fileExits(Context context, String fileName) {
        File fileDir = new File(getExternalCacheDir(context));
        File[] files = fileDir.listFiles();
        if (files.length != 0) {
            for (File f : files) {
                if (f.getName().contains(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从缓存目录取对应文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File getFile(Context context, String fileName) {
        File fileDir = new File(getExternalCacheDir(context));
        File[] files = fileDir.listFiles();
        if (files.length != 0) {
            for (File f : files) {
                if (f.getName().contains(fileName)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * 获取缓存文件夹目录
     *
     * @param mContext
     * @return
     */
    public static String getExternalCacheDir(Context mContext) {
        File file = mContext.getExternalCacheDir();
        if (file == null || !file.exists()) {
            file = mContext.getCacheDir();
            if (file == null || !file.exists()) {
                file = mContext.getFilesDir();
            }
        }
        return file.getAbsolutePath();
    }
}
