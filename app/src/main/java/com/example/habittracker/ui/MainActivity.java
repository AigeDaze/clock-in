package com.example.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Checkin;
import com.example.habittracker.data.Goal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView habitRecyclerView;
    private GoalAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getInstance(this);

        habitRecyclerView = findViewById(R.id.recycler_habits);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GoalAdapter(
                goal -> performCheckIn(goal),
                goal -> startActivity(new Intent(MainActivity.this, com.example.habittracker.HabitDetailActivity.class)
                        .putExtra("goal_id", goal.getId())));
        habitRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_habit);
        fab.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddGoalActivity.class)));

        loadGoals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
    }

    private void loadGoals() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Goal> goals = db.goalDao().getAllGoalsSync();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<GoalItem> items = new ArrayList<>();

            for (Goal goal : goals) {
                List<String> dates = db.checkinDao().getCheckinDatesForGoal(goal.getId());
                int streak = computeStreak(dates);
                boolean checkedInToday = dates.contains(today);
                items.add(new GoalItem(goal, streak, checkedInToday));
            }

            runOnUiThread(() -> {
                adapter.submitList(items);
                updateEmptyState(items.isEmpty());
            });
        });
    }

    private void performCheckIn(Goal goal) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = db.checkinDao().hasCheckedIn(goal.getId(), today);
            if (count == 0) {
                db.checkinDao().insert(new Checkin(goal.getId(), today));
            }
            runOnUiThread(this::loadGoals);
        });
    }

    private int computeStreak(List<String> dates) {
        if (dates == null || dates.isEmpty()) return 0;

        List<String> sorted = new ArrayList<>(dates);
        Collections.sort(sorted, Collections.reverseOrder());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar expected = Calendar.getInstance();

        String todayStr = sdf.format(expected.getTime());
        if (!sorted.get(0).equals(todayStr)) {
            expected.add(Calendar.DAY_OF_YEAR, -1);
        }

        int streak = 0;
        for (String dateStr : sorted) {
            String expectedStr = sdf.format(expected.getTime());
            if (dateStr.equals(expectedStr)) {
                streak++;
                expected.add(Calendar.DAY_OF_YEAR, -1);
            } else if (streak > 0) {
                break;
            }
        }

        return streak;
    }

    private void updateEmptyState(boolean empty) {
        findViewById(R.id.text_empty).setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        habitRecyclerView.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
    }
}
