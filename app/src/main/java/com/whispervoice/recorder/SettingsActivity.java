package com.whispervoice.recorder;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private Button btnDownloadEasy, btnDownloadSmall, btnDownloadMedium;
    private TextView tvEasyStatus, tvSmallStatus, tvMediumStatus;
    private ProgressBar progressBar;
    private DownloadManager downloadManager;
    private long currentDownloadId = -1;
    private WhisperModel whisperModel;

    private static final String URL_TINY = "https://github.com/vilassn/whisper_android/releases/download/v1.0/whisper-tiny.tflite";
    private static final String URL_SMALL = "https://github.com/vilassn/whisper_android/releases/download/v1.0/whisper-small.tflite";
    private static final String URL_MEDIUM = "https://github.com/vilassn/whisper_android/releases/download/v1.0/whisper-medium.tflite";

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == currentDownloadId) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, "Download completed!", 
                    Toast.LENGTH_SHORT).show();
                updateModelStatus();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        whisperModel = new WhisperModel(this);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        initViews();
        updateModelStatus();

        registerReceiver(downloadReceiver, 
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        btnDownloadEasy.setOnClickListener(v -> downloadModel(URL_TINY, "whisper-tiny.tflite"));
        btnDownloadSmall.setOnClickListener(v -> downloadModel(URL_SMALL, "whisper-small.tflite"));
        btnDownloadMedium.setOnClickListener(v -> downloadModel(URL_MEDIUM, "whisper-medium.tflite"));
    }

    private void initViews() {
        btnDownloadEasy = findViewById(R.id.btn_download_easy);
        btnDownloadSmall = findViewById(R.id.btn_download_small);
        btnDownloadMedium = findViewById(R.id.btn_download_medium);
        tvEasyStatus = findViewById(R.id.tv_easy_status);
        tvSmallStatus = findViewById(R.id.tv_small_status);
        tvMediumStatus = findViewById(R.id.tv_medium_status);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void downloadModel(String url, String fileName) {
        progressBar.setVisibility(View.VISIBLE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalFilesDir(this, 
            Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Downloading " + fileName);
        request.setDescription("Whisper model download");

        currentDownloadId = downloadManager.enqueue(request);
        Toast.makeText(this, "Download started: " + fileName, Toast.LENGTH_SHORT).show();
    }

    private void updateModelStatus() {
        tvEasyStatus.setText(whisperModel.isModelAvailable("easy") ? 
            "Status: Downloaded ✓" : "Status: Not downloaded");
        tvSmallStatus.setText(whisperModel.isModelAvailable("small") ? 
            "Status: Downloaded ✓" : "Status: Not downloaded");
        tvMediumStatus.setText(whisperModel.isModelAvailable("medium") ? 
            "Status: Downloaded ✓" : "Status: Not downloaded");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }
}