package com.bluemouse.kid.bluemouse;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by kid on 2017/2/8.
 */

public class Config {


    private static String Log_Str = "[Config]";
    private static Properties p;
    public static String get(Context c ,String key) {
        p = new Properties();
        try {
            InputStream in = new FileInputStream(c.getFilesDir().getAbsolutePath()+"/login.properties");
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p.getProperty(key);
    }

    // 写
    public static void put(Context c ,String key, String value) {
        p = new Properties();
        try {
            InputStream in = new FileInputStream(c.getFilesDir().getAbsolutePath()+"/login.properties");
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.setProperty(key, value);
        OutputStream fos;
        try {
            fos = new FileOutputStream(c.getFilesDir().getAbsolutePath()+"/login.properties");

            p.store(fos, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}