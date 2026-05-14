package com.example.habittracker.data;

public class Checkin {
    private long id;
    private long goalId;
    private String date;

    public Checkin(long goalId, String date) {
        this.goalId = goalId;
        this.date = date;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getGoalId() { return goalId; }
    public String getDate() { return date; }
}
