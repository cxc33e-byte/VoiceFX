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

        AudioManager am =
                (AudioManager) context.getSystemService(
                        Context.AUDIO_SERVICE
                );

        if (am == null) {
            return "AudioManager غير متوفر";
        }

        StringBuilder r = new StringBuilder();

        r.append("INPUT DEVICES\n\n");

        AudioDeviceInfo[] inputs =
                am.getDevices(AudioManager.GET_DEVICES_INPUTS);

        for (AudioDeviceInfo d : inputs) {

            r.append("NAME: ")
                    .append(d.getProductName())
                    .append("\n");

            r.append("TYPE: ")
                    .append(d.getType())
                    .append("\n");

            r.append("ID: ")
                    .append(d.getId())
                    .append("\n");

            r.append("----------------\n");
        }

        r.append("\nMIC ACTIVE: ")
                .append(am.isMicrophoneMute() ? "MUTED" : "ACTIVE");

        r.append("\n\nCOMMUNICATION MODE: ")
                .append(am.getMode());

        return r.toString();
    }
}
