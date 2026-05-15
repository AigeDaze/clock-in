package com.example.habittracker.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.R;
import com.google.android.material.color.DynamicColors;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LocaleHelper.onAttach(newBase);
        context = applyFontScale(context);
        super.attachBaseContext(context);
    }

    private Context applyFontScale(Context context) {
        float scale = FontHelper.getFontScale(context);
        if (scale == 1.0f) return context;
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.fontScale = scale;
        return context.createConfigurationContext(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeRes = ThemeColorHelper.getThemeRes(this);
        if (themeRes != 0) {
            setTheme(themeRes);
        }
        if (FontHelper.isBold(this)) {
            getTheme().applyStyle(R.style.TextAppearanceOverlay_Bold, true);
        }
        super.onCreate(savedInstanceState);
        if (!ThemeColorHelper.isCustomTheme(this)) {
            DynamicColors.applyToActivityIfAvailable(this);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyWallpaper();
    }

    private void applyWallpaper() {
        if (WallpaperHelper.hasWallpaper(this)) {
            Drawable wallpaper = WallpaperHelper.getWallpaperDrawable(this);
            if (wallpaper != null) {
                getWindow().setBackgroundDrawable(wallpaper);
            }
        }
    }
}
