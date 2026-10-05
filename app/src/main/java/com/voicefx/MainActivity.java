package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        buildUI();
    }

    private void buildUI() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        Button scan = new Button(this);
        scan.setText("فحص مصادر المايك");

        scan.setOnClickListener(v -> scanAudioDevices());

        root.addView(scan);

        result = new TextView(this);
        result.setTextSize(17);
        result.setPadding(10, 30, 10, 10);

        root.addView(result);

        setContentView(root);
    }

    private void scanAudioDevices() {

        AudioManager audioManager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        AudioDeviceInfo[] devices =
                audioManager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        StringBuilder text = new StringBuilder();

        text.append("مصادر الإدخال الموجودة:\n\n");

        if (devices.length == 0) {
            text.append("ماكو مصدر إدخال ظاهر للنظام.");
        }

        for (AudioDeviceInfo device : devices) {

            text.append("الاسم: ")
                    .append(device.getProductName())
                    .append("\n");

            text.append("النوع: ")
                    .append(deviceTypeName(device.getType()))
                    .append("\n");

            text.append("ID: ")
                    .append(device.getId())
                    .append("\n\n");
        }

        result.setText(text.toString());
    }

    private String deviceTypeName(int type) {

        switch (type) {

            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "مايك الجهاز";

            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth";

            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "سماعة سلكية";

            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB";

            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return "USB Headset";

            default:
                return "نوع آخر: " + type;
        }
    }
}
