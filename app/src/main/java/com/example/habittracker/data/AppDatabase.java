package com.example.habittracker.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppDatabase {
    private static AppDatabase INSTANCE;
    private final List<Goal> goals = new ArrayList<>();
    private final GoalDao goalDao = new GoalDao(goals);
    private final CheckinDao checkinDao = new CheckinDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public static AppDatabase getInstance(Object context) {
        if (INSTANCE == null) INSTANCE = new AppDatabase();
        return INSTANCE;
    }

    public GoalDao goalDao() { return goalDao; }
    public CheckinDao checkinDao() { return checkinDao; }
}
