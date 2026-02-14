package com.whispervoice.recorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private Button btnRecord;
    private Button btnSettings;
    private TextView tvTranscription;
    private RadioGroup rgQuality;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private WhisperModel whisperModel;
    private String selectedQuality = "easy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        whisperModel = new WhisperModel(this);

        btnRecord.setOnClickListener(v -> toggleRecording());
        btnSettings.setOnClickListener(v -> openSettings());

        rgQuality.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_easy) {
                selectedQuality = "easy";
            } else if (checkedId == R.id.rb_small) {
                selectedQuality = "small";
            } else if (checkedId == R.id.rb_medium) {
                selectedQuality = "medium";
            }
        });
    }

    private void initViews() {
        btnRecord = findViewById(R.id.btn_record);
        btnSettings = findViewById(R.id.btn_settings);
        tvTranscription = findViewById(R.id.tv_transcription);
        rgQuality = findViewById(R.id.rg_quality);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (!whisperModel.isModelAvailable(selectedQuality)) {
            Toast.makeText(this, "Please download the " + selectedQuality + 
                " model first in Settings", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) 
                + "/recording_" + System.currentTimeMillis() + ".3gp";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            btnRecord.setText("Stop Recording");
            tvTranscription.setText("Recording...");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;
                btnRecord.setText("Start Recording");
                tvTranscription.setText("Processing transcription...");

                processAudioFile();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Stop recording failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processAudioFile() {
        new Thread(() -> {
            String transcription = whisperModel.transcribe(audioFilePath, selectedQuality);
            runOnUiThread(() -> {
                if (transcription != null && !transcription.isEmpty()) {
                    tvTranscription.setText(transcription);
                } else {
                    tvTranscription.setText("Transcription failed. Please try again.");
                }
            });
        }).start();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
    }
}