package com.example.learning_english.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R; // Import R

import java.util.List;

/**
 * Adapter đơn giản để hiển thị danh sách các chuỗi String.
 * Sử dụng layout item_grammar_topic.xml.
 */
public class SimpleStringListAdapter extends RecyclerView.Adapter<SimpleStringListAdapter.StringViewHolder> {

    private final List<String> itemList;
    private final OnItemClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(String itemText);
    }

    public SimpleStringListAdapter(List<String> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grammar_topic, parent, false);

        // Tìm ImageView bằng ID mới và chuẩn
        View icon = view.findViewById(R.id.image_topic_icon);
        if (icon != null) {
            icon.setVisibility(View.GONE); // Ẩn icon đi
        }

        // Điều chỉnh lề cho TextView nếu icon bị ẩn
        TextView titleView = view.findViewById(R.id.text_grammar_topic_title);
        if (titleView != null && icon != null && icon.getVisibility() == View.GONE) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
            // Lấy giá trị paddingStart của LinearLayout gốc để làm margin thay vì 0
            int startPadding = view.getPaddingStart(); // Lấy paddingStart của view gốc (LinearLayout)
            params.leftMargin = startPadding; // Đặt margin trái bằng paddingStart gốc
            // Hoặc bạn có thể set margin là 0 nếu muốn text sát lề trái hoàn toàn
            // params.leftMargin = 0;
            titleView.setLayoutParams(params);
        }

        return new StringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringViewHolder holder, int position) {
        String itemText = itemList.get(position);
        holder.bind(itemText, listener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder cho một chuỗi String
    static class StringViewHolder extends RecyclerView.ViewHolder {
        TextView itemTextView;

        StringViewHolder(@NonNull View itemView) {
            super(itemView);
            // Lấy ID của TextView từ layout item_grammar_topic.xml
            itemTextView = itemView.findViewById(R.id.text_grammar_topic_title);
        }

        void bind(final String itemText, final OnItemClickListener listener) {
            if (itemTextView != null) {
                itemTextView.setText(itemText);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(itemText);
                }
            });
        }
    }
}
