package com.bluemouse.kid.bluemouse;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import static com.bluemouse.kid.bluemouse.Constants.*;

/**
 * Created by Kid on 2016/11/9.
 */

public class BlueToothService {
    private ConnectThread mConnect = null;
    private ConnectedThread mConnected = null;
    private Handler mHandler = null;
    private String Log_Str = " [BlueToothService] ";
    public BluetoothAdapter mAdapter = null;
    public BluetoothDevice mDevice = null;
    private UdiskService UdiskHandler = new UdiskService();

    private int mState = STATE_NONE;

    public BlueToothService(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter == null){//表明此手机不支持蓝牙
            return;
        }
        setState(STATE_NONE);
    }

    public synchronized int getState() {
        return mState;
    }

    private synchronized void setState(int state) {
        Log.d(Log_Str, "setState() " + mState + " -> " + state);
        mState = state;
        if (mHandler == null){
            Log.e(Log_Str,"mHandler is null");
            return;
        }
        mHandler.obtainMessage(mState).sendToTarget();
    }

    public synchronized void setHandle(Handler handler,String Debug) {
        mHandler = handler;
        Log.e(Log_Str,Debug+"is Debugging");
    }

    public synchronized void setHandle(Handler handler) {
        mHandler = handler;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BT_UUID);
            } catch (IOException e) {
                //Log.e(Log_Str, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            //Log.e(Log_Str, "BEGIN mConnectThread");
            mAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    //Log.e(Log_Str, "unable to close() socket during connection failure", e2);
                }
                //connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnect = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(Log_Str, "connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.e(Log_Str,"Begin Conn!!");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e(Log_Str, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //Log.i(Log_Str, "BEGIN mConnectedThread");
            Receive_Handler();
        }

        public void readbyte(byte[] buffer) {
            try {
                mmInStream.read(buffer);
                //Log.e(Log_Str, "Waiting for reading");
            } catch (IOException e) {
                Log.e(Log_Str, "disconnected", e);
                setState(STATE_NONE);
                //start_listen();
            }
        }

        public void writeByte(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                //Log.e(out_Log,"Send Write!!");
            } catch (IOException e) {
                Log.e(Log_Str, "Exception during write", e);
                setState(STATE_NONE);
                //start_listen();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(Log_Str, "close() of connect socket failed", e);
            }
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.e(Log_Str, "connect to: " + device);
        if (mConnect != null) {
            mConnect.cancel();
            mConnect = null;
        }
        // Cancel any thread attempting to make a connection
        if (mConnected != null) {
            mConnected.cancel();
            mConnected = null;
        }
            // Start the thread to connect with the given device
        mConnect = new ConnectThread(device);
        mConnect.start();

        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        //Log.e(Log_Str, "connected Begin!");

        // Cancel the thread that completed the connection
        if (mConnect != null) {
            mConnect.cancel();
            mConnect = null;
        }
        if (mConnected != null) {
            mConnected.cancel();
            mConnected = null;
        }
        mConnected = new ConnectedThread(socket);
        mConnected.start();


        mDevice = device;

        setState(STATE_CONNECTED);
    }

    public synchronized void start_listen() {
        //Log.d(Log_Str, "start");
        if (mConnect != null) {
            mConnect.cancel();
            mConnect = null;
        }

        if (mConnected != null) {
            mConnected.cancel();
            mConnected = null;
        }

        if(mState != STATE_LISTEN){
            setState(STATE_LISTEN);
        }
    }

    public synchronized void stop() {
        //Log.d(Log_Str, "stop");
        if (mConnect != null) {
            mConnect.cancel();
            mConnect = null;
        }
        if (mConnected != null) {
            mConnected.cancel();
            mConnected = null;
        }

        setState(STATE_NONE);
        mHandler = null;
        mAdapter = null;
    }

    public void writeByte(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnected;
        }
        r.writeByte(out);
    }

    public void readbyte(byte[] in) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
