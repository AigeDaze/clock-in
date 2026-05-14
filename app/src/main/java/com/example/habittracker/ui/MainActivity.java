package com.example.habittracker.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import android.view.Menu;
import android.view.MenuItem;

import com.example.habittracker.data.Checkin;
import com.example.habittracker.data.Goal;
import com.google.android.material.button.MaterialButton;
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

    private View contentWrapper;
    private TextView textTodayTitle;
    private HorizontalScrollView pendingScroll;
    private LinearLayout pendingContainer;
    private TextView textAllDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getInstance(this);

        contentWrapper = findViewById(R.id.content_wrapper);
        textTodayTitle = findViewById(R.id.text_today_title);
        pendingScroll = findViewById(R.id.pending_scroll);
        pendingContainer = findViewById(R.id.pending_container);
        textAllDone = findViewById(R.id.text_all_done);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }

    private void loadGoals() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Goal> goals = db.goalDao().getAllGoalsSync();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            List<Goal> pendingGoals = new ArrayList<>();
            List<GoalItem> items = new ArrayList<>();

            for (Goal goal : goals) {
                List<String> dates = db.checkinDao().getCheckinDatesForGoal(goal.getId());
                int streak = computeStreak(dates);
                boolean checkedInToday = dates.contains(today);
                items.add(new GoalItem(goal, streak, checkedInToday));
                if (!checkedInToday) {
                    pendingGoals.add(goal);
                }
            }

            runOnUiThread(() -> {
                adapter.submitList(items);
                updatePendingSection(pendingGoals);
                updateEmptyState(items.isEmpty());
            });
        });
    }

    private void updatePendingSection(List<Goal> pendingGoals) {
        pendingContainer.removeAllViews();

        if (pendingGoals.isEmpty()) {
            textTodayTitle.setVisibility(View.GONE);
            pendingScroll.setVisibility(View.GONE);
            textAllDone.setVisibility(View.VISIBLE);
            return;
        }

        textAllDone.setVisibility(View.GONE);
        textTodayTitle.setVisibility(View.VISIBLE);
        pendingScroll.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Goal goal : pendingGoals) {
            View cardView = inflater.inflate(R.layout.item_pending_checkin, pendingContainer, false);

            TextView nameView = cardView.findViewById(R.id.card_goal_name);
            nameView.setText(goal.getTitle());

            MaterialButton checkinBtn = cardView.findViewById(R.id.card_btn_checkin);
            checkinBtn.setOnClickListener(v -> performCheckIn(goal));

            pendingContainer.addView(cardView);
        }
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
        contentWrapper.setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.text_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
