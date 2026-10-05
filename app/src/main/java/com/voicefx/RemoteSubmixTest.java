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
            return "Buffer Error: " + bufferSize;
        }

        AudioRecord record = null;

        try {

            record = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 4
            );

            if (record.getState()
                    != AudioRecord.STATE_INITIALIZED) {

                return "AudioRecord INITIALIZED = NO";
            }

            boolean preferred =
                    record.setPreferredDevice(target);

            if (!preferred) {
                return "ID 21 موجود لكن التوجيه فشل";
            }

            record.startRecording();

            int recordingState =
                    record.getRecordingState();

            short[] buffer =
                    new short[bufferSize / 2];

            int totalRead = 0;
            int positiveReads = 0;

            long start =
                    System.currentTimeMillis();

            while (System.currentTimeMillis() - start < 1500) {

                int read =
                        record.read(
                                buffer,
                                0,
                                buffer.length,
                                AudioRecord.READ_NON_BLOCKING
                        );

                if (read > 0) {
                    totalRead += read;
                    positiveReads++;
                }
            }

            record.stop();

            return
                    "ID 21 موجود ✅\n" +
                    "Preferred: true\n" +
                    "RecordingState: " +
                    recordingState +
                    "\n" +
                    "Positive reads: " +
                    positiveReads +
                    "\n" +
                    "Total PCM: " +
                    totalRead;

        } catch (SecurityException e) {

            return
                    "صلاحية النظام منعت القراءة ❌\n" +
                    e.getMessage();

        } catch (Exception e) {

            return
                    "خطأ ❌\n" +
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
