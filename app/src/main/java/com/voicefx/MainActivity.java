package com.voicefx;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("اختبار h2w");
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);
        layout.addView(info);

        Button testButton = new Button(this);
        testButton.setText("اختبار h2w 🎤");

        testButton.setOnClickListener(v -> {

            testButton.setEnabled(false);
            resultText.setText("جاري اختبار h2w...");

            new Thread(() -> {

                String result = testH2W();

                runOnUiThread(() -> {
                    resultText.setText(result);
                    testButton.setEnabled(true);
                });

            }).start();
        });

        layout.addView(testButton);

        resultText = new TextView(this);
        resultText.setTextSize(16);
        resultText.setPadding(10, 20, 10, 10);

        layout.addView(resultText);

        setContentView(layout);
    }

    private String testH2W() {

        if (android.os.Build.VERSION.SDK_INT < 23) {
            return "Android قديم";
        }

        AudioManager audioManager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        if (audioManager == null) {
            return "AudioManager غير متوفر";
        }

        AudioDeviceInfo h2w = null;

        AudioDeviceInfo[] inputs =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        for (AudioDeviceInfo device : inputs) {

            String name =
                    String.valueOf(device.getProductName());

            if (device.getType() ==
                    AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                name.toLowerCase().contains("h2w")) {

                h2w = device;
                break;
            }
        }

        if (h2w == null) {
            return "h2w ما انوجد كـ INPUT ❌";
        }

        int sampleRate = 48000;

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) {
            return "Buffer Error: " + bufferSize;
        }

        AudioRecord record = null;

        try {

            record = new AudioRecord(
                    android.media.MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
            );

            if (record.getState() !=
                    AudioRecord.STATE_INITIALIZED) {

                return "AudioRecord فشل بالتهيئة ❌";
            }

            boolean preferred =
                    record.setPreferredDevice(h2w);

            record.startRecording();

            short[] buffer =
                    new short[bufferSize / 2];

            int positiveReads = 0;
            int totalSamples = 0;
            int nonZeroSamples = 0;

            long start =
                    System.currentTimeMillis();

            while (System.currentTimeMillis() - start < 5000) {

                int read =
                        record.read(
                                buffer,
                                0,
                                buffer.length
                        );

                if (read > 0) {

                    positiveReads++;
                    totalSamples += read;

                    for (int i = 0; i < read; i++) {

                        if (buffer[i] != 0) {
                            nonZeroSamples++;
                        }
                    }
                }
            }

            return
                    "اختبار h2w انتهى ✅\n\n" +
                    "الاسم: " +
                    h2w.getProductName() +
                    "\n" +
                    "ID: " +
                    h2w.getId() +
                    "\n" +
                    "Preferred: " +
                    preferred +
                    "\n" +
                    "RecordingState: " +
                    record.getRecordingState() +
                    "\n" +
                    "Positive reads: " +
                    positiveReads +
                    "\n" +
                    "Total PCM: " +
                    totalSamples +
                    "\n" +
                    "Non-zero PCM: " +
                    nonZeroSamples;

        } catch (Exception e) {

            return
                    "خطأ ❌\n" +
                    e.getClass().getSimpleName() +
                    "\n" +
                    e.getMessage();

        } finally {

            if (record != null) {

                try {
                    record.stop();
                } catch (Exception ignored) {}

                record.release();
            }
        }
    }
}
