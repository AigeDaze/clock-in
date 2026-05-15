package com.example.habittracker.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.habittracker.R;

public class CropWallpaperActivity extends BaseActivity {

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_wallpaper);

        cropImageView = findViewById(R.id.crop_image_view);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnConfirm = findViewById(R.id.btn_confirm);

        Uri imageUri = getIntent().getParcelableExtra("image_uri");
        if (imageUri == null) {
            finish();
            return;
        }
        cropImageView.setImageURI(imageUri);

        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> doCrop());
    }

    private void doCrop() {
        RectF cropRect = cropImageView.getCropRect();
        int outW = getResources().getDisplayMetrics().widthPixels;
        int outH = getResources().getDisplayMetrics().heightPixels;

        Bitmap cropped = cropImageView.cropToRect(cropRect, outW, outH);
        if (cropped == null) {
            Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        WallpaperHelper.saveCroppedBitmap(this, cropped);

        Intent result = new Intent();
        result.putExtra("wallpaper_saved", true);
        setResult(RESULT_OK, result);
        finish();
    }
}
