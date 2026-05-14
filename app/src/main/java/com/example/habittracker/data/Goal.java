package com.example.habittracker.data;

public class Goal {
    private long id;
    private String title;
    private int reminderHour;
    private int reminderMinute;

    public Goal(String title, int reminderHour, int reminderMinute) {
        this.title = title;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getReminderHour() { return reminderHour; }
    public void setReminderHour(int h) { this.reminderHour = h; }
    public int getReminderMinute() { return reminderMinute; }
    public void setReminderMinute(int m) { this.reminderMinute = m; }
}
