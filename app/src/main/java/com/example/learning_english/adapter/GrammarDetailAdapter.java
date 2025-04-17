package com.example.learning_english.adapter;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.model.grammar.ContentItem;
import com.example.learning_english.model.grammar.Example;
// Import các lớp model;

import java.util.ArrayList;
import java.util.List;

public class GrammarDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Định nghĩa các View Type
    private static final int VIEW_TYPE_SECTION_HEADER = 1;
    private static final int VIEW_TYPE_SUBSECTION_HEADER = 2;
    private static final int VIEW_TYPE_PARAGRAPH = 3;
    private static final int VIEW_TYPE_EXAMPLE_BLOCK = 4;
    private static final int VIEW_TYPE_LIST = 5;

    private final Context context;
    // Danh sách phẳng chứa các đối tượng đại diện cho từng item cần hiển thị
    private final List<Object> displayItems;

    public GrammarDetailAdapter(Context context, List<Object> displayItems) {
        this.context = context;
        // Khởi tạo với danh sách rỗng hoặc danh sách đã được làm phẳng
        this.displayItems = displayItems != null ? displayItems : new ArrayList<>();
    }

    // --- Xác định View Type cho từng vị trí ---
    @Override
    public int getItemViewType(int position) {
        Object item = displayItems.get(position);
        if (item instanceof String && ((String) item).startsWith("SECTION_")) {
            return VIEW_TYPE_SECTION_HEADER;
        } else if (item instanceof String && ((String) item).startsWith("SUBSECTION_")) {
            return VIEW_TYPE_SUBSECTION_HEADER;
        } else if (item instanceof ContentItem) {
            ContentItem content = (ContentItem) item;
            switch (content.type) {
                case "paragraph": return VIEW_TYPE_PARAGRAPH;
                case "example_block": return VIEW_TYPE_EXAMPLE_BLOCK;
                case "list": return VIEW_TYPE_LIST;
                default:
                    // Log or handle unknown type
                    return -1;
            }
        }
        // Log or handle unknown item type
        return -1;
    }

    // --- Tạo ViewHolder tương ứng với View Type ---
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        switch (viewType) {
            case VIEW_TYPE_SECTION_HEADER:
                view = inflater.inflate(R.layout.item_grammar_section_header, parent, false);
                return new SectionHeaderViewHolder(view);
            case VIEW_TYPE_SUBSECTION_HEADER:
                view = inflater.inflate(R.layout.item_grammar_subsection_header, parent, false);
                return new SubSectionHeaderViewHolder(view);
            case VIEW_TYPE_PARAGRAPH:
                view = inflater.inflate(R.layout.item_grammar_paragraph, parent, false);
                return new ParagraphViewHolder(view);
            case VIEW_TYPE_EXAMPLE_BLOCK:
                view = inflater.inflate(R.layout.item_grammar_example_block, parent, false);
                return new ExampleBlockViewHolder(view);
            case VIEW_TYPE_LIST:
                view = inflater.inflate(R.layout.item_grammar_list, parent, false);
                return new ListViewHolder(view);
            default:
                // Return a default ViewHolder or throw an exception
                view = new View(context); // Simple empty view
                return new RecyclerView.ViewHolder(view) {};
        }
    }

    // --- Gắn dữ liệu vào ViewHolder ---
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayItems.get(position);
        try { // Add try-catch for safety
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SECTION_HEADER:
                    ((SectionHeaderViewHolder) holder).bind((String) item);
                    break;
                case VIEW_TYPE_SUBSECTION_HEADER:
                    ((SubSectionHeaderViewHolder) holder).bind((String) item);
                    break;
                case VIEW_TYPE_PARAGRAPH:
                    ((ParagraphViewHolder) holder).bind((ContentItem) item);
                    break;
                case VIEW_TYPE_EXAMPLE_BLOCK:
                    ((ExampleBlockViewHolder) holder).bind((ContentItem) item);
                    break;
                case VIEW_TYPE_LIST:
                    ((ListViewHolder) holder).bind((ContentItem) item);
                    break;
            }
        } catch (ClassCastException e) {
            // Log error if casting fails
            Log.e("GrammarDetailAdapter", "Error binding view holder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    // --- Các lớp ViewHolder ---

    // ViewHolder cho Tiêu đề Section
    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_section_title);
        }
        void bind(String sectionTitleWithPrefix) {
            if (sectionTitleWithPrefix != null && sectionTitleWithPrefix.startsWith("SECTION_")) {
                title.setText(sectionTitleWithPrefix.substring("SECTION_".length()));
            } else {
                title.setText(sectionTitleWithPrefix); // Fallback
            }
        }
    }

    // ViewHolder cho Tiêu đề SubSection
    static class SubSectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        SubSectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_subsection_title);
        }
        void bind(String subSectionTitleWithPrefix) {
            String actualTitle = "";
            if (subSectionTitleWithPrefix != null && subSectionTitleWithPrefix.startsWith("SUBSECTION_")) {
                actualTitle = subSectionTitleWithPrefix.substring("SUBSECTION_".length());
            } else if (subSectionTitleWithPrefix != null) {
                actualTitle = subSectionTitleWithPrefix; // Fallback if prefix missing
            }

            if (actualTitle.isEmpty()) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                title.setText(actualTitle);
            }
        }
    }

    // ViewHolder cho Đoạn văn
    static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        TextView paragraph;
        ParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            paragraph = itemView.findViewById(R.id.text_paragraph);
        }
        void bind(ContentItem content) {
            if (content != null && content.text != null) {
                // Use Html.fromHtml for basic formatting if needed
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    paragraph.setText(Html.fromHtml(content.text, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    paragraph.setText(Html.fromHtml(content.text));
                }
                // paragraph.setText(content.text); // Or just plain text
            } else {
                paragraph.setText(""); // Clear text if content is null
            }
        }
    }

    // ViewHolder cho Khối Ví dụ
    class ExampleBlockViewHolder extends RecyclerView.ViewHolder {
        LinearLayout exampleContainer;
        LayoutInflater inflater;

        ExampleBlockViewHolder(@NonNull View itemView) {
            super(itemView);
            exampleContainer = itemView.findViewById(R.id.container_examples);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(ContentItem content) {
            exampleContainer.removeAllViews();
            if (content != null && content.examples != null && !content.examples.isEmpty()) {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                for (Example example : content.examples) {
                    if (example == null) continue; // Skip null examples

                    View exampleView = inflater.inflate(R.layout.item_grammar_single_example, exampleContainer, false);
                    TextView sentenceView = exampleView.findViewById(R.id.text_example_sentence);
                    TextView noteView = exampleView.findViewById(R.id.text_example_note);

                    // TODO: Handle potential formatting in sentence (e.g., italics for *)
                    sentenceView.setText(example.sentence != null ? example.sentence.replace("*", "") : "");

                    if (example.note != null && !example.note.isEmpty()) {
                        noteView.setText("(" + example.note + ")");
                        noteView.setVisibility(View.VISIBLE);
                    } else {
                        noteView.setVisibility(View.GONE);
                    }
                    exampleContainer.addView(exampleView);
                }
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }
    }


    // ViewHolder cho Danh sách (bullet list)
    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView listText;
        ListViewHolder(@NonNull View itemView) {
            super(itemView);
            listText = itemView.findViewById(R.id.text_list);
        }
        void bind(ContentItem content) {
            if (content != null && content.items != null && !content.items.isEmpty()) {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                StringBuilder sb = new StringBuilder();
                for (String item : content.items) {
                    if (item != null) {
                        // TODO: Handle potential formatting in item (e.g., italics for *)
                        String cleanedItem = item.replace("*", "");
                        sb.append("• ").append(cleanedItem).append("\n");
                    }
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                listText.setText(sb.toString());
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                listText.setText("");
            }
        }
    }
}
    