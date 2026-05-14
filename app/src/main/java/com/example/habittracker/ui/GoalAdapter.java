package com.example.habittracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.Goal;

import java.util.Locale;

public class GoalAdapter extends ListAdapter<GoalItem, GoalAdapter.ViewHolder> {

    private final OnCheckInListener checkInListener;
    private final OnGoalClickListener goalClickListener;

    public interface OnCheckInListener {
        void onCheckInClick(Goal goal);
    }

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    public GoalAdapter(OnCheckInListener checkInListener, OnGoalClickListener goalClickListener) {
        super(DIFF_CALLBACK);
        this.checkInListener = checkInListener;
        this.goalClickListener = goalClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoalItem item = getItem(position);
        holder.bind(item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView streakText;
        private final Button checkInButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_goal_title);
            streakText = itemView.findViewById(R.id.text_streak);
            checkInButton = itemView.findViewById(R.id.btn_checkin);
        }

        void bind(GoalItem item) {
            titleText.setText(item.goal.getTitle());
            streakText.setText(String.format(Locale.getDefault(), "连续 %d 天", item.streak));

            itemView.setOnClickListener(v -> goalClickListener.onGoalClick(item.goal));

            if (item.checkedInToday) {
                checkInButton.setText(R.string.completed);
                checkInButton.setEnabled(false);
            } else {
                checkInButton.setText(R.string.check_in);
                checkInButton.setEnabled(true);
                checkInButton.setOnClickListener(v -> checkInListener.onCheckInClick(item.goal));
            }
        }
    }

    private static final DiffUtil.ItemCallback<GoalItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<GoalItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull GoalItem oldItem, @NonNull GoalItem newItem) {
            return oldItem.goal.getId() == newItem.goal.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull GoalItem oldItem, @NonNull GoalItem newItem) {
            return oldItem.streak == newItem.streak
                    && oldItem.checkedInToday == newItem.checkedInToday
                    && oldItem.goal.getTitle().equals(newItem.goal.getTitle());
        }
    };
}
