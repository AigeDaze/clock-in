package com.example.habittracker.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class CropImageView extends AppCompatImageView {

    private final Matrix baseMatrix = new Matrix();
    private final Matrix drawMatrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private ScaleGestureDetector scaleDetector;
    private float lastX, lastY;
    private boolean isDragging;

    private final Paint overlayPaint = new Paint();
    private final RectF cropRect = new RectF();

    private float minScale = 1f;
    private float maxScale = 3f;

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        overlayPaint.setStyle(Paint.Style.STROKE);
        overlayPaint.setColor(0xFFFFFFFF);
        overlayPaint.setStrokeWidth(3f);
        overlayPaint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && getDrawable() != null) {
            resetMatrix();
            updateCropRect();
        }
    }

    private void resetMatrix() {
        baseMatrix.reset();
        drawMatrix.reset();

        float viewW = getWidth();
        float viewH = getHeight();
        float imgW = getDrawable().getIntrinsicWidth();
        float imgH = getDrawable().getIntrinsicHeight();

        float scale = Math.min(viewW / imgW, viewH / imgH);
        minScale = Math.min(scale, 0.5f);
        maxScale = scale * 3f;

        baseMatrix.setScale(scale, scale);
        baseMatrix.postTranslate((viewW - imgW * scale) / 2f, (viewH - imgH * scale) / 2f);
        setImageMatrix(baseMatrix);
    }

    private void updateCropRect() {
        float w = getWidth() * 0.85f;
        float h = getHeight() * 0.7f;
        float left = (getWidth() - w) / 2f;
        float top = (getHeight() - h) / 2f;
        cropRect.set(left, top, left + w, top + h);
    }

    public RectF getCropRect() {
        return new RectF(cropRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Darken outside crop area
        canvas.save();
        canvas.clipRect(cropRect, android.graphics.Region.Op.DIFFERENCE);
        canvas.drawColor(0xAA000000);
        canvas.restore();

        // Draw crop border
        canvas.drawRect(cropRect, overlayPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                isDragging = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging && !scaleDetector.isInProgress()) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    applyTranslation(dx, dy);
                    lastX = event.getX();
                    lastY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return true;
    }

    private void applyTranslation(float dx, float dy) {
        drawMatrix.set(baseMatrix);
        drawMatrix.postTranslate(dx, dy);
        baseMatrix.set(drawMatrix);
        setImageMatrix(baseMatrix);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            float newScale = getCurrentScale() * factor;
            if (newScale < minScale || newScale > maxScale) return true;

            baseMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(baseMatrix);
            return true;
        }
    }

    private float getCurrentScale() {
        baseMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    public Bitmap cropToRect(RectF rect, int outW, int outH) {
        baseMatrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];

        // Map crop rect back to image coordinates
        float imgLeft = (rect.left - transX) / scale;
        float imgTop = (rect.top - transY) / scale;
        float imgRight = (rect.right - transX) / scale;
        float imgBottom = (rect.bottom - transY) / scale;

        int srcW = getDrawable().getIntrinsicWidth();
        int srcH = getDrawable().getIntrinsicHeight();
        imgLeft = Math.max(0, imgLeft);
        imgTop = Math.max(0, imgTop);
        imgRight = Math.min(srcW, imgRight);
        imgBottom = Math.min(srcH, imgBottom);

        int cropW = (int) (imgRight - imgLeft);
        int cropH = (int) (imgBottom - imgTop);
        if (cropW <= 0 || cropH <= 0) return null;

        Bitmap src = null;
        if (getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
            src = ((android.graphics.drawable.BitmapDrawable) getDrawable()).getBitmap();
        }
        if (src == null) return null;

        Bitmap cropped = Bitmap.createBitmap(src, (int) imgLeft, (int) imgTop, cropW, cropH);
        return Bitmap.createScaledBitmap(cropped, outW, outH, true);
    }
}
