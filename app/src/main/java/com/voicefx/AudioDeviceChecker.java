package com.voicefx;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

public class AudioDeviceChecker {

    public static String check(Context context) {

        StringBuilder result = new StringBuilder();

        result.append("الزاجل - فحص Audio Input\n\n");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return result.append(
                    "إصدار أندرويد قديم"
            ).toString();
        }

        AudioManager audioManager =
                (AudioManager) context.getSystemService(
                        Context.AUDIO_SERVICE
                );

        if (audioManager == null) {
            return result.append(
                    "AudioManager غير متوفر"
            ).toString();
        }

        AudioDeviceInfo[] devices =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        if (devices.length == 0) {
            result.append("ماكو أجهزة إدخال.\n");
            return result.toString();
        }

        result.append(
                "عدد أجهزة الإدخال: "
        ).append(devices.length)
         .append("\n\n");

        for (AudioDeviceInfo device : devices) {

            result.append("الاسم: ")
                    .append(device.getProductName())
                    .append("\n");

            result.append("النوع: ")
                    .append(device.getType())
                    .append("\n");

            result.append("ID: ")
                    .append(device.getId())
                    .append("\n");

            result.append("----------------\n");
        }

        return result.toString();
    }
}
