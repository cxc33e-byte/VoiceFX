package com.voicefx;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;

public class RemoteSubmixTest {

    public static String test(AudioManager audioManager) {

        if (Build.VERSION.SDK_INT < 23) {
            return "Android قديم";
        }

        if (audioManager == null) {
            return "AudioManager غير متوفر";
        }

        AudioDeviceInfo target = null;

        for (AudioDeviceInfo device :
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS)) {

            if (device.getId() == 21) {
                target = device;
                break;
            }
        }

        if (target == null) {
            return "ID 21 غير موجود";
        }

        final int sampleRate = 48000;

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) {
            return "Buffer Error: " + bufferSize;
        }

        AudioTrack player = null;
        AudioRecord record = null;

        try {

            AudioAttributes attributes =
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes.USAGE_MEDIA
                            )
                            .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SPEECH
                            )
                            .build();

            AudioFormat outputFormat =
                    new AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(
                                    AudioFormat.ENCODING_PCM_16BIT
                            )
                            .setChannelMask(
                                    AudioFormat.CHANNEL_OUT_MONO
                            )
                            .build();

            player = new AudioTrack(
                    attributes,
                    outputFormat,
                    bufferSize * 2,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );

            record = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 4
            );

            if (player.getState()
                    != AudioTrack.STATE_INITIALIZED) {

                return "AudioTrack فشل بالتهيئة";
            }

            if (record.getState()
                    != AudioRecord.STATE_INITIALIZED) {

                return "AudioRecord فشل بالتهيئة";
            }

            boolean preferred =
                    record.setPreferredDevice(target);

            if (!preferred) {
                return "توجيه AudioRecord إلى ID 21 فشل";
            }

            short[] tone =
                    new short[4800];

            for (int i = 0; i < tone.length; i++) {

                double angle =
                        2.0 * Math.PI * 440.0 *
                        i / sampleRate;

                tone[i] =
                        (short)
                        (Math.sin(angle) * 10000);
            }

            player.play();
            record.startRecording();

            player.write(
                    tone,
                    0,
                    tone.length
            );

            short[] input =
                    new short[bufferSize / 2];

            int totalRead = 0;
            int positiveReads = 0;

            long start =
                    System.currentTimeMillis();

            while (System.currentTimeMillis() - start < 1500) {

                int read =
                        record.read(
                                input,
                                0,
                                input.length,
                                AudioRecord.READ_NON_BLOCKING
                        );

                if (read > 0) {
                    totalRead += read;
                    positiveReads++;
                }
            }

            return
                    "ID 21 موجود ✅\n" +
                    "Preferred: " + preferred + "\n" +
                    "Positive reads: " +
                    positiveReads + "\n" +
                    "Total PCM: " +
                    totalRead;

        } catch (SecurityException e) {

            return
                    "النظام منع الالتقاط ❌\n" +
                    e.getMessage();

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

            if (player != null) {

                try {
                    player.stop();
                } catch (Exception ignored) {}

                player.release();
            }
        }
    }
}
