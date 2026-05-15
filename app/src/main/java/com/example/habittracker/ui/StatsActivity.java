package com.example.habittracker.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.example.habittracker.R;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Goal;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsActivity extends BaseActivity {

    private static final long GOAL_ID_ALL = -1;

    private AppDatabase db;
    private HeatmapView heatmapView;
    private BarChart barChart;
    private Spinner goalSpinner;

    private List<Goal> goals = new ArrayList<>();
    private long selectedGoalId = GOAL_ID_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = AppDatabase.getInstance(this);

        heatmapView = findViewById(R.id.heatmap_view);
        barChart = findViewById(R.id.bar_chart);
        goalSpinner = findViewById(R.id.spinner_goal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        goalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedGoalId = GOAL_ID_ALL;
                } else if (position - 1 < goals.size()) {
                    selectedGoalId = goals.get(position - 1).getId();
                }
                loadStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadGoals();
    }

    private void loadGoals() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            goals = db.goalDao().getAllGoalsSync();
            List<String> goalNames = new ArrayList<>();
            goalNames.add(getString(R.string.all_goals));
            for (Goal g : goals) {
                goalNames.add(g.getTitle());
            }
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, goalNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                goalSpinner.setAdapter(adapter);
                loadStats();
            });
        });
    }

    private void loadStats() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Map<String, Integer> dateCounts = new HashMap<>();

            if (selectedGoalId == GOAL_ID_ALL) {
                for (Goal g : goals) {
                    for (String date : db.checkinDao().getCheckinDatesForGoal(g.getId())) {
                        dateCounts.put(date, dateCounts.getOrDefault(date, 0) + 1);
                    }
                }
            } else {
                for (String date : db.checkinDao().getCheckinDatesForGoal(selectedGoalId)) {
                    dateCounts.put(date, 1);
                }
            }

            int maxCount = 0;
            for (int c : dateCounts.values()) {
                if (c > maxCount) maxCount = c;
            }

            int totalGoals = selectedGoalId == GOAL_ID_ALL ? Math.max(1, goals.size()) : 1;
            MonthlyData monthlyData = computeMonthlyData(dateCounts, totalGoals);

            Map<String, Integer> finalDateCounts = dateCounts;
            int finalMaxCount = maxCount;
            runOnUiThread(() -> {
                heatmapView.setData(finalDateCounts, finalMaxCount);
                updateBarChart(monthlyData);
            });
        });
    }

    private MonthlyData computeMonthlyData(Map<String, Integer> dateCounts, int totalGoals) {
        List<String> monthLabels = new ArrayList<>();
        List<Float> rates = new ArrayList<>();

        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            String monthKey = monthFmt.format(cal.getTime());
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            int checkinCount = 0;
            for (Map.Entry<String, Integer> entry : dateCounts.entrySet()) {
                if (entry.getKey().startsWith(monthKey)) {
                    checkinCount += entry.getValue();
                }
            }

            int totalPossible = totalGoals * daysInMonth;
            float rate = totalPossible > 0 ? (float) checkinCount / totalPossible * 100 : 0;

            cal.set(Calendar.DAY_OF_MONTH, 1);
            String label = getResources().getStringArray(R.array.month_labels)[cal.get(Calendar.MONTH)];
            monthLabels.add(label);
            rates.add(rate);
        }

        return new MonthlyData(monthLabels, rates);
    }

    private void updateBarChart(MonthlyData data) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.rates.size(); i++) {
            entries.add(new BarEntry(i, data.rates.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.completion_rate_label));

        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, tv, true);
        dataSet.setColor(tv.data);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(data.labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(20f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(500);
        barChart.invalidate();
    }

    private static class MonthlyData {
        final List<String> labels;
        final List<Float> rates;

        MonthlyData(List<String> labels, List<Float> rates) {
            this.labels = labels;
            this.rates = rates;
        }
    }
}
