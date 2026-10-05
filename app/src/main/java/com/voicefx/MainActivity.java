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
    private volatile boolean running = false;

    private static final int SAMPLE_RATE = 44100;
    private static final int REQUEST_MIC = 100;

    private volatile float boost = 1.0f;
    private volatile float echo = 0.0f;
    private volatile float noise = 0.0f;
    private volatile float volume = 1.0f;

    private short[] echoBuffer;
    private int echoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MIC
            );
        }

        buildUI();
    }

    private void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 35, 30, 30);
        root.setGravity(Gravity.CENTER);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.img_0116);
        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(240, 240);
        logoParams.gravity = Gravity.CENTER;
        logoParams.bottomMargin = 10;

        root.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        addBoost(root);
        addEcho(root);
        addNoise(root);
        addVolume(root);

        Button button = new Button(this);
        button.setText("تشغيل المايك");

        button.setOnClickListener(v -> {
            if (!running) {
                startAudio();
                button.setText("إيقاف المايك");
            } else {
                stopAudio();
                button.setText("تشغيل المايك");
            }
        });

        root.addView(button);

        setContentView(root);
    }

    private void addBoost(LinearLayout root) {

        TextView label = new TextView(this);
        label.setText("الضربة: 100%");
        label.setTextSize(18);

        SeekBar bar = new SeekBar(this);
        bar.setMax(30);
        bar.setProgress(10);

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        boost = 1.0f + (p * 0.15f);
                        label.setText(
                                "الضربة: " +
                                (int)(boost * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                });

        root.addView(label);
        root.addView(bar);
    }

    private void addEcho(LinearLayout root) {

        TextView label = new TextView(this);
        label.setText("الصدى: 0%");
        label.setTextSize(18);

        SeekBar bar = new SeekBar(this);
        bar.setMax(20);
        bar.setProgress(0);

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        echo = p / 20.0f;
                        label.setText(
                                "الصدى: " +
                                (int)(echo * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                });

        root.addView(label);
        root.addView(bar);
    }

    private void addNoise(LinearLayout root) {

        TextView label = new TextView(this);
        label.setText("الوشوشة: 0%");
        label.setTextSize(18);

        SeekBar bar = new SeekBar(this);
        bar.setMax(20);
        bar.setProgress(0);

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        noise = p / 20.0f;
                        label.setText(
                                "الوشوشة: " +
                                (int)(noise * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                });

        root.addView(label);
        root.addView(bar);
    }

    private void addVolume(LinearLayout root) {

        TextView label = new TextView(this);
        label.setText("مستوى الصوت: 100%");
        label.setTextSize(18);

        SeekBar bar = new SeekBar(this);
        bar.setMax(20);
        bar.setProgress(10);

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar s, int p, boolean fromUser) {

                        volume = p / 10.0f;
                        label.setText(
                                "مستوى الصوت: " +
                                (int)(volume * 100) +
                                "%"
                        );
                    }

                    public void onStartTrackingTouch(SeekBar s) {}
                    public void onStopTrackingTouch(SeekBar s) {}
                });

        root.addView(label);
        root.addView(bar);
    }

    private void startAudio() {

        int min = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (min <= 0) {
            return;
        }

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                min * 2
        );

        player = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                min * 2,
                AudioTrack.MODE_STREAM
        );

        echoBuffer = new short[SAMPLE_RATE / 3];
        echoIndex = 0;
        running = true;

        recorder.startRecording();
        player.play();

        new Thread(() -> {

            short[] buffer = new short[min];

            while (running) {

                int read = recorder.read(
                        buffer,
                        0,
                        buffer.length
                );

                if (read > 0) {
                    processAudio(buffer, read);

                    player.write(
                            buffer,
                            0,
                            read
                    );
                }
            }

        }).start();
    }

    private void processAudio(short[] buffer, int length) {

        for (int i = 0; i < length; i++) {

            float sample = buffer[i];

            sample *= boost;

            if (echo > 0 && echoBuffer != null) {

                short delayed = echoBuffer[echoIndex];

                sample += delayed * (echo * 0.95f);

                echoBuffer[echoIndex] = (short)
                        Math.max(
                                -32768,
                                Math.min(
                                        32767,
                                        (int) sample
                                )
                        );

                echoIndex++;

                if (echoIndex >= echoBuffer.length) {
                    echoIndex = 0;
                }
            } else if (echoBuffer != null) {
                echoBuffer[echoIndex] = buffer[i];

                echoIndex++;

                if (echoIndex >= echoBuffer.length) {
                    echoIndex = 0;
                }
            }

            if (noise > 0) {

                double random =
                        (Math.random() * 2.0) - 1.0;

                sample +=
                        random *
                        5000.0f *
                        noise;
            }

            sample *= volume;

            if (sample > 32767) {
                sample = 32767;
            }

            if (sample < -32768) {
                sample = -32768;
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
