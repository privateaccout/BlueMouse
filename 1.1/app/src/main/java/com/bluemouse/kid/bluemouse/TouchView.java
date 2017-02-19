package com.bluemouse.kid.bluemouse;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static com.bluemouse.kid.bluemouse.Constants.LEFT_MASK;
import static com.bluemouse.kid.bluemouse.Constants.M_D;
import static com.bluemouse.kid.bluemouse.Constants.P_L;
import static com.bluemouse.kid.bluemouse.Constants.P_M;
import static com.bluemouse.kid.bluemouse.Constants.P_R;
import static com.bluemouse.kid.bluemouse.Constants.RIGHT_MASK;
import static com.bluemouse.kid.bluemouse.Constants.R_L;
import static com.bluemouse.kid.bluemouse.Constants.R_M;
import static com.bluemouse.kid.bluemouse.Constants.R_R;
import static com.bluemouse.kid.bluemouse.Constants.WHEEL_MASK;
import static com.bluemouse.kid.bluemouse.Statics.mBTservice;

/**
 * Created by root on 17-1-5.
 */

public class TouchView extends View{

    private Judge judge;

    private String Log = "TouchView";
    private GestureDetector mg ;

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onDraw(Canvas canvas){
        judge = new Judge(this.getContext(), new Judge.JudgeListener() {
            @Override
            public void onClick(int button) {
                byte click[] = new byte[]{0x00,0x00,0x00};
                switch (button){
                    case LEFT_MASK:
                        click[0] = P_L;
                        click[1] = R_L;
                        break;
                    case RIGHT_MASK:
                        click[0] = P_R;
                        click[1] = R_R;
                        break;
                    case WHEEL_MASK:
                        click[0] = P_M;
                        click[1] = R_M;
                        break;
                }
                mBTservice.writeByte(click);
                android.util.Log.i(Log,"onClick");
            }

            @Override
            public void onMotion(float x,float y) {

                android.util.Log.i(Log,"onMotion");
                android.util.Log.d(Log,"x = "+(int)x+"     y = "+(int)y);
                int ix = (int) x;
                int iy = (int) y;
                if(ix == 0 && iy == 0){
                    return;
                }
                if(abs(ix) >= 128 || abs(iy) >= 128){
                    return;
                }
                byte[] send_move = new byte[]{0x00,0x00,0x00};
                send_move[2] =  (byte) (iy & 0xFF);
                send_move[1] =  (byte) (ix & 0xFF);
                send_move[0] =  M_D;
                mBTservice.writeByte(send_move);
            }
        });
        super.onDraw(canvas);


    }

    private int abs(int in) {
        // TODO Auto-generated method stub
        if (in >= 0) {
            return in;
        }else{
            return (0-in);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_CANCEL){
            android.util.Log.d(Log,"MotionEvent.ACTION_CANCEL");
        }
        judge.onTouch(event);
        return true;
    }
}