//            Log.e(Log_Str,"mState"+mState);
            r = mConnected;
        }
        r.readbyte(in);

    }

    public String getByteMD5(byte[] b,int len) {
        if (b == null) {
            return null;
        }
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            int i = b.length/1024;
            int j = b.length%1024;
            while (i>0) {
                digest.update(b, ((b.length/1024-i)*1024), 1024);
                i--;
            }
            if(j > 0){
                digest.update(b, ((b.length/1024)*1024), j);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        String result = bigInt.toString(16);
        if(len == 16){
            return result.substring(8, 24);
        }else{
            return result;
        }

    }

    public void Receive_Handler(){
        while (mState == STATE_CONNECTED) {
            byte[] i = new byte[3];
            byte[] j = new byte[509];
            readbyte(i);
            if(mState != STATE_CONNECTED) return;
            if(i[0] == SUPER){
                switch (i[1]){
                    case Super_Mode_Udisk :
                        readbyte(j);
                        if(mState != STATE_CONNECTED) return;
                        String path = new String(j,0,509);
                        path = path.trim();
                        Send_Menu_Byte(UdiskHandler.getMenu(path));
                        break;
                    case Super_Mode_Clip :
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ByteArrayOutputStream bos_md5 = new ByteArrayOutputStream();
                        ByteArrayOutputStream bos_save = new ByteArrayOutputStream();
                        int len = (i[2] & 0x000000FF) << 24;
                        readbyte(i);            //get length
                        if(mState != STATE_CONNECTED) return;
                        len += (i[0] & 0x000000FF) << 16;
                        len += (i[1] & 0x000000FF) << 8;
                        len += (i[2] & 0x000000FF);
                        byte type[] = new byte[255];
                        readbyte(type);           // get filename or string type
                        if(mState != STATE_CONNECTED) return;
                        String file_name = new String(type);
                        file_name = file_name.trim();
                        int n0 = len / 512;
                        int n1 = len % 512;
                        byte[] f0 = new byte[512 + 16];
                        byte[] f1 = new byte[n1 + 16];
                        if(file_name.matches("\\|.*")){
                            Log.e(Log_Str,"字符串");
                            // 字符串
//                            if(len > 3000) {          // length is bigger than limit
//                                Log.e(Log_Str,"length is bigger than limit ");
//                                len = 3000;
//                            }
                            Log.e(Log_Str,"length = "+len);
                            String str_recv = "";
                            //begin receive information
                            Message meg_str = Message.obtain(mHandler,HANDLE_SEND_TEXT,PROGRESS_IMFORMER,0);
                            meg_str.sendToTarget();
                            for(int pp=0;pp<n0;pp++){
                                while (true){
                                    readbyte(f0);
                                    if(mState != STATE_CONNECTED) return;
                                    bos.reset();
                                    bos_md5.reset();
                                    bos.write(f0,0,512);
                                    bos_md5.write(f0,512,16);
                                    if(getByteMD5(bos.toByteArray(),16).equals(bos_md5.toString())){
                                        writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Confirm});
                                        bos_save.write(f0,0,512);
                                        meg_str = Message.obtain(mHandler,HANDLE_SEND_TEXT,PROGRESS_IMFORMER,
                                                (int)(bos_save.size()*100.0/len));
                                        meg_str.sendToTarget();
                                        break;
                                    }
                                    writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Error});
//                                    Log.e(Log_Str,"String Error0");
                                }
                            }
                            if(n1 != 0){
//                                byte f1[] = new byte[n1+16];
                                while(true){
                                    readbyte(f1);
                                    if(mState != STATE_CONNECTED) return;
                                    bos.reset();
                                    bos_md5.reset();
                                    bos.write(f1,0,n1);
                                    bos_md5.write(f1,n1,16);
                                    if(getByteMD5(bos.toByteArray(),16).equals(bos_md5.toString())){
                                        writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Confirm});
                                        bos_save.write(f1,0,n1);

                                        break;
                                    }
                                    writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Error});
