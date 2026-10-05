package com.voicefx;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

public class AudioDeviceChecker {

    public static String check(Context context) {

        if (Build.VERSION.SDK_INT < 23) {
            return "Android قديم";
        }

        AudioManager audioManager =
                (AudioManager) context.getSystemService(
                        Context.AUDIO_SERVICE
                );

        if (audioManager == null) {
            return "AudioManager غير متوفر";
        }

        StringBuilder result = new StringBuilder();

        result.append("الزاجل - فحص الصوت\n\n");

        // INPUTS
        result.append("===== INPUTS =====\n\n");

        AudioDeviceInfo[] inputs =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        for (AudioDeviceInfo device : inputs) {

            result.append("الاسم: ")
                    .append(device.getProductName())
                    .append("\n");

            result.append("النوع: ")
                    .append(device.getType())
                    .append("\n");

            result.append("ID: ")
                    .append(device.getId())
                    .append("\n");

            result.append("Sample Rates: ");

            int[] rates = device.getSampleRates();

            if (rates != null && rates.length > 0) {
                for (int rate : rates) {
                    result.append(rate).append(" ");
                }
            } else {
                result.append("غير متوفر");
            }

            result.append("\n");

            result.append("Channels: ");

            int[] channels = device.getChannelCounts();

            if (channels != null && channels.length > 0) {
                for (int channel : channels) {
                    result.append(channel).append(" ");
                }
            } else {
                result.append("غير متوفر");
            }

            result.append("\n");

            result.append("----------------\n");
        }

        // OUTPUTS
        result.append("\n===== OUTPUTS =====\n\n");

        AudioDeviceInfo[] outputs =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_OUTPUTS
                );

        for (AudioDeviceInfo device : outputs) {

            result.append("الاسم: ")
                    .append(device.getProductName())
                    .append("\n");

            result.append("النوع: ")
                    .append(device.getType())
                    .append("\n");

            result.append("ID: ")
                    .append(device.getId())
                    .append("\n");

            result.append("Sample Rates: ");

            int[] rates = device.getSampleRates();

            if (rates != null && rates.length > 0) {
                for (int rate : rates) {
                    result.append(rate).append(" ");
                }
            } else {
                result.append("غير متوفر");
            }

            result.append("\n");

            result.append("Channels: ");

            int[] channels = device.getChannelCounts();

            if (channels != null && channels.length > 0) {
                for (int channel : channels) {
                    result.append(channel).append(" ");
                }
            } else {
                result.append("غير متوفر");
            }

            result.append("\n");

            result.append("----------------\n");
        }

        return result.toString();
    }
}
