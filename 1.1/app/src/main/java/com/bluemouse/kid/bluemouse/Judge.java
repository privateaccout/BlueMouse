package com.bluemouse.kid.bluemouse;


import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import static com.bluemouse.kid.bluemouse.Constants.LEFT_MASK;
import static com.bluemouse.kid.bluemouse.Constants.LONG_PRESS_TIME;
import static com.bluemouse.kid.bluemouse.Constants.MOUSE_LEFT_CLICK;
import static com.bluemouse.kid.bluemouse.Constants.MOUSE_MOVE;
import static com.bluemouse.kid.bluemouse.Constants.MOUSE_NONE;
import static com.bluemouse.kid.bluemouse.Constants.MOUSE_RIGHT_CLICK;
import static com.bluemouse.kid.bluemouse.Constants.RIGHT_MASK;
import static com.bluemouse.kid.bluemouse.Constants.SCROLL_LIMIT;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_CLICK;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_DOWN;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_FLING;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_LONG_PRESS;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_NONE;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_SCROLL;
import static com.bluemouse.kid.bluemouse.Constants.TOUCH_UP;
import static java.lang.Math.abs;


/**
 * Created by kid on 2017/1/5.
 */

public class Judge {

    private MotionEvent Class_event;
    private int Mode_Array[] = new int[]{TOUCH_UP,TOUCH_UP};
    private int Event_Array[] = new int[]{TOUCH_NONE,TOUCH_NONE};
    private int old_Event_Array[] = new int[]{TOUCH_NONE,TOUCH_NONE};

    private boolean Is_Event_Change[] = new boolean[]{false,false};
    private int Mouse_Event[] = new int[]{MOUSE_NONE,MOUSE_NONE};
    public float Mouse_Data[][] = new float[2][2];      //id = 0 or 1    x = 0   y = 1
    private int left_id = -1;
    private int right_id = -1;

    private int index = 0;

    private boolean IsPointer_One = true;


    private float down_x_id0 = -1;
    private float down_y_id0 = -1;

    private float down_x_id1 = -1;
    private float down_y_id1 = -1;

    private long downtime[] = new long[]{-1,-1};
    private float sit[][] = new float[][]{{-1,-1},{-1,-1}};

    private int Touch_Width = 720;
    private int Touch_Hight = 1280;
    private int Touch_Sit_x = 0;
    private int Touch_Sit_y = 0;

    private JudgeListener mListener;
    private String Log = "Judge";
    private VelocityTracker vTracker = null;
    /**
     * 构造器1
     * @param context
     * @param listener
     */
    public Judge(Context context,JudgeListener listener) {
        mListener = listener;
        Init();
    }

    public  interface JudgeListener {
        // 1--->left   2--->right    3--->mid
        void onClick(int Button);
        void onMotion(float x, float y);
    }

    private void Init(){
        this.Class_event = null;
        Mouse_Data[0][0] = 0;
        Mouse_Data[0][1] = 0;
        Mouse_Data[1][0] = 0;
        Mouse_Data[1][1] = 0;
    }

    public boolean onTouch(MotionEvent event){
        if(event == null){
            android.util.Log.i(Log,"event is null");
            return true;
        }

        if (event.getPointerCount() > 2){
            android.util.Log.e(Log,"pointer is bigger than limit");
            return true;
        }

        this.Class_event = MotionEvent.obtain(event);


        old_Event_Array[0] = Event_Array[0];
        old_Event_Array[1] = Event_Array[1];
        Is_Event_Change[0] = false;
        Is_Event_Change[1] = false;
        Action2Mode();


        if(old_Event_Array[0] != Event_Array[0]){
            Is_Event_Change[0] = true;
        }
        if(old_Event_Array[1] != Event_Array[1]){
            Is_Event_Change[1] = true;
        }

        //General_Mode();
        LeftOrRight();
        Event2Mouse();
        //out_log_Mouse_event();
        Mouse_Send();
        return true;
    }

