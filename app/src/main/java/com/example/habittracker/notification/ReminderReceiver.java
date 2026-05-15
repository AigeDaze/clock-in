package com.example.habittracker.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.habittracker.ui.MainActivity;
import com.example.habittracker.NotificationActionReceiver;
import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Goal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "habit_reminder_channel";
    private static final String ACTION_REMINDER = "com.example.habittracker.REMINDER_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_REMINDER.equals(intent.getAction())) return;

        long goalId = intent.getLongExtra("goal_id", -1);
        String goalTitle = intent.getStringExtra("goal_title");
        String goalMotivation = intent.getStringExtra("goal_motivation");

        if (goalId == -1 || goalTitle == null) return;

        // Android 13+ 通知权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // 检查今日是否已打卡，已打卡则不提醒
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        AppDatabase db = AppDatabase.getInstance(context);
        if (db.checkinDao().hasCheckedIn(goalId, today) > 0) {
            rescheduleForTomorrow(context, goalId, goalTitle, goalMotivation);
            return;
        }

        createNotificationChannel(context);
        showNotification(context, goalId, goalTitle, goalMotivation);
        rescheduleForTomorrow(context, goalId, goalTitle, goalMotivation);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_description));

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, long goalId, String goalTitle, String goalMotivation) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, (int) goalId, openIntent, flags);

        // 打卡快捷按钮
        Intent checkinIntent = new Intent(context, NotificationActionReceiver.class);
        checkinIntent.setAction(NotificationActionReceiver.ACTION_CHECKIN);
        checkinIntent.putExtra("goal_id", goalId);

        PendingIntent checkinPendingIntent = PendingIntent.getBroadcast(
                context, (int) goalId + 10000, checkinIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.reminder_notification_text, goalTitle))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.reminder_notification_text, goalTitle)
                                + (goalMotivation != null && !goalMotivation.isEmpty()
                                        ? "\n\"" + goalMotivation + "\"" : "")))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_input_add, context.getString(R.string.checkin_btn), checkinPendingIntent);

        NotificationManagerCompat.from(context).notify((int) goalId, builder.build());
    }

    private void rescheduleForTomorrow(Context context, long goalId, String goalTitle, String goalMotivation) {
        Goal goal = AppDatabase.getInstance(context).goalDao().getById(goalId);
        if (goal != null) {
            ReminderHelper.scheduleReminder(context, goal);
            return;
        }
        // fallback: 数据库中找不到目标时，使用 intent 中的信息
        Calendar now = Calendar.getInstance();
        Goal tempGoal = new Goal(goalTitle, goalMotivation != null ? goalMotivation : "",
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        tempGoal.setId(goalId);
        ReminderHelper.scheduleReminder(context, tempGoal);
    }
}
