package com.example.wei.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("downloading...",progress));

        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //下载成功后，将前台服务通知关闭，并发送一个下载成功的通知
            stopForeground(true);
            NotificationManager notificationManager = (NotificationManager)getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("download success",-1));
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //下载成功后，将前台服务通知关闭，并发送一个下载成功的通知
            stopForeground(true);
            NotificationManager notificationManager = (NotificationManager)getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("download failed",-1));
        }

        @Override
        public void onPaused() {
            downloadTask = null;
        }

        @Override
        public void onCancled() {
            downloadTask = null;
            stopForeground(true);
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(downloadListener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("downloading...",0));
                //提高优先级，设为前台service
            }
        }
        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancleDownload();
            } else {
                if (downloadUrl != null) {
                    //取消下载时，需要将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.
                            DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    NotificationManager notificationManager = (NotificationManager)getSystemService(
                            NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                    stopForeground(true);//设为后台服务
                }
            }
        }
    }

    private Notification getNotification(String title,int progress) {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress+"%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