    private void Action2Mode(){
        int tmp_id_0 = 0;
        int tmp_id_1 = 1;
        if(this.Class_event.getAction() == MotionEvent.ACTION_DOWN){            // 1
            IsPointer_One = true;
            downtime[0] = this.Class_event.getEventTime();
            down_x_id0 = this.Class_event.getX();
            down_y_id0 = this.Class_event.getY();
            sit[0][0] = down_x_id0;
            sit[0][1] = down_y_id0;
            Mode_Array[0] = TOUCH_DOWN;

            //android.util.Log.e(Log,"id0 is down");
        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_POINTER_1_DOWN){  //2
            IsPointer_One = false;
            downtime[0] = this.Class_event.getEventTime();
            down_x_id0 = this.Class_event.getX(0);
            down_y_id0 = this.Class_event.getY(0);
            sit[0][0] = down_x_id0;
            sit[0][1] = down_y_id0;
            Mode_Array[0] = TOUCH_DOWN;

            //android.util.Log.e(Log,"id0 is down");
        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN){  //2
            IsPointer_One = false;
            downtime[1] = this.Class_event.getEventTime();
            down_x_id1 = this.Class_event.getX(1);
            down_y_id1 = this.Class_event.getY(1);
            sit[1][0] = down_x_id1;
            sit[1][1] = down_y_id1;
            Mode_Array[1] = TOUCH_DOWN;

            //android.util.Log.e(Log,"id1 is down");
        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_POINTER_1_UP){    //2
            IsPointer_One = false;
            // 判断
            up_id0();
            //初始化
            downtime[0] = -1;
            down_x_id0 = -1;
            down_y_id0 = -1;
            sit[0][0] = -1;
            sit[0][1] = -1;
            Mode_Array[0] = TOUCH_UP;

            //android.util.Log.e(Log,"id0 is up");
        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_POINTER_2_UP){    //2
            IsPointer_One = false;
            // 判断
            up_id1();
            //初始化
            downtime[1] = -1;
            down_x_id1 = -1;
            down_y_id1 = -1;
            sit[1][0] = -1;
            sit[1][1] = -1;
            Mode_Array[1] = TOUCH_UP;

            //android.util.Log.e(Log,"id1 is up");
        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_UP){               //1
            IsPointer_One = true;
            this.index = 0;
            if(downtime[0] == -1){
                up_id1();
                //初始化
                downtime[1] = -1;
                down_x_id1 = -1;
                down_y_id1 = -1;
                sit[1][0] = -1;
                sit[1][1] = -1;
                Mode_Array[1] = TOUCH_UP;
                //android.util.Log.e(Log,"id1 is up");
            }else{
                up_id0();
                //初始化
                downtime[0] = -1;
                down_x_id0 = -1;
                down_y_id0 = -1;
                sit[0][0] = -1;
                sit[0][1] = -1;
                Mode_Array[0] = TOUCH_UP;
                //android.util.Log.e(Log,"id0 is up");
            }

        }
        if(this.Class_event.getAction() == MotionEvent.ACTION_MOVE){
            this.index = 0;
            if(downtime[0] != -1){             // id0 pointer is not up
                move_id0();
                sit[0][0] = this.Class_event.getX(0);
                sit[0][1] = this.Class_event.getY(0);
                this.index = 1;
            }
            if(downtime[1] != -1){             // id1 pointer is not up
                // 修正的部分  begin
                if(this.index == 1 && this.Class_event.getPointerCount() == 1){
                    this.index = 0;
                }
                // 修正的部分  end
                move_id1();
                sit[1][0] = this.Class_event.getX(this.index);
                sit[1][1] = this.Class_event.getY(this.index);
            }
        }
    }

    private int move_id0(){
        if(IsScroll(0)){
            if(Mode_Array[0] != TOUCH_SCROLL){
                //android.util.Log.e(Log,"id0 is Scroll");
                Mode_Array[0] = TOUCH_SCROLL;
                Event_Array[0] = TOUCH_SCROLL;
            }
            Mouse_Data[0][0] = this.Class_event.getX(0) - this.sit[0][0];
            Mouse_Data[0][1] = this.Class_event.getY(0) - this.sit[0][1];

            downtime[0] = this.Class_event.getEventTime();
        }else if(this.Class_event.getEventTime() - downtime[0] > LONG_PRESS_TIME){
            if(Mode_Array[0] != TOUCH_LONG_PRESS){
                //android.util.Log.e(Log,"id0 is Long press");
                Mode_Array[0] = TOUCH_LONG_PRESS;
                Event_Array[0] = TOUCH_LONG_PRESS;
                Mouse_Data[0][0] = 0;
                Mouse_Data[0][1] = 0;
            }
        }else {
            Mouse_Data[0][0] = 0;
            Mouse_Data[0][1] = 0;
        }
        return 0;
    }

    private int move_id1(){
        if(IsScroll(this.index)){
            if(Mode_Array[this.index] != TOUCH_SCROLL){
                //android.util.Log.e(Log,"id1 is Scroll");
                Event_Array[1] = TOUCH_SCROLL;
                Mode_Array[this.index] = TOUCH_SCROLL;
            }
            Mouse_Data[1][0] = this.Class_event.getX(index) - this.sit[1][0];
            Mouse_Data[1][1] = this.Class_event.getY(index) - this.sit[1][1];
            downtime[1] = this.Class_event.getEventTime();
        }else if(this.Class_event.getEventTime() - downtime[1] > LONG_PRESS_TIME){
            if(Mode_Array[this.index] != TOUCH_LONG_PRESS){
                //android.util.Log.e(Log,"id1 is Long press");
                Mode_Array[this.index] = TOUCH_LONG_PRESS;
                Event_Array[1] = TOUCH_LONG_PRESS;
                Mouse_Data[1][0] = 0;
                Mouse_Data[1][1] = 0;
            }
        }else{
            Mouse_Data[1][0] = 0;
            Mouse_Data[1][1] = 0;
        }

        return 0;
    }

