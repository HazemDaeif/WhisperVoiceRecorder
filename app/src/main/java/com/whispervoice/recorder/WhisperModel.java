package com.whispervoice.recorder;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class WhisperModel {

    private Context context;

    public WhisperModel(Context context) {
        this.context = context;
    }

    public boolean isModelAvailable(String quality) {
        String fileName = getModelFileName(quality);
        File modelFile = new File(context.getExternalFilesDir(
            Environment.DIRECTORY_DOWNLOADS), fileName);
        return modelFile.exists();
    }

    public String transcribe(String audioFilePath, String quality) {
        if (!isModelAvailable(quality)) {
            return "Model not available. Please download it first.";
        }

        return "Transcription will appear here once Whisper model is integrated. " +
               "Audio file: " + audioFilePath + ", Quality: " + quality;
    }

    private String getModelFileName(String quality) {
        switch (quality) {
            case "easy":
                return "whisper-tiny.tflite";
            case "small":
                return "whisper-small.tflite";
            case "medium":
                return "whisper-medium.tflite";
            default:
                return "whisper-tiny.tflite";
        }
    }

    public String getModelPath(String quality) {
        String fileName = getModelFileName(quality);
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) 
            + File.separator + fileName;
    }
}