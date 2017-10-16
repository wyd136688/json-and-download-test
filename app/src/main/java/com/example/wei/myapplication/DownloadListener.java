package com.example.wei.myapplication;

/**
 * Created by wei on 17-10-9.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCancled();
}
