package com.example.wei.myapplication;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.awt.font.NumericShaper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wei on 17-10-9.
 */
/*


一个异步任务的执行一般包括以下几个步骤：

1.execute(Params... params)，执行一个异步任务，需要我们在代码中调用此方法，触发异步任务的执行。

2.onPreExecute()，在execute(Params... params)被调用后立即执行，一般用来在执行后台任务前对UI做一些标记。

3.doInBackground(Params... params)，在onPreExecute()完成后立即执行，用于执行较为费时的操作，此方法将接收输入参数和返回计算结果。在执行过程中可以调用publishProgress(Progress... values)来更新进度信息。

4.onProgressUpdate(Progress... values)，在调用publishProgress(Progress... values)时，此方法被执行，直接将进度信息更新到UI组件上。

5.onPostExecute(Result result)，当后台操作结束时，此方法将会被调用，计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。

 */
//下载
public class DownloadTask extends AsyncTask<String,Integer,Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PASUSED = 2;
    public static final int TYPE_CANCLED = 3;
    private DownloadListener listener;
    private boolean isCancled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        //用于在后台执行具体的下载逻辑
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            long downloadLength = 0;//记录下载文件的长度
            String downloadUrl = params[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getPath();
            Log.d("test",">>weiyandong>>> fileName + directory ="+directory+fileName);
            file = new File(directory+fileName);
            if(!file.exists()){
                try {
                    file.createNewFile() ;
                    //file is create
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Log.d("test",">>weiyandong>> file.exists ="+file.exists());
            if (file.exists()) {
                downloadLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            Log.d("test",">>weiyandong>>> contentLength="+contentLength);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadLength) {
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes ="+downloadLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            Log.d("test",">>weiyandong>>> response true?="+response.isSuccessful());
            if (response != null && response.isSuccessful()) {
                is = response.body().byteStream();
                Log.d("test",">>weiyandong>> saveFile before ="+saveFile);
                saveFile = new RandomAccessFile(file,"rw");
                Log.d("test",">>weiyandong>> saveFile after ="+saveFile);
                saveFile.seek(downloadLength);// 跳过已下载的章节
                byte [] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCancled) {
                        return TYPE_CANCLED;
                    } else if (isPaused) {
                        return TYPE_PASUSED;
                    } else {
                        total += len;
                        saveFile.write(b,0,len);
                        //计算已下载的百分比
                        int progress = (int)((total+downloadLength)*100/contentLength);
                        publishProgress(progress);

                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (Exception e){

        }finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
                if (isCancled && file != null) {
                    file.delete();

                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }



        return TYPE_FAILED;
    }

    @Override
    protected void onPostExecute(Integer status) {
        //用于通知最终的下载结果
        switch (status) {
            case TYPE_CANCLED:
                listener.onCancled();
                break;
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PASUSED:
                listener.onPaused();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //用于在界面上更新当前的下载进度
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void cancleDownload() {
        isCancled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
