package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.*;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private AudioRecord recorder;
    private AudioTrack player;
    private boolean running = false;

    private static final int SAMPLE_RATE = 44100;

    private float boost = 1.5f;
    private float echo = 0.0f;

    private short[] echoBuffer;
    private int echoIndex;

    private TextView status;

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

        buildUI();
    }

    private void buildUI() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("مؤثرات صوتية");
        info.setTextSize(20);
        info.setGravity(Gravity.CENTER);
        layout.addView(info);

        TextView boostText = new TextView(this);
        boostText.setText("الضربة: 150%");
        boostText.setTextSize(18);
        layout.addView(boostText);

        SeekBar boostBar = new SeekBar(this);
        boostBar.setMax(30);
        boostBar.setProgress(5);

        boostBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        boost = 1.0f + (p * 0.1f);

                        boostText.setText(
                                "الضربة: " +
                                (int)(boost * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                }
        );

        layout.addView(boostBar);

        TextView echoText = new TextView(this);
        echoText.setText("الصدى: 0%");
        echoText.setTextSize(18);
        layout.addView(echoText);

        SeekBar echoBar = new SeekBar(this);
        echoBar.setMax(20);
        echoBar.setProgress(0);

        echoBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        echo = p / 20.0f;

                        echoText.setText(
                                "الصدى: " +
                                (int)(echo * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                }
        );

        layout.addView(echoBar);

        Button button = new Button(this);
        button.setText("تشغيل الزاجل");

        button.setOnClickListener(v -> {

            if (!running) {
                startAudio();
                button.setText("إيقاف الزاجل");
            } else {
                stopAudio();
                button.setText("تشغيل الزاجل");
            }
        });

        layout.addView(button);

        status = new TextView(this);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 25, 10, 10);

        layout.addView(status);

        setContentView(layout);
    }

    private void startAudio() {

        AudioManager manager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        AudioDeviceInfo input = null;

        AudioDeviceInfo[] devices =
                manager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        for (AudioDeviceInfo device : devices) {

            if (device.getType() ==
                    AudioDeviceInfo.TYPE_BUILTIN_MIC) {

                input = device;
                break;
            }
        }

        int minBuffer =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (minBuffer <= 0) {
            status.setText("فشل إنشاء الصوت ❌");
            return;
        }

        try {

            recorder =
                    new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBuffer * 2
                    );

            if (input != null) {
                recorder.setPreferredDevice(input);
            }

            player =
                    new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBuffer * 2,
                            AudioTrack.MODE_STREAM
                    );

            echoBuffer =
                    new short[SAMPLE_RATE / 3];

            echoIndex = 0;

            recorder.startRecording();
            player.play();

            running = true;

            status.setText(
                    "الزاجل يعمل ✅\n" +
                    "الميكروفون + المؤثرات"
            );

            new Thread(() -> {

                short[] buffer =
                        new short[minBuffer];

                while (running) {

                    int read =
                            recorder.read(
                                    buffer,
                                    0,
                                    buffer.length
                            );

                    if (read > 0) {

                        processAudio(
                                buffer,
                                read
                        );

                        player.write(
                                buffer,
                                0,
                                read
                        );
                    }
                }

            }).start();

        } catch (Exception e) {

            status.setText(
                    "خطأ: " +
                    e.getClass().getSimpleName()
            );
        }
    }

    private void processAudio(
            short[] buffer,
            int length) {

        for (int i = 0; i < length; i++) {

            float sample = buffer[i];

            sample *= boost;

            if (echo > 0) {

                short delayed =
                        echoBuffer[echoIndex];

                sample +=
                        delayed *
                        (echo * 0.8f);
            }

            sample =
                    Math.max(
                            -32768,
                            Math.min(
                                    32767,
                                    sample
                            )
                    );

            echoBuffer[echoIndex] =
                    (short) sample;

            echoIndex++;

            if (echoIndex >= echoBuffer.length) {
                echoIndex = 0;
            }

            buffer[i] = (short) sample;
        }
    }

    private void stopAudio() {

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

        echoBuffer = null;
    }

    @Override
    protected void onDestroy() {

        stopAudio();

        super.onDestroy();
    }
}
