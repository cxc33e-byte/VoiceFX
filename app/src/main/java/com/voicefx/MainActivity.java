package com.voicefx;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.media.AudioManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Button startButton;
    private Button outputButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    100
            );
        }

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER
        );

        layout.setPadding(
                30, 30, 30, 30
        );

        TextView title =
                new TextView(this);

        title.setText("الزاجل");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        TextView info =
                new TextView(this);

        info.setText(
                "مؤثرات الصوت + فحص أجهزة الصوت"
        );

        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);

        layout.addView(info);

        startButton =
                new Button(this);

        startButton.setText(
                "تشغيل الزاجل"
        );

        startButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            VoiceService.class
                    );

            if (android.os.Build.VERSION.SDK_INT >= 26) {

                startForegroundService(intent);

            } else {

                startService(intent);
            }

            startButton.setText(
                    "الزاجل يعمل ✅"
            );
        });

        layout.addView(startButton);

        outputButton =
                new Button(this);

        outputButton.setText(
                "فحص مخارج الصوت"
        );

        outputButton.setOnClickListener(v -> {

            AudioManager audioManager =
                    (AudioManager)
                            getSystemService(
                                    AUDIO_SERVICE
                            );

            resultText.setText(
                    AudioDeviceChecker.check(
                            MainActivity.this
                    )
            );
        });

        layout.addView(outputButton);

        resultText =
                new TextView(this);

        resultText.setTextSize(16);

        resultText.setPadding(
                10, 20, 10, 10
        );

        layout.addView(resultText);

        setContentView(layout);
    }
}