    private int up_id0(){
        if(Mode_Array[0] == TOUCH_DOWN){
            //android.util.Log.d(Log,"id0 is Click");
            Event_Array[0] = TOUCH_CLICK;
        }
        if(Mode_Array[0] == TOUCH_SCROLL){
            //android.util.Log.d(Log,"id0 is Fling");
            Event_Array[0] = TOUCH_FLING;
        }
        if(Mode_Array[0] == TOUCH_LONG_PRESS){
            //android.util.Log.d(Log,"id0 is None");
            Event_Array[0] = TOUCH_NONE;
        }
        Mouse_Data[0][0] = 0;
        Mouse_Data[0][1] = 0;
        return 0;
    }

    private int up_id1(){
        if(Mode_Array[1] == TOUCH_DOWN){
            //android.util.Log.d(Log,"id1 is Click");
            Event_Array[1] = TOUCH_CLICK;
        }
        if(Mode_Array[1] == TOUCH_SCROLL){
            //android.util.Log.d(Log,"id1 is Fling");
            Event_Array[1] = TOUCH_FLING;
        }
        if(Mode_Array[1] == TOUCH_LONG_PRESS){
            //android.util.Log.d(Log,"id1 is None");
            Event_Array[1] = TOUCH_NONE;
        }

        Mouse_Data[1][0] = 0;
        Mouse_Data[1][1] = 0;
        return 0;
    }

    private void LeftOrRight(){
        if(IsPointer_One){
            if(down_x_id0 > this.Touch_Width/2){
                this.left_id = 1;
                this.right_id = 0;
            }else{
                this.left_id = 0;
                this.right_id = 1;
            }
        }else{
            if(left_id == -1){
                android.util.Log.e(Log,"-------------- left id is -1 -------------------");
            }
            if(this.Class_event.getPointerCount() == 1){
                //android.util.Log.e(Log,"-------------- point num is 1 -------------------");
            }
            if(this.Class_event.getPointerCount() == 2){
                if(this.Class_event.getX(0) > this.Class_event.getX(1)){
                    this.left_id = 1;
                    this.right_id = 0;
                }else{
                    this.left_id = 0;
                    this.right_id = 1;
                }
            }
        }
    }

    private void Event2Mouse(){
        boolean Right_Done = false;
        switch (this.Event_Array[this.left_id]){
            case TOUCH_FLING:
                Event_Array[this.left_id] = TOUCH_NONE;
                Mouse_Event[0] = MOUSE_NONE;
                break;
            case TOUCH_NONE:
            case TOUCH_LONG_PRESS:
                Mouse_Event[0] = MOUSE_NONE;
                //android.util.Log.i(Log,"Mouse event is Left None");
                break;

            case TOUCH_CLICK:
                if(IsPointer_One && this.Class_event.getX(0) > this.Touch_Width/2){
                    //android.util.Log.i(Log,"Mouse event is Right Click");
                    Right_Done = true;
                    Mouse_Event[1] = MOUSE_RIGHT_CLICK;
                }else{
                    //android.util.Log.i(Log,"Mouse event is Left Click");

                    Mouse_Event[0] = MOUSE_LEFT_CLICK;
                }

                Event_Array[this.left_id] = TOUCH_NONE;
                break;
            case TOUCH_SCROLL:
                //android.util.Log.i(Log,"Mouse event is Left Move");

                //out_log_Mouse_data();
                Mouse_Event[0] = MOUSE_MOVE;
                break;
        }

        if(Right_Done){
            return;
        }

        switch (this.Event_Array[this.right_id]){
            case TOUCH_FLING:
                Event_Array[this.right_id] = TOUCH_NONE;
                Mouse_Event[1] = MOUSE_NONE;
                break;
            case TOUCH_NONE:
            case TOUCH_LONG_PRESS:
                Mouse_Event[1] = MOUSE_NONE;
                //android.util.Log.i(Log,"Mouse event is Right None");
                break;

            case TOUCH_CLICK:
                //android.util.Log.i(Log,"Mouse event is Right Click");
                Event_Array[this.right_id] = TOUCH_NONE;
                Mouse_Event[1] = MOUSE_RIGHT_CLICK;
                break;
            case TOUCH_SCROLL:
                //android.util.Log.i(Log,"Mouse event is Right Move");
                //out_log_Mouse_data();
                Mouse_Event[1] = MOUSE_MOVE;
                break;
        }






    }

