package com.bluemouse.kid.bluemouse;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import static com.bluemouse.kid.bluemouse.Constants.STATE_CONNECTED;
import static com.bluemouse.kid.bluemouse.Constants.STATE_NONE;
import static com.bluemouse.kid.bluemouse.Statics.mBTservice;

public class BlueConn extends AppCompatActivity {

    private String Log_Str = "[BlueConn]";
    private FloatingActionButton Search;
    private DeviceList deviceListadapter;
    private IntentFilter filter;
    private ListView Result;
    private ObjectAnimator a;
    private ActionBar actionBar;
    private View Scan_Obj;
    private Handler Conn_Handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_CONNECTED) {
                if(actionBar != null){
                    actionBar.setTitle(R.string.conn_conn_ok);
                }
                if(a != null){
                    a.end();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_conn);
        Scan_Obj = findViewById(R.id.Scan_Obj);
        Scan_Obj.setVisibility(View.GONE);

        Result = (ListView) findViewById(R.id.Result);
        deviceListadapter = new DeviceList(this);
        Result.setAdapter(deviceListadapter);

        mBTservice.setHandle(Conn_Handle);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        this.registerReceiver(mReceiver, filter); // 不要忘了之后解除绑定
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        filter = null;
        if(mBTservice.mAdapter.isDiscovering()){
            mBTservice.mAdapter.cancelDiscovery();
        }
        Search = (FloatingActionButton) findViewById(R.id.Search);
        a = ObjectAnimator.ofFloat(Search,"rotation",0f,360f);
        a.setInterpolator(new LinearInterpolator());
        a.setDuration(800);
        a.setRepeatCount(-1);
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scan_Obj.setVisibility(View.GONE);
                if(mBTservice.mAdapter.isDiscovering()){
                    if(actionBar != null){
                        actionBar.setTitle(R.string.conn_find_over);
                    }
                    mBTservice.mAdapter.cancelDiscovery();
                    Log.e(Log_Str,"cancel find!");
                    a.end();
                }else{
                    if(actionBar != null){
                        actionBar.setTitle(R.string.conn_finding);
                    }
                    a.start();
                    deviceListadapter.arr.clear();
                    deviceListadapter.notifyDataSetChanged();
                    Log.e(Log_Str,"start discovering!");
                    mBTservice.mAdapter.startDiscovery();
                }

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getMenuInflater().inflate(R.menu.conn, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                break;
            case R.id.scan:
                Log.e(Log_Str,"click scan");
                Intent intent = new Intent(BlueConn.this, CaptureActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        if (!mBTservice.mAdapter.isEnabled()) { //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, 0);
        }
        Log.e(Log_Str,"onStart");
        super.onStart();
    }
    @Override
    protected void onResume(){
        mBTservice.setHandle(Conn_Handle);
        super.onResume();
        Log.e(Log_Str,"onResume");
    }
    @Override
    protected void onDestroy(){
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        finish();
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                //TextView scanResult = (TextView) findViewById(R.id.textView);
                if (resultCode == RESULT_OK) {
                    //mBTservice.mAdapter.getRemoteDevice();
                    Log.e(Log_Str,"data"+data);
                    String result = data.getStringExtra(CodeUtils.RESULT_STRING);
                    if(BluetoothAdapter.checkBluetoothAddress(result)){
                        final BluetoothDevice scan_obj = mBTservice.mAdapter.getRemoteDevice(result);
                        CircleImageView Scan_Device = (CircleImageView) findViewById(R.id.Scan_Device);
                        TextView Scan_Name = (TextView) findViewById(R.id.Scan_Name);
                        Scan_Name.setText(scan_obj.getName());
                        Scan_Device.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mBTservice.connect(scan_obj);
                            }
                        });

                        Scan_Obj.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(this,"非法输入，请重新扫描",Toast.LENGTH_SHORT).show();
                    }
                    Log.e(Log_Str,"result"+result);
                } else if (resultCode == RESULT_CANCELED) {
                    Log.e(Log_Str,"没有扫描出结果");
                }
                break;

            default:
                break;
        }
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从Intent中获取设备对象
                Log.e(Log_Str,"Find One!");
                mBTservice.start_listen();

                // 打包发给 Device_List
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Map<String,Object> device_item = new HashMap<String,Object>();
                device_item.put(device.getName()+ "\n" + device.getAddress()+"\t"+
                        device.getBluetoothClass().getDeviceClass(),device);
                // device.getBluetoothClass().getDeviceClass() 获取设备类型
                // 比如 笔记本电脑，台式电脑，手机等

                deviceListadapter.arr.add(device_item);
                deviceListadapter.notifyDataSetChanged();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.e(Log_Str,"Search Finish");
                if(actionBar != null){
                    actionBar.setTitle(R.string.conn_find_over);
                }
                if(deviceListadapter.arr.size() == 0){
                    if(a != null){
                        a.end();
                    }
                }
            }
        }
    };




    private class DeviceList extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        public List<Map<String,Object>> arr = new ArrayList<Map<String,Object>>();
        public DeviceList(Context context) {
            super();
            this.context = context;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arr.size();
        }
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }


        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            // TODO Auto-generated method stub
            if(view == null){
                view = inflater.inflate(R.layout.simple_item, null);
            }
            Iterator keyValue = arr.get(position).entrySet().iterator();
            Map.Entry device_item = (Map.Entry) keyValue.next();
//            String device_str = device_item.getKey().toString();
            BluetoothDevice device_obj = (BluetoothDevice) device_item.getValue();
            TextView DeviceName = (TextView) view.findViewById(R.id.DeviceName);
            DeviceName.setText(device_obj.getName());
            CircleImageView DeviceShow = (CircleImageView) view.findViewById(R.id.DeviceShow);

            DeviceShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Iterator keyValue = arr.get(position).entrySet().iterator();
                    Map.Entry device_item = (Map.Entry) keyValue.next();
                    BluetoothDevice device_obj = (BluetoothDevice) device_item.getValue();

                    if(actionBar != null){
                        actionBar.setTitle(R.string.conn_find_over);
                        mBTservice.connect(device_obj);
                        actionBar.setTitle(R.string.conn_conn);
                    }
                }
            });
            return view;
        }
    }



}
