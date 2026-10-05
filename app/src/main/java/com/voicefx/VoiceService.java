package com.voicefx;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;

public class VoiceService extends Service {

    private AudioRecord recorder;
    private AudioTrack player;
    private Thread audioThread;

    private volatile boolean running = false;

    private static final int SAMPLE_RATE = 44100;

    private float boost = 1.5f;
    private float echo = 0.0f;
    private float volume = 1.0f;
    private float noise = 0.0f;

    private short[] echoBuffer;
    private int echoIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(
                        this,
                        "alzajel"
                )
                .setContentTitle("الزاجل")
                .setContentText("المؤثرات الصوتية تعمل")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        startForeground(1001, notification);

        startAudio();
    }

    private void startAudio() {

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) return;

        try {

            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
            );

            player = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2,
                    AudioTrack.MODE_STREAM
            );

            echoBuffer =
                    new short[SAMPLE_RATE / 3];

            echoIndex = 0;

            recorder.startRecording();
            player.play();

            running = true;

            audioThread = new Thread(() -> {

                short[] buffer =
                        new short[bufferSize];

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

            });

            audioThread.start();

        } catch (Exception e) {
            stopAudio();
        }
    }

    private void processAudio(
            short[] buffer,
            int length) {

        for (int i = 0; i < length; i++) {

            float sample = buffer[i];

            // الضربة
            sample *= boost;

            // مستوى الصوت
            sample *= volume;

            // الصدى
            if (echo > 0) {

                short delayed =
                        echoBuffer[echoIndex];

                sample +=
                        delayed *
                        (echo * 0.8f);
            }

            // الوشوشة
            if (noise > 0) {

                double random =
                        Math.random() * 2.0 - 1.0;

                sample +=
                        random *
                        3000.0f *
                        noise;
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

            buffer[i] =
                    (short) sample;
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

        if (audioThread != null) {
            audioThread = null;
        }

        echoBuffer = null;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "alzajel",
                            "الزاجل",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        stopAudio();

        super.onDestroy();
    }

    @Override
    public android.os.IBinder onBind(
            Intent intent) {

        return null;
    }
}
