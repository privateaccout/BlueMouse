package com.bluemouse.kid.bluemouse;

import android.util.Log;


/**
 * Created by kid on 2016/12/31.
 */

public class Statics {

    private static String out_Log = " [Statics] ";

    public static BlueToothService mBTservice = new BlueToothService();
    //public static

    public static void setmBTserviceNull(){
        Log.e(out_Log,"setmBTserviceNull");
        mBTservice = null;
    }

}
