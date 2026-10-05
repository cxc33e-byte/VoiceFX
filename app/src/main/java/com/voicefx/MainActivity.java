package com.voicefx;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.*;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private TextView resultText;
    private AudioRecord recorder;
    private AudioTrack player;
    private Thread thread;
    private volatile boolean running = false;

    private final int SAMPLE_RATE = 48000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
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
        info.setText("اختبار h2w Input → h2w Output");
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);
        layout.addView(info);

        Button start = new Button(this);
        start.setText("تشغيل اختبار h2w");

        start.setOnClickListener(v -> {

            if (!running) {
                startTest();
                start.setText("إيقاف الاختبار");
            } else {
                stopTest();
                start.setText("تشغيل اختبار h2w");
            }
        });

        layout.addView(start);

        resultText = new TextView(this);
        resultText.setTextSize(16);
        resultText.setPadding(10, 20, 10, 10);
        layout.addView(resultText);

        setContentView(layout);
    }

    private void startTest() {

        new Thread(() -> {

            try {

                AudioManager audioManager =
                        (AudioManager) getSystemService(AUDIO_SERVICE);

                AudioDeviceInfo inputH2w = null;
                AudioDeviceInfo outputH2w = null;

                for (AudioDeviceInfo d :
                        audioManager.getDevices(
                                AudioManager.GET_DEVICES_INPUTS)) {

                    if (String.valueOf(d.getProductName())
                            .toLowerCase()
                            .contains("h2w")) {

                        inputH2w = d;
                        break;
                    }
                }

                for (AudioDeviceInfo d :
                        audioManager.getDevices(
                                AudioManager.GET_DEVICES_OUTPUTS)) {

                    if (String.valueOf(d.getProductName())
                            .toLowerCase()
                            .contains("h2w")) {

                        outputH2w = d;
                        break;
                    }
                }

                if (inputH2w == null) {
                    showResult("h2w INPUT غير موجود ❌");
                    return;
                }

                if (outputH2w == null) {
                    showResult("h2w OUTPUT غير موجود ❌");
                    return;
                }

                int bufferSize =
                        AudioRecord.getMinBufferSize(
                                SAMPLE_RATE,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT
                        );

                if (bufferSize <= 0) {
                    showResult("Buffer Error");
                    return;
                }

                recorder = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize * 2
                );

                AudioFormat outputFormat =
                        new AudioFormat.Builder()
                                .setSampleRate(SAMPLE_RATE)
                                .setEncoding(
                                        AudioFormat.ENCODING_PCM_16BIT
                                )
                                .setChannelMask(
                                        AudioFormat.CHANNEL_OUT_MONO
                                )
                                .build();

                player = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(
                                        AudioAttributes.USAGE_MEDIA
                                )
                                .setContentType(
                                        AudioAttributes.CONTENT_TYPE_SPEECH
                                )
                                .build(),
                        outputFormat,
                        bufferSize * 2,
                        AudioTrack.MODE_STREAM,
                        AudioManager.AUDIO_SESSION_ID_GENERATE
                );

                boolean inputPreferred =
                        recorder.setPreferredDevice(inputH2w);

                boolean outputPreferred =
                        player.setPreferredDevice(outputH2w);

                recorder.startRecording();
                player.play();

                running = true;

                showResult(
                        "اشتغل الاختبار ✅\n\n" +
                        "INPUT h2w ID: " +
                        inputH2w.getId() +
                        "\n" +
                        "OUTPUT h2w ID: " +
                        outputH2w.getId() +
                        "\n\n" +
                        "Input Preferred: " +
                        inputPreferred +
                        "\n" +
                        "Output Preferred: " +
                        outputPreferred
                );

                short[] buffer =
                        new short[bufferSize / 2];

                while (running) {

                    int read =
                            recorder.read(
                                    buffer,
                                    0,
                                    buffer.length
                            );

                    if (read > 0) {

                        for (int i = 0; i < read; i++) {

                            float sample =
                                    buffer[i] * 1.8f;

                            if (sample > 32767)
                                sample = 32767;

                            if (sample < -32768)
                                sample = -32768;

                            buffer[i] = (short) sample;
                        }

                        player.write(
                                buffer,
                                0,
                                read
                        );
                    }
                }

            } catch (Exception e) {

                showResult(
                        "خطأ ❌\n" +
                        e.getClass().getSimpleName() +
                        "\n" +
                        e.getMessage()
                );

            } finally {
                stopTest();
            }

        }, "H2W-Test").start();
    }

    private void stopTest() {

        running = false;

        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception ignored) {}

            recorder.release();
            recorder = null;
        }

        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {}

            player.release();
            player = null;
        }
    }

    private void showResult(String text) {

        runOnUiThread(() ->
                resultText.setText(text)
        );
    }

    @Override
    protected void onDestroy() {
        stopTest();
        super.onDestroy();
    }
}
