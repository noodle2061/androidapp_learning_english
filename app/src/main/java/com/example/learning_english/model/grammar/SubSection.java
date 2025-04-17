package com.example.learning_english.model.grammar;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp đại diện cho một SubSection (phần nhỏ hơn) trong một Section.
 */
public class SubSection {
    @SerializedName("subSectionTitle")
    public String subSectionTitle; // Tiêu đề của sub-section (ví dụ: "1.1. Khái Niệm", có thể rỗng)

    @SerializedName("content")
    public List<ContentItem> content; // Danh sách các mục nội dung (đoạn văn, ví dụ, list)
}
