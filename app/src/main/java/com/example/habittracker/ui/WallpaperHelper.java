package com.example.habittracker.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WallpaperHelper {

    private static final String WALLPAPER_FILE = "wallpaper.png";
    private static final String PREFS_NAME = "settings";
    private static final String KEY_OPACITY = "wallpaper_opacity";
    private static final int DEFAULT_OPACITY = 180; // ~70%

    public static void saveCroppedBitmap(Context context, Bitmap bitmap) {
        try {
            File file = new File(context.getFilesDir(), WALLPAPER_FILE);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception ignored) {}
    }

    public static void saveWallpaper(Context context, Uri imageUri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            if (input == null) return;

            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            if (bitmap == null) return;

            saveCroppedBitmap(context, bitmap);
        } catch (Exception ignored) {}
    }

    public static boolean hasWallpaper(Context context) {
        return new File(context.getFilesDir(), WALLPAPER_FILE).exists();
    }

    public static Drawable getWallpaperDrawable(Context context) {
        File file = new File(context.getFilesDir(), WALLPAPER_FILE);
        if (!file.exists()) return null;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) return null;

        BitmapDrawable image = new BitmapDrawable(context.getResources(), bitmap);
        image.setAlpha(getOpacity(context));

        // 20% 黑色遮罩，提升文字可读性
        ColorDrawable scrim = new ColorDrawable(0x33000000);
        return new LayerDrawable(new Drawable[]{image, scrim});
    }

    public static void clearWallpaper(Context context) {
        File file = new File(context.getFilesDir(), WALLPAPER_FILE);
        if (file.exists()) file.delete();
    }

    public static int getOpacity(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_OPACITY, DEFAULT_OPACITY);
    }

    public static void setOpacity(Context context, int opacity) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_OPACITY, opacity).commit();
    }
}
