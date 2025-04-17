package com.example.learning_english.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.model.grammar.GrammarTopic; // Import model

import java.util.List;

public class GrammarListAdapter extends RecyclerView.Adapter<GrammarListAdapter.TopicViewHolder> {

    private final List<GrammarTopic> topicList;
    private final OnTopicClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnTopicClickListener {
        void onTopicClick(GrammarTopic topic);
    }

    public GrammarListAdapter(List<GrammarTopic> topicList, OnTopicClickListener listener) {
        this.topicList = topicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grammar_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        GrammarTopic topic = topicList.get(position);
        holder.bind(topic, listener);
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    // ViewHolder cho một chủ đề
    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView topicTitle;

        TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            topicTitle = itemView.findViewById(R.id.text_grammar_topic_title);
        }

        void bind(final GrammarTopic topic, final OnTopicClickListener listener) {
            topicTitle.setText(topic.topicTitle);
            itemView.setOnClickListener(v -> listener.onTopicClick(topic));
        }
    }
}