//                                    Log.e(Log_Str,"String Error1");
                                }
                            }
                            str_recv = bos_save.toString();
                            Log.e(Log_Str,"meg send0");
                            meg_str = Message.obtain(mHandler,HANDLE_SEND_TEXT,PROGRESS_IMFORMER,
                                    100,str_recv);
                            Log.e(Log_Str,"meg send1");
                            meg_str.sendToTarget();
                            Log.e(Log_Str,"meg send2");
                            //Log.e(Log_Str,"str = "+str_recv);
                            Log.e(Log_Str,"len = "+str_recv.length());
                            Log.e(Log_Str,"md5 = "+getByteMD5(str_recv.getBytes(),16));
                        }else{
                            // 文件
                            Log.e(Log_Str,"file name = "+file_name);
                            File dir = new File(CLIP_DIR);
                            if(!dir.exists()){//判断文件目录是否存在
                                dir.mkdirs();
                            }
                            File file = new File(CLIP_DIR+"//"+file_name);
                            if (file.exists()) {
                                file.delete();
                            }
                            try {
                                file.createNewFile();
                            } catch (Exception e) {}
                            while(n1 > 0){
                                while(true){
                                    readbyte(f0);
                                    if(mState != STATE_CONNECTED) return;
                                    try{
                                        bos.reset();
                                        bos_md5.reset();
                                        bos.write(f0,0,512);
                                        bos_md5.write(f0,512,16);
                                        if(getByteMD5(bos.toByteArray(),16).equals(bos_md5.toString())){
                                            getFile(bos.toByteArray(),file);
                                            n1--;
                                            writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Confirm});
                                            break;
                                        }
                                    }catch (java.lang.ArrayIndexOutOfBoundsException e){
                                        e.printStackTrace();
                                    }
                                    Log.e(Log_Str,"error!!");
                                    writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Error});
                                }
                            }
                            if(n1 != 0){
                                while(true){
                                    readbyte(f1);
                                    if(mState != STATE_CONNECTED) return;
                                    try{
                                        bos.reset();
                                        bos_md5.reset();
                                        bos.write(f1,0,n1);
                                        bos_md5.write(f1,n1,16);
                                        if(getByteMD5(bos.toByteArray(),16).equals(bos_md5.toString())){
                                            getFile(bos.toByteArray(),file);
                                            writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Confirm});
                                            break;
                                        }
                                    }catch (java.lang.ArrayIndexOutOfBoundsException e){
                                        e.printStackTrace();
                                    }
                                    Log.e(Log_Str,"error!!");
                                    writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Error});
                                }
                            }
                            Log.e(Log_Str,"file_md5 = "+getFileMD5(file));
                            Log.e(Log_Str,"file_len = "+file.length());
                        }
                        try {
                            bos.close();
                            bos_save.close();
                            bos_md5.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;

                }



            }
        }
    }

    private void Send_Menu_Byte(String[] Menu){
        boolean IsOverFlow = false;
        int BreakPoint = 0;

        do{
            String Menu_Str = "000";
            if (Menu != null) {
                for(int i = BreakPoint;i<Menu.length;i++){
                    if(Menu_Str.getBytes().length+Menu[i].getBytes().length >= 505){
                        BreakPoint = i;
                        IsOverFlow = true;
                        break;
                    }
                    Menu_Str += Menu[i]+"|";
                    IsOverFlow = false;
                }
            }

            for(int tmp = Menu_Str.getBytes().length;tmp<512;tmp++){
                Menu_Str = Menu_Str.concat(" ");
            }
            Menu_Str = Menu_Str.concat(getByteMD5(Menu_Str.getBytes(),16));
            byte[] Send_Menu_byte = Menu_Str.getBytes();
            Send_Menu_byte[0] = SUPER;
            Send_Menu_byte[1] = Super_Mode_Udisk;
            if(IsOverFlow){
                Send_Menu_byte[2] = Super_Continue;
            }
            if(Menu == null){
                Send_Menu_byte[2] = Super_Null;
            }
//            while(true){
//                writeByte(Send_Menu_byte);
//                readbyte(i);
//                if(mState != STATE_CONNECTED) return;
//                if()
//            }

            //Log.e(Log_Str,"Menu_Str = ###"+Menu_Str.getBytes().length+"###"+Menu_Str+"###");
        }while(IsOverFlow);
    }

    private void getFile(byte[] bfile, File file) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }



}
