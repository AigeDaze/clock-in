package com.example.habittracker.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.habittracker.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends BaseActivity {

    private TextView optionChinese;
    private TextView optionEnglish;
    private LinearLayout colorRow1;
    private LinearLayout colorRow2;
    private TextView optionWallpaperPick;
    private TextView optionWallpaperReset;
    private SeekBar opacitySeekBar;
    private LinearLayout fontSizeContainer;
    private SwitchMaterial switchBold;

    private final ActivityResultLauncher<String> pickWallpaper = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::onWallpaperPicked);

    private final ActivityResultLauncher<Intent> cropWallpaper = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                    restartApp();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        optionChinese = findViewById(R.id.option_chinese);
        optionEnglish = findViewById(R.id.option_english);
        colorRow1 = findViewById(R.id.color_row_1);
        colorRow2 = findViewById(R.id.color_row_2);
        optionWallpaperPick = findViewById(R.id.option_wallpaper_pick);
        optionWallpaperReset = findViewById(R.id.option_wallpaper_reset);
        opacitySeekBar = findViewById(R.id.seekbar_opacity);
        fontSizeContainer = findViewById(R.id.font_size_container);
        switchBold = findViewById(R.id.switch_bold);

        optionChinese.setOnClickListener(v -> switchLanguage(LocaleHelper.LANG_ZH));
        optionEnglish.setOnClickListener(v -> switchLanguage(LocaleHelper.LANG_EN));
        optionWallpaperPick.setOnClickListener(v -> pickWallpaper.launch("image/*"));
        optionWallpaperReset.setOnClickListener(v -> resetWallpaper());

        opacitySeekBar.setMax(255);
        opacitySeekBar.setProgress(WallpaperHelper.getOpacity(this));
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                WallpaperHelper.setOpacity(SettingsActivity.this, seekBar.getProgress());
            }
        });

        switchBold.setChecked(FontHelper.isBold(this));
        switchBold.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FontHelper.setBold(SettingsActivity.this, isChecked);
        });

        buildColorPalette();
        buildFontSizeSelector();
    }

    private void onWallpaperPicked(Uri uri) {
        if (uri == null) return;
        Intent intent = new Intent(this, CropWallpaperActivity.class);
        intent.putExtra("image_uri", uri);
        cropWallpaper.launch(intent);
    }

    private void resetWallpaper() {
        WallpaperHelper.clearWallpaper(this);
        Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
        restartApp();
    }

    private void buildColorPalette() {
        colorRow1.removeAllViews();
        colorRow2.removeAllViews();

        int[] colors = ThemeColorHelper.getAllColors();
        int currentIndex = ThemeColorHelper.getColorIndex(this);
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (8 * density);

        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            View dot = new View(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams(size, size));

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(colors[i]);
            if (i == currentIndex) {
                shape.setStroke((int) (3 * density), 0xFF000000);
            }
            dot.setBackground(shape);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.setMargins(margin, 0, margin, 0);
            params.gravity = Gravity.CENTER;
            dot.setLayoutParams(params);

            dot.setClickable(true);
            dot.setFocusable(true);
            dot.setOnClickListener(v -> switchColor(index));

            if (i < 5) {
                colorRow1.addView(dot);
            } else {
                colorRow2.addView(dot);
            }
        }
    }

    private void switchColor(int index) {
        int current = ThemeColorHelper.getColorIndex(this);
        if (index == current) return;

        ThemeColorHelper.setColorIndex(this, index);
        restartApp();
    }

    private void switchLanguage(String lang) {
        String current = LocaleHelper.getLanguage(this);
        if (lang.equals(current)) return;

        LocaleHelper.setLanguage(this, lang);
        restartApp();
    }

    private void buildFontSizeSelector() {
        fontSizeContainer.removeAllViews();
        int currentIndex = FontHelper.getFontScaleIndex(this);
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < FontHelper.SCALES.length; i++) {
            final int index = i;
            RadioButton rb = new RadioButton(this);
            String[] labels = getResources().getStringArray(R.array.font_scale_labels);
            rb.setText(labels[i]);
            rb.setTextSize(12);
            rb.setChecked(i == currentIndex);
            rb.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            rb.setOnClickListener(v -> {
                FontHelper.setFontScaleIndex(SettingsActivity.this, index);
                updateRadioStates();
            });
            fontSizeContainer.addView(rb);
        }
    }

    private void updateRadioStates() {
        int currentIndex = FontHelper.getFontScaleIndex(this);
        for (int i = 0; i < fontSizeContainer.getChildCount(); i++) {
            ((RadioButton) fontSizeContainer.getChildAt(i)).setChecked(i == currentIndex);
        }
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
