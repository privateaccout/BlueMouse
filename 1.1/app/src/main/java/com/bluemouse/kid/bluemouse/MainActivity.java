package com.bluemouse.kid.bluemouse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluemouse.kid.bluemouse.Bubble.BubbleTextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.bluemouse.kid.bluemouse.Constants.*;
import static com.bluemouse.kid.bluemouse.Statics.mBTservice;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String Log_Str = "[MainActivity]";
    private View Search;
    private View main_include;
    private ChatList chatListadapter;
    private FABToolbarLayout layout;
    private Toolbar toolbar;
    private long exit_time = System.currentTimeMillis();
    private TextView PairName;
    private ListView ChatView;
    private SharedPreferences chat_data;
    private SharedPreferences.Editor chat_data_editor;

    public Handler Main_Handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == STATE_NONE){
                Log.i(Log_Str,"Connection Lost!");
                PairName.setText("未连接");
                if(layout.isToolbar()){
                    layout.hide();
                }
                Toast.makeText(getApplicationContext(),"连接已断开",Toast.LENGTH_LONG).show();
            }
            if (msg.what == STATE_CONNECTED) {
                PairName.setText(mBTservice.mDevice.getName().toString());
            }
            if(msg.what == HANDLE_SEND_TEXT){
                Log.e(Log_Str,"arg2 = "+msg.arg2);
                if(msg.arg2 == 100){
                    Add_Chat_Item(HANDLE_SEND_TEXT,"",msg.obj.toString());
                    //Log.e(Log_Str,"send data "+);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_include = findViewById(R.id.main_include);
        layout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ChatView = (ListView) findViewById(R.id.ChatView);
        chatListadapter = new ChatList(this);
        ChatView.setAdapter(chatListadapter);
        chatListadapter.arr.clear();
        chatListadapter.notifyDataSetChanged();

        chat_data = getSharedPreferences("data",0);
        chat_data_editor = chat_data.edit();
        chat_data_editor.putInt("max_number",chat_data.getInt("max_number",0));
        chat_data_editor.putInt("min_number",chat_data.getInt("min_number",0));
        chat_data_editor.commit();
        int max = chat_data.getInt("max_number",0);         //next save number
        int min = chat_data.getInt("min_number",0);         //exists min number
        int num = max - min;
        if(num - chat_data.getAll().size() >= 50){
            //   rearrange();
        }
        for(int i=chat_data.getInt("min_number",0);i<chat_data.getInt("max_number",0);i++){
            if(chat_data.contains(i+"")){
                Map<String,Object> chat_item = new HashMap<String,Object>();
                chat_item.put("place_hold",chat_data.getString(i+"","NONE"+CLIP_TEXT+CLIP_PC));
                chatListadapter.arr.add(chat_item);
            }
        }
        chatListadapter.notifyDataSetChanged();

        Button text = (Button) findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = chat_data.getInt("max_number",0);
                String value = "testtt";
                Log.e(Log_Str,"add !!"+num);
                value += CLIP_TEXT;
                value += CLIP_PC;
                chat_data_editor.putInt("max_number",num+1);
                chat_data_editor.putString(num+"",value);
                chat_data_editor.commit();
                Map<String,Object> chat_item = new HashMap<String,Object>();
                chat_item.put("place_hold",value);
                chatListadapter.arr.add(chat_item);
                chatListadapter.notifyDataSetChanged();
            }
        });
        Button image = (Button) findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBTservice.writeByte(new byte[]{SUPER,Super_Mode_Clip,Super_Null});
                Log.e(Log_Str,"send instruction");
            }
        });
