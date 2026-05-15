package com.example.habittracker.data;

public class Goal {
    private long id;
    private String title;
    private String motivation;
    private int reminderHour;
    private int reminderMinute;
    private transient boolean checkedInToday;

    public Goal(String title, String motivation, int reminderHour, int reminderMinute) {
        this.title = title;
        this.motivation = motivation != null ? motivation : "";
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
    }

    public Goal(String title, int reminderHour, int reminderMinute) {
        this(title, "", reminderHour, reminderMinute);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public int getReminderHour() { return reminderHour; }
    public void setReminderHour(int h) { this.reminderHour = h; }
    public int getReminderMinute() { return reminderMinute; }
    public void setReminderMinute(int m) { this.reminderMinute = m; }
    public boolean isCheckedInToday() { return checkedInToday; }
    public void setCheckedInToday(boolean v) { this.checkedInToday = v; }
}
