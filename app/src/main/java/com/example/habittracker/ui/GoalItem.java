package com.example.habittracker.ui;

import com.example.habittracker.data.Goal;

public class GoalItem {
    public final Goal goal;
    public final int streak;
    public final boolean checkedInToday;

    public GoalItem(Goal goal, int streak, boolean checkedInToday) {
        this.goal = goal;
        this.streak = streak;
        this.checkedInToday = checkedInToday;
    }
}
