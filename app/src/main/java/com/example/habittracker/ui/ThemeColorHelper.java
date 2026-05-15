package com.example.habittracker.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.habittracker.R;

public class ThemeColorHelper {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_COLOR = "theme_color_index";
    public static final int INDEX_DEFAULT = 0;

    private static final int[] COLORS = {
        0xFF6750A4, // 0. Purple
        0xFF006493, // 1. Blue
        0xFF006C4C, // 2. Teal
        0xFF386A20, // 3. Green
        0xFF8D5000, // 4. Orange
        0xFFB3261E, // 5. Red
        0xFF7D5260, // 6. Pink
        0xFF00497A, // 7. Navy
        0xFF5F5F5F, // 8. Grey
    };

    private static final int[] THEME_RES = {
        R.style.Theme_HabitTracker_Purple,  // 0
        R.style.Theme_HabitTracker_Blue,    // 1
        R.style.Theme_HabitTracker_Teal,    // 2
        R.style.Theme_HabitTracker_Green,   // 3
        R.style.Theme_HabitTracker_Orange,  // 4
        R.style.Theme_HabitTracker_Red,     // 5
        R.style.Theme_HabitTracker_Pink,    // 6
        R.style.Theme_HabitTracker_Navy,    // 7
        R.style.Theme_HabitTracker_Grey,    // 8
    };

    public static int getColorIndex(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_COLOR, INDEX_DEFAULT);
    }

    public static void setColorIndex(Context context, int index) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_COLOR, index).commit();
    }

    public static int getThemeRes(Context context) {
        int index = getColorIndex(context);
        if (index < 0 || index >= THEME_RES.length) return 0;
        return THEME_RES[index];
    }

    public static int getColorValue(int index) {
        if (index < 0 || index >= COLORS.length) return COLORS[0];
        return COLORS[index];
    }

    public static int[] getAllColors() {
        return COLORS;
    }

    public static boolean isCustomTheme(Context context) {
        return getColorIndex(context) != INDEX_DEFAULT;
    }
}
