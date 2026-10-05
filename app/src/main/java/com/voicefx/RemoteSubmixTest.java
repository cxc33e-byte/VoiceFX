package com.voicefx;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;

public class RemoteSubmixTest {

    public static String test(AudioManager audioManager) {

        if (Build.VERSION.SDK_INT < 23) {
            return "Android قديم";
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

        int sampleRate = 48000;

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        if (bufferSize <= 0) {
            return "تعذر إنشاء buffer";
        }

        AudioRecord record = null;

        try {

            record = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
            );

            if (record.getState()
                    != AudioRecord.STATE_INITIALIZED) {

                return "AudioRecord فشل بالتهيئة";
            }

            boolean accepted =
                    record.setPreferredDevice(target);

            String result =
                    "ID 21 موجود\n" +
                    "الاسم: " +
                    target.getProductName() +
                    "\n" +
                    "setPreferredDevice: " +
                    accepted;

            record.startRecording();

            short[] buffer =
                    new short[bufferSize / 2];

            int read =
                    record.read(
                            buffer,
                            0,
                            buffer.length
                    );

            record.stop();

            result +=
                    "\nread PCM samples: " +
                    read;

            return result;

        } catch (Exception e) {

            return "خطأ: " +
                    e.getClass().getSimpleName() +
                    "\n" +
                    e.getMessage();

        } finally {

            if (record != null) {
                record.release();
            }
        }
    }
}
