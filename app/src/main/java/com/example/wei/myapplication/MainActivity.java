package com.example.wei.myapplication;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.util.List;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "MainActivity";

    private String responseData;
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //下载操作
        Button start = (Button)findViewById(R.id.start);
        Button pause = (Button)findViewById(R.id.pause);
        Button cancel = (Button)findViewById(R.id.cancel);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        cancel.setOnClickListener(this);
        //开启并绑定服务
        Intent intent = new Intent(this,DownloadService.class);
        startService(intent);//开启
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);//绑定
        //check permisson
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        }
        //OKHttp 的用法
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client =  new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    responseData = response.body().toString();
                    Log.d("test",">>weiyandong>>> responseData ="+responseData);
                    // Pull 解析XML方式
                    try {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser xmlPullParser = factory.newPullParser();
                        xmlPullParser.setInput(new StringReader(responseData));
                        int eventType = xmlPullParser.getEventType();
                        String id = "";
                        String name = "";
                        String version ="";
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String nodeName = xmlPullParser.getName();
                            switch (eventType) {
                                //开始解析某个节点
                                case XmlPullParser.START_TAG : {
                                    if ("id".equals(nodeName)) {
                                        id = xmlPullParser.nextText();
                                    } else if ("name".equals(nodeName)) {
                                        name = xmlPullParser.nextText();
                                    } else if ("version".equals(nodeName)) {
                                        version = xmlPullParser.nextText();
                                    }
                                    break;
                                }
                                //完成解析某个节点
                                case XmlPullParser.END_TAG : {
                                    if ("app".equals(nodeName)) {
                                        Log.d(TAG,">>weiyandong>> id ="+id);
                                        Log.d(TAG,">>weiyandong>> name ="+name);
                                        Log.d(TAG,">>weiyandong>>> version ="+version);
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }
                            eventType = xmlPullParser.next();
                        }
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }

                    //SAX解析xml
                    try {
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        XMLReader xmlReader = factory.newSAXParser().getXMLReader();
                        MyHandler handler = new MyHandler();
                        xmlReader.setContentHandler(handler);
                        xmlReader.parse(new InputSource(new StringReader(responseData)));

                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }

                    // json 解析 使用JSONObject
                    String jsonData = "";
                    // [{"id":"5","version":"1","name":"jack"},{...},{...}]
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for(int i = 0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            String version = jsonObject.getString("version");
                            Log.d("test",">>weiyandong>> id ="+id);
                            // ....
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //使用JSON 解析 json
                    Gson gson = new Gson();
                    List<App> appList = gson.fromJson(jsonData,new TypeToken<List<App>>(){}.getType());
                    for (App app : appList) {
                        Log.d("test",">>weiyandong>> id ="+app.getId());
                        //....
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length>0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"拒绝权限将无法使用程序",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start:
                String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
                downloadBinder.startDownload(url);
                break;
            case R.id.pause:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }
}