    private void out_log_Mouse_data(){
        android.util.Log.i(Log,"Mouse_Data left  x = "+Mouse_Data[this.left_id][0]);
        android.util.Log.i(Log,"Mouse_Data left  y = "+Mouse_Data[this.left_id][1]);
        android.util.Log.i(Log,"Mouse_Data right x = "+Mouse_Data[this.right_id][0]);
        android.util.Log.i(Log,"Mouse_Data right y = "+Mouse_Data[this.right_id][1]);
    }

    private void out_log_Mouse_event(){
        android.util.Log.i(Log,"Mouse_Event left  event : "+Mouse_Event[0]);
        android.util.Log.i(Log,"Mouse_Event Right event : "+Mouse_Event[1]);
    }

    private boolean IsScroll(int i){
        // android.util.Log.e(Log,"index : "+index);
        // android.util.Log.d(Log,this.Class_event.toString());
        // 若首先按下一个pointer0，再按下另一个pointer1
        // 再放开pointer0，若pointer1移出view范围，index 将会变成1，
        // 导致错误 java.lang.IllegalArgumentException: pointerIndex out of range
        // 修改 ActionInit 函数的 MotionEvent.ACTION_MOVE 部分
        if(this.Class_event.getPointerId(i) == 1 && this.Class_event.getPointerCount() == 1 && i == 0){
            if(abs(this.sit[1][0] - this.Class_event.getX(i)) > SCROLL_LIMIT ||
               abs(this.sit[1][1] - this.Class_event.getY(i)) > SCROLL_LIMIT){
                //android.util.Log.e(Log,"=============================");
                //android.util.Log.e(Log,left_id+"     "+right_id);
                return true;
            }
            return false;
        }
        if(abs(this.sit[i][0] - this.Class_event.getX(i)) > SCROLL_LIMIT ||
           abs(this.sit[i][1] - this.Class_event.getY(i)) > SCROLL_LIMIT){
            return true;
        }
        return false;
    }

    private void Mouse_Send(){
        if(Mouse_Event[0] == MOUSE_NONE && Mouse_Event[1] == MOUSE_NONE){
            //android.util.Log.d(Log,"Mouse event is None");
        }
        if(Mouse_Event[0] == MOUSE_NONE && Mouse_Event[1] == MOUSE_RIGHT_CLICK){
            mListener.onClick(RIGHT_MASK);
            //android.util.Log.d(Log,"Mouse event is Right Click");
        }
        if(Mouse_Event[0] == MOUSE_NONE && Mouse_Event[1] == MOUSE_MOVE){
            mListener.onMotion(Mouse_Data[this.right_id][0],Mouse_Data[this.right_id][1]);
            //android.util.Log.d(Log,"Mouse event is Move");
        }

        if(Mouse_Event[0] == MOUSE_LEFT_CLICK && Mouse_Event[1] == MOUSE_NONE){
            mListener.onClick(LEFT_MASK);
            //android.util.Log.d(Log,"Mouse event is Left Click");
        }
        if(Mouse_Event[0] == MOUSE_LEFT_CLICK && Mouse_Event[1] == MOUSE_RIGHT_CLICK){
            mListener.onClick(LEFT_MASK);
            mListener.onClick(RIGHT_MASK);
            //android.util.Log.d(Log,"Mouse event is Left and Right Click");
        }
        if(Mouse_Event[0] == MOUSE_LEFT_CLICK && Mouse_Event[1] == MOUSE_MOVE){
            mListener.onClick(LEFT_MASK);
            mListener.onMotion(Mouse_Data[this.right_id][0],Mouse_Data[this.right_id][1]);
            //android.util.Log.d(Log,"Mouse event is Left Click and Move");
        }

        if(Mouse_Event[0] == MOUSE_MOVE && Mouse_Event[1] == MOUSE_NONE){
            mListener.onMotion(Mouse_Data[this.left_id][0],Mouse_Data[this.left_id][1]);
            //android.util.Log.d(Log,"Mouse event is Move");
        }
        if(Mouse_Event[0] == MOUSE_MOVE && Mouse_Event[1] == MOUSE_RIGHT_CLICK){
            mListener.onMotion(Mouse_Data[this.left_id][0],Mouse_Data[this.left_id][1]);
            mListener.onClick(RIGHT_MASK);
            //android.util.Log.d(Log,"Mouse event is Move and Right Click");
        }
        if(Mouse_Event[0] == MOUSE_MOVE && Mouse_Event[1] == MOUSE_MOVE){

            //android.util.Log.d(Log,"Mouse event is Scroll");
        }


    }

}
