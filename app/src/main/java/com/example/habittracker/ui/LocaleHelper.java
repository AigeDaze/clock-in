package com.example.habittracker.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import com.example.habittracker.R;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_LANGUAGE = "language";
    public static final String LANG_ZH = "zh";
    public static final String LANG_EN = "en";

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        Locale locale = LANG_ZH.equals(lang) ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, LANG_ZH);
    }

    public static void setLanguage(Context context, String lang) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, lang).commit();
    }

    public static String getLanguageDisplayName(Context context) {
        String lang = getLanguage(context);
        if (LANG_ZH.equals(lang)) return context.getString(R.string.chinese);
        return context.getString(R.string.english);
    }
}
