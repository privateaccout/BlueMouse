package com.bluemouse.kid.bluemouse;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static com.bluemouse.kid.bluemouse.Constants.Mode_End;
import static com.bluemouse.kid.bluemouse.Constants.Mode_Mouse;
import static com.bluemouse.kid.bluemouse.Constants.STATE_CONNECTED;
import static com.bluemouse.kid.bluemouse.Statics.mBTservice;

/**
 * Created by kid on 2016/12/31.
 */

public class Mouse extends Activity {


    private TouchView my;
    private String out_Log = " [Mouse] ";



    private Handler Mouse_Handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != STATE_CONNECTED){
                Log.i(out_Log,"Connection Lost!");
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(out_Log,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mouse);
        my = (TouchView) findViewById(R.id.my);
        mBTservice.setHandle(Mouse_Handle);
        mBTservice.writeByte(new byte[]{Mode_Mouse,0x00,0x00});
    }

    @Override
    protected void onResume(){
        Log.e(out_Log,"onResume");
        super.onResume();
        mBTservice.setHandle(Mouse_Handle);
        //mBTservice.writeByte(new byte[]{Mode_Mouse});
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        if(mBTservice.getState() == STATE_CONNECTED){
            mBTservice.writeByte(new byte[]{Mode_End,Mode_End,Mode_End});
        }
        finish();
        Log.e(out_Log,"onDestroy");
    }
}


