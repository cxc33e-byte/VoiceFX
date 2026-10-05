package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private TextView result;
    private Handler handler = new Handler();
    private boolean monitoring = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(25, 25, 25, 25);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        Button button = new Button(this);
        button.setText("مراقبة أجهزة الصوت");
        layout.addView(button);

        result = new TextView(this);
        result.setTextSize(16);
        result.setPadding(10, 20, 10, 10);
        layout.addView(result);

        button.setOnClickListener(v -> {

            if (!monitoring) {
                monitoring = true;
                button.setText("إيقاف المراقبة");
                monitor();
            } else {
                monitoring = false;
                button.setText("مراقبة أجهزة الصوت");
            }
        });

        setContentView(layout);
    }

    private void monitor() {

        if (!monitoring) return;

        AudioManager am =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        StringBuilder s = new StringBuilder();

        s.append("INPUTS\n\n");

        AudioDeviceInfo[] inputs =
                am.getDevices(AudioManager.GET_DEVICES_INPUTS);

        for (AudioDeviceInfo d : inputs) {

            s.append("NAME: ")
                    .append(d.getProductName())
                    .append("\n");

            s.append("TYPE: ")
                    .append(d.getType())
                    .append("\n");

            s.append("ID: ")
                    .append(d.getId())
                    .append("\n");

            s.append("----------------\n");
        }

        s.append("\nOUTPUTS\n\n");

        AudioDeviceInfo[] outputs =
                am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

        for (AudioDeviceInfo d : outputs) {

            s.append("NAME: ")
                    .append(d.getProductName())
                    .append("\n");

            s.append("TYPE: ")
                    .append(d.getType())
                    .append("\n");

            s.append("ID: ")
                    .append(d.getId())
                    .append("\n");

            s.append("----------------\n");
        }

        s.append("\nMODE: ")
                .append(am.getMode());

        result.setText(s.toString());

        handler.postDelayed(
                this::monitor,
                1000
        );
    }

    @Override
    protected void onDestroy() {
        monitoring = false;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