//        Button upload = (Button) findViewById(R.id.upload);
//        upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });



        PairName = (TextView) headerView.findViewById(R.id.PairName);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        Search = findViewById(R.id.fabtoolbar_fab);
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBTservice.getState() == STATE_CONNECTED) {
                    layout.show();
                }else{
                    if(layout.isFab()){
                        Intent conn = new Intent(MainActivity.this,BlueConn.class);
                        startActivity(conn);
                    }else{
                        layout.hide();
                    }
                }
            }
        });
    }

    private void Add_Chat_Item(int type,String path,String value){
        switch (type){
            case HANDLE_SEND_TEXT:
                int num = chat_data.getInt("max_number",0);
                Log.e(Log_Str,"add !!"+num);
                value += CLIP_TEXT;
                value += CLIP_PC;
                chat_data_editor.putInt("max_number",num+1);
                chat_data_editor.putString(num+"",value);
                chat_data_editor.commit();
                Map<String,Object> chat_item = new HashMap<String,Object>();
                if(value.length()>110){
                    value = value.substring(0,100).concat(CLIP_TEXT+CLIP_PC);
                }
                chat_item.put("place_hold",value);
                chatListadapter.arr.add(chat_item);
                chatListadapter.notifyDataSetChanged();
                break;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if (!mBTservice.mAdapter.isEnabled()) { //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, 0);
        }
        Log.e(Log_Str,"onStart");
    }
    @Override
    protected void onResume(){
        mBTservice.setHandle(Main_Handle);
        if(mBTservice.getState() == STATE_CONNECTED){
            PairName.setText(mBTservice.mDevice.getName().toString());
        }else{
            PairName.setText("未连接");
        }
        super.onResume();
        Log.e(Log_Str,"onResume");
    }

    @Override
    protected void onDestroy(){
        mBTservice.mAdapter.disable();
        mBTservice.stop();
        Statics.setmBTserviceNull();
        super.onDestroy();
        System.exit(0);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(layout.isToolbar()){
                layout.hide();
            }else if(System.currentTimeMillis() - exit_time > 2000){
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                exit_time = System.currentTimeMillis();
            }else{
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this,Settings.class);
                startActivity(settings);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            String src = "/data/data/"+getApplicationContext().getPackageName()+"/shared_prefs/data.xml";
            Log.e(Log_Str,"upload!"+src);
            String url = "http://192.168.1.103/up.php";
            FileUploadTask fileuploadtask = new FileUploadTask(src,url);
            fileuploadtask.execute();
        } else if (id == R.id.nav_gallery) {
            Log.e(Log_Str,"Clear All");
            chat_data_editor.clear();
            chat_data_editor.putInt("max_number",0);
            chat_data_editor.putInt("min_number",0);
            chat_data_editor.commit();
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class ChatList extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        public List<Map<String,Object>> arr = new ArrayList<Map<String,Object>>();
        public ChatList(Context context) {
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
            if(view == null){
                view = inflater.inflate(R.layout.chat_pc, null);
            }

            Iterator keyValue = arr.get(position).entrySet().iterator();
            Map.Entry chat_item = (Map.Entry) keyValue.next();
            String chat_text = (String)chat_item.getValue();
            chat_text = chat_text.substring(0,chat_text.length()-8);
            Log.e(Log_Str,"chat text = "+chat_text);

            View Progress_Layout = view.findViewById(R.id.Progress_Layout);
            Progress_Layout.setVisibility(View.GONE);
            BubbleTextView btview = (BubbleTextView) view.findViewById(R.id.chat_text);
            btview.setText(chat_text);
            btview.setVisibility(View.VISIBLE);






//            TextView DeviceName = (TextView) view.findViewById(R.id.DeviceName);
//            DeviceName.setText(device_obj.getName());
//            CircleImageView DeviceShow = (CircleImageView) view.findViewById(R.id.DeviceShow);
//
//            DeviceShow.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    Iterator keyValue = arr.get(position).entrySet().iterator();
//                    Map.Entry device_item = (Map.Entry) keyValue.next();
//                    BluetoothDevice device_obj = (BluetoothDevice) device_item.getValue();
//
//                }
//            });
            return view;
        }
    }

    // show Dialog method
    private void showDialog(String mess) {
        new AlertDialog.Builder(MainActivity.this).setTitle("Message")
                .setMessage(mess)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    class FileUploadTask extends AsyncTask<Object, Integer, Void> {

        private ProgressDialog dialog = null;
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        //the file path to upload
        String pathToOurFile = null;
        //the server address to process uploaded file
        String urlServer = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        File uploadFile = null;
        long totalSize = 0; // Get size of file, bytes

        public FileUploadTask(String file_path,String send_url){
            urlServer = send_url;
            pathToOurFile = file_path;
            uploadFile = new File(pathToOurFile);
            totalSize = uploadFile.length();

        }

        @Override
        protected void onPreExecute() {
            Log.e(Log_Str,"upload file path is "+pathToOurFile);
            Log.e(Log_Str,"upload url is "+urlServer);
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("正在上传...");
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Object... arg0) {

            long length = 0;
            int progress;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 256 * 1024;// 256KB

            try {
                FileInputStream fileInputStream = new FileInputStream(new File(
                        pathToOurFile));

                URL url = new URL(urlServer);
                connection = (HttpURLConnection) url.openConnection();

                // Set size of every block for post
                connection.setChunkedStreamingMode(256 * 1024);// 256KB

                // Allow Inputs & Outputs
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Enable POST method
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);

                outputStream = new DataOutputStream(
                        connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream
                        .writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                                + pathToOurFile + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    length += bufferSize;
                    progress = (int) ((length * 100) / totalSize);
                    publishProgress(progress);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);
                publishProgress(100);

                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

				/* 将Response显示于Dialog */
                // Toast toast = Toast.makeText(UploadtestActivity.this, ""
                // + serverResponseMessage.toString().trim(),
                // Toast.LENGTH_LONG);
                // showDialog(serverResponseMessage.toString().trim());
				/* 取得Response内容 */
                // InputStream is = connection.getInputStream();
                // int ch;
                // StringBuffer sbf = new StringBuffer();
                // while ((ch = is.read()) != -1) {
                // sbf.append((char) ch);
                // }
                //
                // showDialog(sbf.toString().trim());

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

            } catch (Exception ex) {
                // Exception handling
                // showDialog("" + ex);
                // Toast toast = Toast.makeText(UploadtestActivity.this, "" +
                // ex,
                // Toast.LENGTH_LONG);

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                dialog.dismiss();
                // TODO Auto-generated method stub
            } catch (Exception e) {
            }
        }

    }



}
