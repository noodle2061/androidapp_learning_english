package com.example.learning_english.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.model.PracticeTopic;

import java.util.List;

public class PracticeTopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<PracticeTopic> topicList;
    private final OnPracticeTopicClickListener listener;

    public interface OnPracticeTopicClickListener {
        void onPracticeTopicClick(PracticeTopic topic);
    }

    public PracticeTopicAdapter(List<PracticeTopic> topicList, OnPracticeTopicClickListener listener) {
        this.topicList = topicList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return topicList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == PracticeTopic.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_practice_header, parent, false);
            return new HeaderViewHolder(view);
        } else { // TYPE_ITEM
            View view = inflater.inflate(R.layout.item_practice_topic, parent, false);
            return new ItemViewHolder(view, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PracticeTopic topic = topicList.get(position);
        if (holder.getItemViewType() == PracticeTopic.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(topic);
        } else {
            ((ItemViewHolder) holder).bind(topic);
        }
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    // ViewHolder for Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.text_practice_header);
        }
        void bind(PracticeTopic topic) {
            headerTitle.setText(topic.title);
        }
    }

    // ViewHolder for Item
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        PracticeTopic currentTopic;
        ItemViewHolder(@NonNull View itemView, OnPracticeTopicClickListener listener) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.text_practice_topic_title);
            itemView.setOnClickListener(v -> {
                if (listener != null && currentTopic != null) {
                    listener.onPracticeTopicClick(currentTopic);
                }
            });
        }
        void bind(PracticeTopic topic) {
            currentTopic = topic;
            itemTitle.setText(topic.title);
        }
    }
}