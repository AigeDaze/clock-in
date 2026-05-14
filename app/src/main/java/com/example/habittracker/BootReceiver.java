package com.example.habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Goal;
import com.example.habittracker.notification.ReminderHelper;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<Goal> goals = AppDatabase.getInstance(context).goalDao().getAllGoalsSync();
                for (Goal goal : goals) {
                    ReminderHelper.scheduleReminder(context, goal);
                }
            });
        }
    }
}
