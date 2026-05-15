package com.example.habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Checkin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_CHECKIN = "com.example.habittracker.CHECKIN_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_CHECKIN.equals(intent.getAction())) return;

        long goalId = intent.getLongExtra("goal_id", -1);
        if (goalId == -1) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        AppDatabase db = AppDatabase.getInstance(context);
        int count = db.checkinDao().hasCheckedIn(goalId, today);

        if (count > 0) {
            Toast.makeText(context, R.string.already_checked_in, Toast.LENGTH_SHORT).show();
        } else {
            db.checkinDao().insert(new Checkin(goalId, today));
            Toast.makeText(context, R.string.checkin_success_toast, Toast.LENGTH_SHORT).show();
        }

        NotificationManagerCompat.from(context).cancel((int) goalId);
    }
}
