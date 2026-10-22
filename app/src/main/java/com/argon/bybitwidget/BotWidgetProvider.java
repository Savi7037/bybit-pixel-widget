package com.argon.bybitwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class BotWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.argon.bybitwidget.ACTION_REFRESH";
    private static final String STATUS_URL = "http://129.121.170.177:8765/status.json";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, BotWidgetProvider.class));
            onUpdate(context, manager, ids);
        }
    }

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        Intent intent = new Intent(context, BotWidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_bot);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        views.setTextViewText(R.id.tv_updated, "Syncing...");
        manager.updateAppWidget(widgetId, views);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(STATUS_URL + "?t=" + System.currentTimeMillis());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    views.setTextViewText(R.id.tv_title, "🛡️ ARGON: " + json.optString("argon", "ACTIVE"));
                    views.setTextViewText(R.id.tv_equity, "Eq: $" + json.optDouble("equity", 0.0));
                    views.setTextViewText(R.id.tv_price, "BTC: $" + json.optDouble("price", 0.0));
                    views.setTextViewText(R.id.tv_gz, "Golden Zone (0.5-0.618): " + json.optString("golden_zone", "--"));
                    views.setTextViewText(R.id.tv_signal, json.optString("signal", "--"));

                    views.setTextViewText(R.id.tv_open_pos, json.optString("open_pos", "FLAT"));
                    double runPnl = json.optDouble("running_pnl", 0.0);
                    views.setTextViewText(R.id.tv_running_pnl, json.optString("running_pnl_str", "$0.00"));
                    views.setTextColor(R.id.tv_running_pnl, Color.parseColor(runPnl >= 0 ? "#00E676" : "#FF5252"));

                    double todayPnl = json.optDouble("closed_today_pnl", 0.0);
                    views.setTextViewText(R.id.tv_closed_today, json.optString("closed_today_str", "0 ($0.00)"));
                    views.setTextColor(R.id.tv_closed_today, Color.parseColor(todayPnl >= 0 ? "#00E676" : "#FF5252"));

                    views.setTextViewText(R.id.tv_last_trade, json.optString("last_trade", "None"));
                    views.setTextViewText(R.id.tv_updated, json.optString("updated", "") + " ↻");
                } else {
                    views.setTextViewText(R.id.tv_signal, "⚠️ HTTP Error");
                    views.setTextViewText(R.id.tv_updated, "Retry");
                }
            } catch (Exception e) {
                views.setTextViewText(R.id.tv_signal, "⚠️ Connection Fail");
                views.setTextViewText(R.id.tv_updated, "Tap ↻");
            }
            manager.updateAppWidget(widgetId, views);
        });
    }
}
