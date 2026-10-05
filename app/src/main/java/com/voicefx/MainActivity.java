package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.widget.*;
import android.view.Gravity;

public class MainActivity extends Activity {

    private LinearLayout root;
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

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
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

        scan.setOnClickListener(v -> scanDevices());

        root.addView(scan);

        result = new TextView(this);
        result.setTextSize(18);
        result.setPadding(10, 30, 10, 10);

        root.addView(result);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void scanDevices() {

        AudioManager manager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        AudioDeviceInfo[] devices =
                manager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        result.setText(
                "عدد مصادر الإدخال: " +
                devices.length +
                "\n\n"
        );

        for (int i = 0; i < devices.length; i++) {

            AudioDeviceInfo device = devices[i];

            String name =
                    String.valueOf(
                            device.getProductName()
                    );

            int type =
                    device.getType();

            String typeName =
                    getTypeName(type);

            result.append(
                    "المصدر " +
                    (i + 1) +
                    "\n" +
                    "الاسم: " +
                    name +
                    "\n" +
                    "النوع: " +
                    typeName +
                    "\n" +
                    "TYPE: " +
                    type +
                    "\n" +
                    "ID: " +
                    device.getId() +
                    "\n\n"
            );
        }
    }

    private String getTypeName(int type) {

        switch (type) {

            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "BUILTIN_MIC - مايك الجهاز";

            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                return "BUILTIN_EARPIECE";

            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "WIRED_HEADSET";

            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                return "WIRED_HEADPHONES";

            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "BLUETOOTH_SCO";

            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                return "BLUETOOTH_A2DP";

            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB_DEVICE";

            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return "USB_HEADSET";

            case AudioDeviceInfo.TYPE_TELEPHONY:
                return "TELEPHONY";

            default:
                return "OTHER";
        }
    }
}
