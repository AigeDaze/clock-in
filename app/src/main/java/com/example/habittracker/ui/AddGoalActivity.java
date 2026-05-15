package com.example.habittracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Goal;
import com.example.habittracker.notification.ReminderHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddGoalActivity extends BaseActivity {

    private TextInputEditText editGoalName;
    private TextInputEditText editMotivation;
    private TimePicker timePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        editGoalName = findViewById(R.id.edit_goal_name);
        editMotivation = findViewById(R.id.edit_motivation);
        timePicker = findViewById(R.id.time_picker);
        btnSave = findViewById(R.id.btn_save);

        timePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> saveGoal());
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

        Goal goal = new Goal(name, motivation, hour, minute);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = AppDatabase.getInstance(this).goalDao().insert(goal);
            goal.setId(id);
            ReminderHelper.scheduleReminder(this, goal);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.habit_added_toast, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
