package com.example.learning_english.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.db.Word;

// Adapter này không thay đổi nhiều về cấu trúc cốt lõi,
// vì Activity sẽ chịu trách nhiệm cung cấp danh sách từ đúng cho package đã chọn.
public class WordListAdapter extends ListAdapter<Word, WordListAdapter.WordViewHolder> {

    private final OnWordInteractionListener listener;
    private final Context context;
    private final int defaultItemBackgroundResId;

    // Interface không đổi
    public interface OnWordInteractionListener {
        void onWordClick(Word word);
        void onWordDeleteClicked(Word word);
        void onWordReviewToggled(Word word, boolean isChecked);
    }

    public WordListAdapter(@NonNull DiffUtil.ItemCallback<Word> diffCallback, OnWordInteractionListener listener, Context context) {
        super(diffCallback);
        this.listener = listener;
        this.context = context;

        // Lấy background mặc định từ theme
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        this.defaultItemBackgroundResId = typedArray.getResourceId(0, 0);
        typedArray.recycle();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word current = getItem(position);
        holder.bind(current, context, defaultItemBackgroundResId);
    }

    // --- ViewHolder Class ---
    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout itemLayout;
        private final TextView wordItemView;
        private final CheckBox wordCheckBox;
        private final ImageButton deleteButton;
        private Word currentWord;
        private final OnWordInteractionListener interactionListener;

        private WordViewHolder(View itemView, OnWordInteractionListener listener) {
            super(itemView);
            this.interactionListener = listener;

            itemLayout = itemView.findViewById(R.id.item_word_layout);
            wordItemView = itemView.findViewById(R.id.textview_word_item);
            wordCheckBox = itemView.findViewById(R.id.checkbox_word_item);
            deleteButton = itemView.findViewById(R.id.button_delete_item);

            // Click vào text -> Xem nghĩa
            wordItemView.setOnClickListener(v -> {
                if (interactionListener != null && currentWord != null) {
                    interactionListener.onWordClick(currentWord);
                }
            });

            // Click vào nút xóa -> Gọi listener
            deleteButton.setOnClickListener(v -> {
                if (interactionListener != null && currentWord != null) {
                    interactionListener.onWordDeleteClicked(currentWord);
                }
            });

            // Xử lý checkbox
            wordCheckBox.setOnCheckedChangeListener(null); // Gỡ listener cũ
            wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && interactionListener != null && currentWord != null) {
                    interactionListener.onWordReviewToggled(currentWord, isChecked);
                }
            });
        }

        // Gán dữ liệu và trạng thái
        public void bind(Word word, Context context, int defaultBackgroundResId) {
            currentWord = word;
            wordItemView.setText(word.getEnglishWord());

            // Cập nhật trạng thái checkbox
            wordCheckBox.setOnCheckedChangeListener(null);
            wordCheckBox.setChecked(word.isForReview());
            wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && interactionListener != null && currentWord != null) {
                    interactionListener.onWordReviewToggled(currentWord, isChecked);
                }
            });

            // Cập nhật nền dựa trên isForReview
            if (word.isForReview()) {
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.review_highlight_background));
            } else {
                if (defaultBackgroundResId != 0) {
                    itemLayout.setBackgroundResource(defaultBackgroundResId);
                } else {
                    itemLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }
    // -----------------------

    // --- DiffUtil Callback (Không đổi) ---
    public static class WordDiff extends DiffUtil.ItemCallback<Word> {
        @Override public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) { return oldItem.getId() == newItem.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) { return oldItem.equals(newItem); }
    }
    // -----------------------
}
