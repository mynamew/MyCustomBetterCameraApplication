package com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 *  缓存路径
 * @author timi.
 * @date 2017/3/2
 * @time 14:04
*/
public class PathUtils {


    // 公共目录  img
    public static String getSaveImgPath(Context context) {
        return context.getExternalCacheDir().getAbsolutePath()+"/qhb/img";
    }

    // 公共目录  img
    public static String getPublishImgPath(Context context) {
        return context.getExternalCacheDir().getAbsolutePath()+"/qhb/publishimg";
    }


    //公共目录 视频
    public static String getCrashVideoPath(Context context) {
        File vedioFile = new File(context.getExternalCacheDir(), "qhb/vedio");
        if (!vedioFile.exists()) {//如果文件夹不存在，创建文件夹
            vedioFile.mkdirs();
        }
        return vedioFile.getAbsolutePath();
    }


    public static void folderScan(Context context, String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            File[] array = file.listFiles();

            for (int i = 0; i < array.length; i++) {
                File f = array[i];

                if (f.isFile()) {// FILE TYPE
                    String name = f.getName();

                    if (name.contains(".jpg")) {
                        fileScan(context, f.getAbsolutePath());
                    }
                } else {// FOLDER TYPE
                    folderScan(context, f.getAbsolutePath());
                }
            }
        }
    }

    public static void fileScan(Context context, String file) {
        Uri data = Uri.parse("file://" + file);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                data));
    }
    public static String getDiskCacheDir(Context context, String uniqueName)
    {
        String filePath = "";
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable())
        {
            //��ʾSD�����ڻ��߲����Ƴ�
            filePath = context.getExternalCacheDir().getPath();//��û���·��
        }else
        {
            filePath = context.getCacheDir().getPath();
        }
        return filePath+ File.separator+uniqueName;
    }

}
