package com.argon.bybitwidget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#141820"));
        layout.setPadding(60, 60, 60, 60);

        TextView info = new TextView(this);
        info.setText("🛡️ Argon Bybit Pixel Widget\n\n1. Go to your Pixel Home Screen.\n2. Long-press empty space -> Widgets.\n3. Drag 'Argon Bybit Widget' to your screen.\n4. Tap the widget anytime for instant live sync!");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16f);
        layout.addView(info);

        Button btn = new Button(this);
        btn.setText("Force Refresh Widget Now");
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, BotWidgetProvider.class);
            intent.setAction(BotWidgetProvider.ACTION_REFRESH);
            sendBroadcast(intent);
        });
        layout.addView(btn);

        setContentView(layout);
    }
}
