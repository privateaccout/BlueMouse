package com.bluemouse.kid.bluemouse;

import java.util.UUID;

/**
 * Created by Kid on 2016/11/10.
 */
public interface Constants {


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTED = 6;
    public static final int MESSAGE_ACTIVE_SEND = 7;

    public static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B54FB");
    public static final int MOUSE_SERVICE = 0;
    public static final int FILE_SERVICE  = 1;
    public static final int UDISK_SERVICE = 2;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED  = 3;  // now connected to a remote device
    public final static int HANDLE_SEND_TEXT = 4;
    public final static int HANDLE_SEND_FILE = 5;
    public final static int HANDLE_SEND_PICT = 6;

    public final static int PROGRESS_IMFORMER = 7;





    public static final int LONG_PRESS_TIME = 800;
    public static final int SCROLL_LIMIT = 3;

    public static final int MOUSE_NONE          = 11;
    public static final int MOUSE_LEFT_CLICK    = 12;
    public static final int MOUSE_RIGHT_CLICK   = 12;
    public static final int MOUSE_MOVE          = 18;


    public static final int TOUCH_UP = 21;
    public static final int TOUCH_DOWN = 22;
    public static final int TOUCH_SCROLL = 23;
    public static final int TOUCH_LONG_PRESS = 24;
    public static final int TOUCH_CLICK = 25;
    public static final int TOUCH_FLING = 26;
    public static final int TOUCH_NONE = 27;

    // Mode_Array  == UP DOWN SCROLL LONG_PRESS
    // Event_Array == NONE CLICK SCROLL LONG_PRESS FLING
    // Mouse_Event == NONE CLICK MOVE           0-->left 1-->right


    //button // 1--->left   2--->right    3--->mid
    public static final int LEFT_MASK  = 1;
    public static final int RIGHT_MASK = 2;
    public static final int WHEEL_MASK = 3;


    public final byte P_L = 0x01;		//0000 0001
    public final byte P_R = 0x02;		//0000 0010
    public final byte P_M = 0x03;		//0000 0011

    public final byte R_L = 0x05;		//0000 0101
    public final byte R_R = 0x06;		//0000 0110
    public final byte R_M = 0x07;		//0000 0111

    public final byte M_D = 0x0A;		//0000 1010
    public final byte V_S = 0x08;		//0000 1000
    public final byte H_S = 0x09;		//0000 1001


    public final byte Mode_Init  = 0x00;		//0000 0000
    public final byte Mode_Mouse = 0x01;		//0000 0001


    public final byte Mode_End   = (byte) 0xFF;		//0000 0000

    public final static byte SUPER = 0x40;		//0100 0000

    public final static byte Super_Mode_Udisk = 0x11;		//0001 0001
    public final static byte Super_Mode_Clip  = 0x12;		//0001 0010
    public final static byte Super_Null       = 0x23;		//0010 0011     #
    public final static byte Super_Continue   = 0x21;		//0010 0001     !
    public final static byte Super_Confirm    = 0x24;		//0010 0100
    public final static byte Super_Error      = 0x22;		//0010 0010



    public final String CLIP_DIR = "//sdcard//BlueMouse";

    public final String CLIP_FILE = "|FILE";
    public final String CLIP_TEXT = "|TEXT";
    public final String CLIP_JPEG = "|JPEG";
    public final String CLIP_NONE = "|NONE";

    public final String CLIP_MY   = "|MY";
    public final String CLIP_PC   = "|PC";
}