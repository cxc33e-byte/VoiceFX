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

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل - فحص المايكات");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        Button scan = new Button(this);
        scan.setText("فحص مصادر المايك");
        layout.addView(scan);

        result = new TextView(this);
        result.setTextSize(18);
        result.setPadding(10, 30, 10, 10);

        layout.addView(result);

        scan.setOnClickListener(v -> scanDevices());

        setContentView(layout);
    }

    private void scanDevices() {

        AudioManager manager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        AudioDeviceInfo[] devices =
                manager.getDevices(AudioManager.GET_DEVICES_INPUTS);

        StringBuilder text = new StringBuilder();

        text.append("عدد مصادر الإدخال: ")
                .append(devices.length)
                .append("\n\n");

        for (int i = 0; i < devices.length; i++) {

            AudioDeviceInfo device = devices[i];

            int type = device.getType();
            int id = device.getId();

            text.append("المصدر ")
                    .append(i + 1)
                    .append("\n");

            text.append("الاسم: ")
                    .append(device.getProductName())
                    .append("\n");

            text.append("النوع: ")
                    .append(getTypeName(type))
                    .append("\n");

            text.append("TYPE: ")
                    .append(type)
                    .append("\n");

            text.append("ID: ")
                    .append(id)
                    .append("\n");

            text.append("--------------------\n\n");
        }

        result.setText(text.toString());
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
                return "TELEPHONY - الاتصالات";

            default:
                return "OTHER - غير معروف";
        }
    }
}
