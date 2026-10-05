package com.voicefx;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

public class AudioDeviceChecker {

    public static String check(Context context) {

        StringBuilder result = new StringBuilder();

        result.append("الزاجل - مخارج الصوت\n\n");

        if (Build.VERSION.SDK_INT < 23) {
            return result.append(
                    "Android قديم"
            ).toString();
        }

        AudioManager audioManager =
                (AudioManager) context.getSystemService(
                        Context.AUDIO_SERVICE
                );

        if (audioManager == null) {
            return "AudioManager غير متوفر";
        }

        AudioDeviceInfo[] devices =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_OUTPUTS
                );

        result.append(
                "عدد مخارج الصوت: "
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
