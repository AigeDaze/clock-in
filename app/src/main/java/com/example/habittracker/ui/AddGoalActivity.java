package com.example.habittracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Goal;
import com.example.habittracker.notification.ReminderHelper;
import com.google.android.material.textfield.TextInputEditText;

public class AddGoalActivity extends AppCompatActivity {

    private TextInputEditText editGoalName;
    private TimePicker timePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        editGoalName = findViewById(R.id.edit_goal_name);
        timePicker = findViewById(R.id.time_picker);
        btnSave = findViewById(R.id.btn_save);

        timePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> saveGoal());
    }

    private void saveGoal() {
        String name = editGoalName.getText().toString().trim();
        if (name.isEmpty()) {
            editGoalName.setError("请输入习惯名称");
            return;
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        Goal goal = new Goal(name, hour, minute);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = AppDatabase.getInstance(this).goalDao().insert(goal);
            goal.setId(id);
            ReminderHelper.scheduleReminder(this, goal);

            runOnUiThread(() -> {
                Toast.makeText(this, "习惯已添加", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
