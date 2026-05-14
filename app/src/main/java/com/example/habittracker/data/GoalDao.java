package com.example.habittracker.data;

import java.util.ArrayList;
import java.util.List;

public class GoalDao {
    private final List<Goal> goals;

    public GoalDao(List<Goal> goals) { this.goals = goals; }

    public long insert(Goal goal) {
        goal.setId(goals.size() + 1);
        goals.add(goal);
        return goal.getId();
    }

    public List<Goal> getAllGoalsSync() { return new ArrayList<>(goals); }

    public Goal getById(long id) {
        for (Goal g : goals) {
            if (g.getId() == id) return g;
        }
        return null;
    }

    public void update(Goal goal) {
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getId() == goal.getId()) {
                goals.set(i, goal);
                return;
            }
        }
    }

    public void delete(long id) {
        goals.removeIf(g -> g.getId() == id);
    }
}
