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
        root.setPadding(30, 40, 30, 30);
        root.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("🎙️ VoiceFX");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        addSlider(root, "Bass");
        addSlider(root, "Treble");
        addSlider(root, "Echo");

        Button button = new Button(this);
        button.setText("🎙️ تشغيل المايك");

        button.setOnClickListener(v -> {
            if (!running) {
                startAudio();
                button.setText("⏹️ إيقاف المايك");
            } else {
                stopAudio();
                button.setText("🎙️ تشغيل المايك");
            }
        });

        root.addView(button);

        setContentView(root);
    }

    private void addSlider(LinearLayout root, String name) {
        TextView label = new TextView(this);
        label.setText(name + ": 0");
        label.setTextSize(18);

        SeekBar bar = new SeekBar(this);
        bar.setMax(20);
        bar.setProgress(10);

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {
                        label.setText(name + ": " + (progress - 10));
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    public void onStopTrackingTouch(SeekBar seekBar) {}
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

        if (min <= 0) return;

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
                    player.write(
                            buffer,
                            0,
                            read
                    );
                }
            }

        }).start();
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
    }

    @Override
    protected void onDestroy() {
        stopAudio();
        super.onDestroy();
    }
}
