package com.voicefx;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

public class RemoteSubmixTest {

    public static String test(
            android.media.AudioManager audioManager) {

        if (Build.VERSION.SDK_INT < 23) {
            return "Android قديم";
        }

        int sampleRate = 48000;

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) {
            return "Buffer Error";
        }

        AudioRecord record = null;

        try {

            record = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
            );

            if (record.getState()
                    != AudioRecord.STATE_INITIALIZED) {

                return "AudioRecord فشل";
            }

            record.startRecording();

            StringBuilder result =
                    new StringBuilder();

            result.append(
                    "بدأ الاختبار لمدة 15 ثانية\n\n"
            );

            result.append(
                    "هسه افتح Telegram وسوِّ تسجيل صوتي.\n\n"
            );

            short[] buffer =
                    new short[bufferSize / 2];

            int totalSamples = 0;
            int positiveReads = 0;
            int zeroReads = 0;

            long start =
                    System.currentTimeMillis();

            while (
                    System.currentTimeMillis()
                    - start < 15000
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
                    positiveReads++;

                } else if (read == 0) {

                    zeroReads++;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            result.append(
                    "انتهى الاختبار.\n\n"
            );

            result.append(
                    "RecordingState: "
            );

            result.append(
                    record.getRecordingState()
            );

            result.append("\n");

            result.append(
                    "Positive reads: "
            );

            result.append(
                    positiveReads
            );

            result.append("\n");

            result.append(
                    "Zero reads: "
            );

            result.append(
                    zeroReads
            );

            result.append("\n");

            result.append(
                    "Total PCM samples: "
            );

            result.append(
                    totalSamples
            );

            return result.toString();

        } catch (Exception e) {

            return
                    "خطأ:\n" +
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
