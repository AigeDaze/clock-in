package com.example.habittracker.ui;

import android.content.Context;
import android.content.SharedPreferences;

public class FontHelper {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_SCALE = "font_scale";
    private static final String KEY_BOLD = "font_bold";

    public static final float[] SCALES = {0.85f, 1.0f, 1.15f, 1.3f};

    public static float getFontScale(Context context) {
        int index = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_SCALE, 1); // default: index 1 = 1.0x
        if (index < 0 || index >= SCALES.length) return 1.0f;
        return SCALES[index];
    }

    public static int getFontScaleIndex(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_SCALE, 1);
    }

    public static void setFontScaleIndex(Context context, int index) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_SCALE, index).commit();
    }

    public static boolean isBold(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_BOLD, false);
    }

    public static void setBold(Context context, boolean bold) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_BOLD, bold).commit();
    }
}
