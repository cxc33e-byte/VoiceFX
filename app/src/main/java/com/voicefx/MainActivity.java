package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.*;
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

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        Button test = new Button(this);
        test.setText("اختبار المصدر TYPE 25 - ID 21");

        layout.addView(test);

        result = new TextView(this);
        result.setTextSize(18);
        result.setGravity(Gravity.CENTER);
        result.setPadding(10, 30, 10, 10);

        layout.addView(result);

        test.setOnClickListener(v -> testSource());

        setContentView(layout);
    }

    private void testSource() {

        AudioManager manager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        AudioDeviceInfo[] devices =
                manager.getDevices(
                        AudioManager.GET_DEVICES_INPUTS
                );

        AudioDeviceInfo target = null;

        for (AudioDeviceInfo device : devices) {

            if (device.getId() == 21) {
                target = device;
                break;
            }
        }

        if (target == null) {

            result.setText(
                    "المصدر ID 21 غير موجود حالياً."
            );

            return;
        }

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        try {

            AudioRecord recorder =
                    new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            44100,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize * 2
                    );

            boolean accepted =
                    recorder.setPreferredDevice(target);

            result.setText(
                    "ID: 21\n" +
                    "TYPE: 25\n\n" +
                    "نتيجة اختيار المصدر:\n" +
                    (accepted
                            ? "تم قبول المصدر من النظام ✅"
                            : "النظام رفض المصدر ❌")
            );

            recorder.release();

        } catch (Exception e) {

            result.setText(
                    "صار خطأ:\n\n" +
                    e.getClass().getSimpleName() +
                    "\n" +
                    e.getMessage()
            );
        }
    }
}
