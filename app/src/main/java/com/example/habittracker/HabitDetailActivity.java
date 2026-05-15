package com.example.habittracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.habittracker.ui.BaseActivity;

import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Checkin;
import com.example.habittracker.data.Goal;
import com.example.habittracker.notification.ReminderHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HabitDetailActivity extends BaseActivity {

    private long goalId;
    private MaterialToolbar toolbar;
    private TextInputEditText editGoalName;
    private TextInputEditText editMotivation;
    private TimePicker timePicker;
    private Button btnSave;
    private Button btnDelete;
    private TextView textCheckinHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        goalId = getIntent().getLongExtra("goal_id", -1);
        if (goalId == -1) {
            Toast.makeText(this, R.string.habit_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editGoalName = findViewById(R.id.edit_goal_name);
        editMotivation = findViewById(R.id.edit_motivation);
        timePicker = findViewById(R.id.time_picker);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        textCheckinHistory = findViewById(R.id.text_checkin_history);

        timePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> saveGoal());
        btnDelete.setOnClickListener(v -> confirmDelete());

        loadGoal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoal();
    }

    private void loadGoal() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Goal goal = AppDatabase.getInstance(this).goalDao().getById(goalId);
            if (goal == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.habit_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            List<String> checkinDates = AppDatabase.getInstance(this)
                    .checkinDao().getCheckinDatesForGoal(goalId);

            runOnUiThread(() -> {
                editGoalName.setText(goal.getTitle());
                editMotivation.setText(goal.getMotivation());
                timePicker.setHour(goal.getReminderHour());
                timePicker.setMinute(goal.getReminderMinute());
                toolbar.setTitle(goal.getTitle());

                if (checkinDates.isEmpty()) {
                    textCheckinHistory.setText(R.string.no_checkin_records);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String date : checkinDates) {
                        sb.append(date).append("\n");
                    }
                    textCheckinHistory.setText(sb.toString().trim());
                }
            });
        });
    }

    private void saveGoal() {
        String name = editGoalName.getText().toString().trim();
        if (name.isEmpty()) {
            editGoalName.setError(getString(R.string.enter_habit_name));
            return;
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String motivation = editMotivation.getText().toString().trim();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Goal goal = new Goal(name, motivation, hour, minute);
            goal.setId(goalId);
            AppDatabase.getInstance(this).goalDao().update(goal);

            ReminderHelper.cancelReminder(this, goalId);
            ReminderHelper.scheduleReminder(this, goal);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteGoal())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteGoal() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(this).goalDao().delete(goalId);
            AppDatabase.getInstance(this).checkinDao().deleteByGoalId(goalId);
            ReminderHelper.cancelReminder(this, goalId);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.deleted_toast, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
