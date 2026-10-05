package com.voicefx;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class RemoteSubmixTest {

    public interface ResultListener {
        void onResult(String result);
    }

    public static void monitor(
            final ResultListener listener) {

        if (Build.VERSION.SDK_INT < 24) {
            listener.onResult("Android أقل من 7.0");
            return;
        }

        final int sampleRate = 48000;

        final int bufferSize =
                AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) {
            listener.onResult(
                    "Buffer Error: " + bufferSize
            );
            return;
        }

        final AudioRecord record;

        try {

            record = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
            );

        } catch (Exception e) {

            listener.onResult(
                    "AudioRecord Error: " +
                    e.getMessage()
            );
            return;
        }

        if (record.getState()
                != AudioRecord.STATE_INITIALIZED) {

            record.release();

            listener.onResult(
                    "AudioRecord ما تهيأ"
            );

            return;
        }

        final StringBuilder result =
                new StringBuilder();

        result.append(
                "مراقبة المايك بدأت ✅\n\n"
        );

        record.registerAudioRecordingCallback(
                new AudioRecord.AudioRecordingCallback() {

                    @Override
                    public void onRecordingConfigChanged(
                            AudioRecordingConfiguration config) {

                        result.append(
                                "تغيير بالمصدر:\n"
                        );

                        result.append(
                                "Client Audio Source: "
                        );

                        result.append(
                                config.getClientAudioSource()
                        );

                        result.append("\n");

                        if (Build.VERSION.SDK_INT >= 29) {

                            result.append(
                                    "Silenced: "
                            );

                            result.append(
                                    config.isClientSilenced()
                            );

                            result.append("\n");
                        }

                        result.append(
                                "----------------\n"
                        );
                    }
                },
                new Handler(
                        Looper.getMainLooper()
                )
        );

        try {

            record.startRecording();

            result.append(
                    "RecordingState: "
            );

            result.append(
                    record.getRecordingState()
            );

            result.append("\n");

        } catch (Exception e) {

            record.release();

            listener.onResult(
                    "Start Error: " +
                    e.getMessage()
            );

            return;
        }

        new Thread(() -> {

            short[] buffer =
                    new short[bufferSize / 2];

            long start =
                    System.currentTimeMillis();

            int totalSamples = 0;

            while (
                    System.currentTimeMillis()
                    - start < 10000
            ) {

                int read =
                        record.read(
                                buffer,
                                0,
                                buffer.length,
                                AudioRecord.READ_NON_BLOCKING
                        );

                if (read > 0) {
                    totalSamples += read;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            try {
                record.stop();
            } catch (Exception ignored) {
            }

            record.release();

            result.append(
                    "\nانتهت المراقبة.\n"
            );

            result.append(
                    "Total PCM samples: "
            );

            result.append(
                    totalSamples
            );

            new Handler(
                    Looper.getMainLooper()
            ).post(() ->
                    listener.onResult(
                            result.toString()
                    )
            );

        }, "MicMonitor").start();
    }
}
