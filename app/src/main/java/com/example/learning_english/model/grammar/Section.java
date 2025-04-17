package com.example.learning_english.model.grammar;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp đại diện cho một Section lớn trong chủ đề ngữ pháp.
 */
public class Section {
    @SerializedName("sectionTitle")
    public String sectionTitle; // Tiêu đề của section (ví dụ: "1. Khái Niệm và Cách Dùng")

    @SerializedName("subSections")
    public List<SubSection> subSections; // Danh sách các sub-section con
}
