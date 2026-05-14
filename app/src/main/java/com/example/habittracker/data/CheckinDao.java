package com.example.habittracker.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CheckinDao {
    private final Map<Long, List<String>> data = new LinkedHashMap<>();

    public long insert(Checkin checkin) {
        data.computeIfAbsent(checkin.getGoalId(), k -> new ArrayList<>()).add(checkin.getDate());
        return 1;
    }

    public int hasCheckedIn(long goalId, String date) {
        List<String> dates = data.get(goalId);
        return (dates != null && dates.contains(date)) ? 1 : 0;
    }

    public List<String> getCheckinDatesForGoal(long goalId) {
        return data.getOrDefault(goalId, new ArrayList<>());
    }

    public void deleteByGoalId(long goalId) {
        data.remove(goalId);
    }
}
