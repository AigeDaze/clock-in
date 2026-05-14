package com.example.habittracker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Checkin;
import com.example.habittracker.data.Goal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_CHECKIN = "com.example.habittracker.WIDGET_CHECKIN";

    private static final int[] SLOT_LAYOUT_IDS = {
            R.id.widget_slot_1, R.id.widget_slot_2, R.id.widget_slot_3,
            R.id.widget_slot_4, R.id.widget_slot_5, R.id.widget_slot_6
    };

    private static final int[] GOAL_NAME_IDS = {
            R.id.widget_goal_name_1, R.id.widget_goal_name_2, R.id.widget_goal_name_3,
            R.id.widget_goal_name_4, R.id.widget_goal_name_5, R.id.widget_goal_name_6
    };

    private static final int[] BTN_IDS = {
            R.id.widget_btn_1, R.id.widget_btn_2, R.id.widget_btn_3,
            R.id.widget_btn_4, R.id.widget_btn_5, R.id.widget_btn_6
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_WIDGET_CHECKIN.equals(intent.getAction())) {
            long goalId = intent.getLongExtra("goal_id", -1);
            if (goalId != -1) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                AppDatabase db = AppDatabase.getInstance(context);
                if (db.checkinDao().hasCheckedIn(goalId, today) == 0) {
                    db.checkinDao().insert(new Checkin(goalId, today));
                }
                // 刷新所有小组件
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName comp = new ComponentName(context, HabitWidgetProvider.class);
                int[] widgetIds = mgr.getAppWidgetIds(comp);
                updateWidgets(context, mgr, widgetIds);
            }
            return;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    private void updateWidgets(Context context, AppWidgetManager mgr, int[] widgetIds) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Goal> goals = AppDatabase.getInstance(context).goalDao().getAllGoalsSync();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            for (Goal goal : goals) {
                List<String> dates = AppDatabase.getInstance(context)
                        .checkinDao().getCheckinDatesForGoal(goal.getId());
                boolean checkedInToday = dates.contains(today);
                goal.setCheckedInToday(checkedInToday);
            }

            for (int widgetId : widgetIds) {
                RemoteViews views = buildRemoteViews(context, goals, widgetId);
                mgr.updateAppWidget(widgetId, views);
            }
        });
    }

    private RemoteViews buildRemoteViews(Context context, List<Goal> goals, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_habit);

        // 筛选未打卡目标
        List<Goal> pending = new java.util.ArrayList<>();
        for (Goal g : goals) {
            if (!g.isCheckedInToday()) {
                pending.add(g);
            }
        }

        // 隐藏所有 slot
        for (int slotId : SLOT_LAYOUT_IDS) {
            views.setViewVisibility(slotId, View.GONE);
        }

        // 填充未打卡目标到 slot
        int slotCount = Math.min(pending.size(), SLOT_LAYOUT_IDS.length);
        for (int i = 0; i < slotCount; i++) {
            Goal goal = pending.get(i);
            views.setViewVisibility(SLOT_LAYOUT_IDS[i], View.VISIBLE);
            views.setTextViewText(GOAL_NAME_IDS[i], goal.getTitle());

            // 打卡按钮 PendingIntent
            Intent checkinIntent = new Intent(context, HabitWidgetProvider.class);
            checkinIntent.setAction(ACTION_WIDGET_CHECKIN);
            checkinIntent.putExtra("goal_id", goal.getId());

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            int requestCode = widgetId * 1000 + (int) goal.getId();
            PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, checkinIntent, flags);
            views.setOnClickPendingIntent(BTN_IDS[i], pi);
        }

        // 全部完成 / 无目标
        if (pending.isEmpty() && !goals.isEmpty()) {
            views.setViewVisibility(R.id.widget_all_done, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_all_done, View.GONE);
        }

        return views;
    }
}
