package com.example.learning_english.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton; // Import CompoundButton
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log; // Giữ lại Log

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.db.Word;

// Không cần import SparseBooleanArray, ArrayList, List nữa

public class WordListAdapter extends ListAdapter<Word, WordListAdapter.WordViewHolder> {

    private final OnWordInteractionListener listener;
    private final Context context;
    private final int defaultItemBackgroundResId;

    // Interface được cập nhật: chỉ cần click và delete, thêm toggleReview
    public interface OnWordInteractionListener {
        void onWordClick(Word word); // Nhấn vào text
        void onWordDeleteClicked(Word word); // Nhấn nút xóa item
        void onWordReviewToggled(Word word, boolean isChecked); // Checkbox được nhấn
        // Không cần onSelectionChanged nữa
    }

    // Constructor không cần SparseBooleanArray nữa
    public WordListAdapter(@NonNull DiffUtil.ItemCallback<Word> diffCallback, OnWordInteractionListener listener, Context context) {
        super(diffCallback);
        this.listener = listener;
        // this.selectedItems = new SparseBooleanArray(); // Xóa dòng này
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
        // Truyền listener vào ViewHolder
        return new WordViewHolder(itemView, listener); // Không cần truyền adapterRef nữa
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word current = getItem(position);
        // Truyền context và background mặc định vào bind
        // Không cần truyền trạng thái chọn tạm thời nữa
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
        // private final WordListAdapter adapterRef; // Xóa tham chiếu Adapter

        // Constructor không cần adapterRef
        private WordViewHolder(View itemView, OnWordInteractionListener listener) {
            super(itemView);
            this.interactionListener = listener;
            // this.adapterRef = adapter; // Xóa dòng này

            itemLayout = itemView.findViewById(R.id.item_word_layout);
            wordItemView = itemView.findViewById(R.id.textview_word_item);
            wordCheckBox = itemView.findViewById(R.id.checkbox_word_item);
            deleteButton = itemView.findViewById(R.id.button_delete_item);

            // Click vào text -> Xem nghĩa (giữ nguyên)
            wordItemView.setOnClickListener(v -> {
                if (interactionListener != null && currentWord != null) {
                    interactionListener.onWordClick(currentWord);
                }
            });

            // Click vào nút xóa -> Gọi listener (giữ nguyên)
            deleteButton.setOnClickListener(v -> {
                if (interactionListener != null && currentWord != null) {
                    interactionListener.onWordDeleteClicked(currentWord);
                }
            });

            // --- THAY ĐỔI LOGIC CHECKBOX ---
            // Sử dụng OnCheckedChangeListener thay vì OnClickListener
            wordCheckBox.setOnCheckedChangeListener(null); // Quan trọng: Gỡ listener cũ trước khi bind
            wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Chỉ gọi listener nếu trạng thái thay đổi thực sự do người dùng nhấn
                // và listener không null, currentWord không null
                if (buttonView.isPressed() && interactionListener != null && currentWord != null) {
                    // Gọi phương thức mới của listener để báo Activity/ViewModel cập nhật DB
                    interactionListener.onWordReviewToggled(currentWord, isChecked);
                    // Không cần gọi adapterRef.toggleSelection nữa
                }
            });
            // ---------------------------------
        }

        // Gán dữ liệu và trạng thái (không cần isTemporarilySelected)
        public void bind(Word word, Context context, int defaultBackgroundResId) {
            currentWord = word;
            wordItemView.setText(word.getEnglishWord());

            // --- CẬP NHẬT CHECKBOX STATE ---
            // Checkbox giờ đây hiển thị trạng thái isForReview thực tế từ DB
            // Gỡ listener tạm thời để tránh trigger khi đang bind dữ liệu
            wordCheckBox.setOnCheckedChangeListener(null);
            wordCheckBox.setChecked(word.isForReview());
            // Gắn lại listener sau khi setChecked
            wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && interactionListener != null && currentWord != null) {
                    interactionListener.onWordReviewToggled(currentWord, isChecked);
                }
            });
            // -----------------------------

            // --- CẬP NHẬT NỀN DỰA TRÊN isForReview (giữ nguyên) ---
            if (word.isForReview()) {
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.review_highlight_background));
            } else {
                if (defaultBackgroundResId != 0) {
                    itemLayout.setBackgroundResource(defaultBackgroundResId);
                } else {
                    itemLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            // ---------------------------------------
        }
    }
    // -----------------------

    // --- XÓA CÁC PHƯƠNG THỨC QUẢN LÝ LỰA CHỌN TẠM THỜI ---
    // public List<Integer> getSelectedWordIds() { ... } // Xóa
    // public void clearSelections() { ... } // Xóa
    // public void toggleSelection(int position) { ... } // Xóa
    // ----------------------------------------------------

    // --- DiffUtil Callback (Giữ nguyên) ---
    public static class WordDiff extends DiffUtil.ItemCallback<Word> {
        @Override public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) { return oldItem.getId() == newItem.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) { return oldItem.equals(newItem); }
    }
    // -----------------------
}
