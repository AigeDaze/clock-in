package com.example.habittracker.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.habittracker.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HeatmapView extends View {

    private Map<String, Integer> dateCounts = new HashMap<>();
    private int maxCount = 0;
    private int numCols = 0;
    private int numRows = 7;
    private final List<CellInfo> cells = new ArrayList<>();

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float density;
    private int cellSize;
    private int cellGap;
    private int labelPad;

    private static final int[] LEVEL_COLORS = {
        0xFFC6E48B, // level 1 (1~25%)
        0xFFC6E48B, // level 1 (1~25%) — same as index 0 for getLevelColor fallback
        0xFF7BC96F, // level 2 (25~50%)
        0xFF239A3B, // level 3 (50~75%)
        0xFF196127  // level 4 (75~100%)
    };

    private final String[] dayLabels;
    private final String[] monthLabels;

    private int emptyColor;
    private int labelColor;
    private boolean isDark;

    public HeatmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        cellSize = (int) (15 * density);
        cellGap = (int) (3 * density);
        labelPad = (int) (6 * density);

        dayLabels = context.getResources().getStringArray(R.array.day_labels);
        monthLabels = context.getResources().getStringArray(R.array.month_labels);

        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        isDark = nightMode == Configuration.UI_MODE_NIGHT_YES;
        emptyColor = isDark ? 0xFF21262D : 0xFFEBEDF0;

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, tv, true);
        labelColor = tv.data;

        labelPaint.setColor(labelColor);
        labelPaint.setTextSize(9 * density);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setAntiAlias(true);
    }

    public void setData(Map<String, Integer> dateCounts, int maxCount) {
        this.dateCounts = dateCounts != null ? dateCounts : new HashMap<>();
        this.maxCount = Math.max(1, maxCount);
        computeGrid();
        requestLayout();
        invalidate();
    }

    private void computeGrid() {
        cells.clear();
        numCols = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        List<String> dateList = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            dateList.add(sdf.format(cal.getTime()));
        }

        if (dateList.isEmpty()) return;

        try {
            Date firstDate = sdf.parse(dateList.get(0));
            cal.setTime(firstDate);
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dow + 5) % 7;
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            long firstMondayMs = cal.getTimeInMillis();

            long dayMs = 24L * 3600 * 1000;

            for (String dateStr : dateList) {
                Date date = sdf.parse(dateStr);
                cal.setTime(date);
                long dateMs = cal.getTimeInMillis();

                int row = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                int col = (int) ((dateMs - firstMondayMs) / dayMs / 7);

                int count = dateCounts.containsKey(dateStr) ? dateCounts.get(dateStr) : 0;
                cells.add(new CellInfo(row, col, count, dateStr));
                if (col + 1 > numCols) numCols = col + 1;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = (int) (numCols * (cellSize + cellGap) + labelPad * 4 + getPaddingLeft() + getPaddingRight());
        int h = (int) (numRows * (cellSize + cellGap) + labelPad * 2 + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(resolveSize(Math.max(w, 200), widthMeasureSpec),
                            resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padLeft = getPaddingLeft();
        int padTop = getPaddingTop();
        float labelW = labelPad * 3;

        // Day-of-week labels (every other row)
        for (int i = 0; i < numRows; i++) {
            if (i % 2 == 0) {
                float y = padTop + labelPad + i * (cellSize + cellGap) + cellSize / 2f;
                float textY = y - (labelPaint.descent() + labelPaint.ascent()) / 2;
                canvas.drawText(dayLabels[i], padLeft + 2 * density, textY, labelPaint);
            }
        }

        // Month labels at top
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setColor(labelColor);
        int lastMonth = -1;
        for (CellInfo cell : cells) {
            if (cell.col == 0) continue;
            // Check if this is the first cell of a new month
            try {
                String month = cell.date.substring(5, 7);
                int m = Integer.parseInt(month);
                if (m != lastMonth) {
                    lastMonth = m;
                    float x = padLeft + labelW + cell.col * (cellSize + cellGap);
                    float textY = padTop + labelPad / 2f - (labelPaint.descent() + labelPaint.ascent()) / 2;
                    canvas.drawText(monthLabels[m - 1], x, textY, labelPaint);
                }
            } catch (Exception ignored) {}
        }

        // Draw cells
        for (CellInfo cell : cells) {
            float left = padLeft + labelW + cell.col * (cellSize + cellGap);
            float top = padTop + labelPad + cell.row * (cellSize + cellGap);
            RectF rect = new RectF(left, top, left + cellSize, top + cellSize);

            cellPaint.setColor(getLevelColor(cell.count));
            canvas.drawRoundRect(rect, 2 * density, 2 * density, cellPaint);
        }
    }

    private int getLevelColor(int count) {
        if (count == 0) return emptyColor;
        if (maxCount <= 1) return LEVEL_COLORS[4];
        float ratio = (float) count / maxCount;
        if (ratio <= 0.25f) return LEVEL_COLORS[1];
        if (ratio <= 0.50f) return LEVEL_COLORS[2];
        if (ratio <= 0.75f) return LEVEL_COLORS[3];
        return LEVEL_COLORS[4];
    }

    private static class CellInfo {
        final int row, col, count;
        final String date;
        CellInfo(int row, int col, int count, String date) {
            this.row = row;
            this.col = col;
            this.count = count;
            this.date = date;
        }
    }
}
