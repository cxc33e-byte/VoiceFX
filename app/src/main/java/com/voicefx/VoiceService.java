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
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;

    // المؤثرات
    private float boost = 1.8f;
    private float volume = 1.0f;
    private float echo = 0.25f;
    private float noise = 0.0f;

    private short[] echoBuffer;
    private int echoIndex;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(this, "alzajel")
                        .setContentTitle("الزاجل")
                        .setContentText("معالجة الصوت تعمل")
                        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                        .setOngoing(true)
                        .build();

        startForeground(1001, notification);

        startAudio();
    }

    private void startAudio() {

        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_IN,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (bufferSize <= 0) {
            return;
        }

        bufferSize *= 2;

        try {

            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            if (recorder.getState()
                    != AudioRecord.STATE_INITIALIZED) {

                stopAudio();
                return;
            }

            player = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SPEECH
                            )
                            .build(),

                    new AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(
                                    AudioFormat.ENCODING_PCM_16BIT
                            )
                            .setChannelMask(CHANNEL_OUT)
                            .build(),

                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );

            if (player.getState()
                    != AudioTrack.STATE_INITIALIZED) {

                stopAudio();
                return;
            }

            // 330ms تقريباً من الصدى
            echoBuffer = new short[SAMPLE_RATE / 3];
            echoIndex = 0;

            recorder.startRecording();
            player.play();

            running = true;

            audioThread = new Thread(() -> {

                short[] buffer =
                        new short[bufferSize / 2];

                while (running) {

                    int read = recorder.read(
                            buffer,
                            0,
                            buffer.length
                    );

                    if (read > 0) {

                        processAudio(
                                buffer,
                                read
                        );

                        /*
                         * هذا هو الـPCM المعالج.
                         *
                         * لاحقاً نربطه بطبقة
                         * Virtual Microphone.
                         */
                        sendProcessedAudio(
                                buffer,
                                read
                        );

                        // حالياً فقط للمراقبة المحلية
                        player.write(
                                buffer,
                                0,
                                read
                        );
                    }
                }

            }, "AlZajelAudio");

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
            if (echo > 0.0f) {

                short delayed =
                        echoBuffer[echoIndex];

                sample +=
                        delayed * echo;
            }

            // الوشوشة
            if (noise > 0.0f) {

                double random =
                        Math.random() * 2.0 - 1.0;

                sample +=
                        random *
                        3000.0f *
                        noise;
            }

            // منع التشويش الرقمي
            if (sample > 32767) {
                sample = 32767;
            }

            if (sample < -32768) {
                sample = -32768;
            }

            short output =
                    (short) sample;

            echoBuffer[echoIndex] =
                    output;

            echoIndex++;

            if (echoIndex >= echoBuffer.length) {
                echoIndex = 0;
            }

            buffer[i] = output;
        }
    }

    /*
     * نقطة خروج الصوت المعالج.
     *
     * حالياً لا نرسله لأي تطبيق آخر.
     * هذه النقطة هي التي سنربطها لاحقاً
     * بالـVirtual Microphone إذا كان الحل
     * المستخدم يوفر واجهة لذلك.
     */
    private void sendProcessedAudio(
            short[] buffer,
            int length) {

        // الصوت المعالج موجود هنا كـPCM 16-bit mono.
        // لا نرسل الصوت إلى Telegram مباشرة،
        // لأن Android يمنع التطبيق العادي من
        // استبدال ميكروفون تطبيق آخر.
    }

    private void stopAudio() {

        running = false;

        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }

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
        echoIndex = 0;
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

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
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
    public IBinder onBind(Intent intent) {
        return null;
    }
}
