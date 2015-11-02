package com.ammatti.stanley.sipdialer.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 1310042 on 2015/11/2.
 */
public class UpdateAPK extends Activity {

    String s_curVersion = null;
    String s_newVersion = null;
    ProgressDialog pBar = null;

    public void checkUpdateVersion(String Versionurl) {
        try {
            //Log.e("tdy", "checkUpdate start");
            URL updateURL = new URL(Versionurl);
            URLConnection conn = updateURL.openConnection();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);

            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            /* Convert the Bytes read to a String. */
            final String s = new String(baf.toByteArray());

            /* Get current Version Number */
            s_curVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            s_newVersion = s;
            //Log.e("tdy",s_curVersion+" = "+s_newVersion);
            if (s_curVersion.equals(s_newVersion)) {
                //Log.e("tdy","不需更新");
                //mHandler.post(showVersion);
            } else {
                //Log.e("tdy","需更新");
                //mHandler.post(showUpdate);
            }
        } catch (Exception e) {
            //Log.e("tdy","網路發生錯誤");
            e.printStackTrace();
        }
    }

    private Runnable showUpdate = new Runnable() {

        public void run() {
                                /*
            new AlertDialog.Builder(SpeakSettings.this)
                    .setTitle("軟體更新")
                    .setMessage("目前版本("+s_curVersion+")\n發現新版本("+s_newVersion+")，是否更新?")
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                         // User clicked OK so do some stuff
                            pBar = new ProgressDialog(SpeakSettings.this);
                            pBar.setTitle("正在下載");
                            pBar.setMessage("請稍候...");
                            pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            pBar.setIndeterminate(false);//設置進度條是否不明確
                            pBar.setCancelable(true);
                            pBar.setProgress(100);
                            pBar.show();
                            //開始下載
                            Thread thread = new Thread(){
                                public void run(){
                                    Message message = new Message();
                                    message.what = downLoadFile(fileurl, fileName, path);
                                    msghandler.sendMessage(message);
                                }
                            };
                            thread.start();
                        }
                    })
                    .setNegativeButton("暫不更新", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                         // User clicked Cancel
                        }
                    })
                    .show();
                     */
        }
    };

    //show目前版本
    private Runnable showVersion = new Runnable() {
        public void run() {
                /*new AlertDialog.Builder(SpeakSettings.this)
                        .setTitle("軟體更新")
                        .setMessage("目前為最新版本(" + s_curVersion + ")")
                        .setNegativeButton("好", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                         // User clicked Cancel
                            }
                        })
                        .show();*/
        }
    };

    //下載檔案
    public int downLoadFile(String httpUrl, String fileName, String path) {
        try {
            // 當存放文件的文件目錄不存在的時候創建該文件目錄
            File tmpFile = new File(path);
            if (!tmpFile.exists()) {
                tmpFile.mkdir();
            }
            //this is the file to be downloaded
            URL url = new URL(httpUrl);
            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            //and connect!
            urlConnection.connect();

            File file = new File(path, fileName);
            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(file);
            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
            //this is the total size of the file
            int totalSize = urlConnection.getContentLength();
            //variable to store total downloaded bytes
            int downloadedSize = 0;
            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer
            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                downloadedSize += bufferLength;
                //this is where you would do something to report the prgress, like this maybe
                //updateProgress(downloadedSize, totalSize);
                pBar.setProgress(downloadedSize * 100 / totalSize);
            }
            //close the output stream when done
            fileOutput.close();
        } catch (Exception e) {
            // TODO: handle exception
            return 0;
        }
        return 1;
    }

    //顯示下載完畢或失敗的event
    public Handler msghandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "下載成功！！", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                    /*
                    intent.setDataAndType(
                            //Uri.fromFile(new File(path, fileName)),
                            //"application/vnd.android.package-archive"
                            );
                    startActivity(intent);
                    */
            } else {
                Toast.makeText(getApplicationContext(), "下載失敗！！", Toast.LENGTH_LONG).show();
            }
            pBar.cancel();
        }
    };
}
