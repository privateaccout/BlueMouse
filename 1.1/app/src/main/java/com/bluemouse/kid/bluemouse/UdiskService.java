package com.bluemouse.kid.bluemouse;

import android.util.Log;

import java.io.File;

/**
 * Created by kid on 2017/1/18.
 */

public class UdiskService {

    private String out_Log = " [UdiskService] ";


    public UdiskService(){

    }


    public String[] getMenu(String father_path){
        // Menu = null;
        File father_fold = new File(father_path);

        if(!father_fold.exists()){
            //Menu = "000";
            Log.e(out_Log,"father_fold not exists");
            return null;
        }
        File[] File_List = new File(father_path).listFiles();
        String[] Menu = new String[File_List.length];
        for(int i=0;i<File_List.length;i++){
            Menu[i] = File_List[i].getName().concat("/");
            Menu[i] += File_List[i].lastModified()+"/";
            byte attr = 0x00;
            if(File_List[i].isDirectory()){
                attr |= 0x08;
                Menu[i] += "  /";
            }else{
                Menu[i] += File_List[i].length()+"/";
            }
            if(File_List[i].canRead()){
                attr |= 0x04;
            }
            if(File_List[i].canWrite()){
                attr |= 0x02;
            }
            if(File_List[i].canExecute()){
                attr |= 0x01;
            }
            String a = new String(new byte[] {attr});
            Menu[i] += a+" / ";
        }
        return Menu;
    }

}
