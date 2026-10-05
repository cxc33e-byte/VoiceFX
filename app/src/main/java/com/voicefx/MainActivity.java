package com.voicefx;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.Manifest;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("الزاجل");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        TextView info = new TextView(this);
        info.setText(
                "الضربة + الصدى + الصوت + الوشوشة"
        );
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);

        layout.addView(info);

        button = new Button(this);
        button.setText("تشغيل الزاجل");

        button.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            VoiceService.class
                    );

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

            button.setText("الزاجل يعمل ✅");
        });

        layout.addView(button);

        setContentView(layout);
    }
}
