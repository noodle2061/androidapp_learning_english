package com.example.learning_english.model.grammar;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp đại diện cho một mục nội dung cụ thể (đoạn văn, khối ví dụ, danh sách).
 */
public class ContentItem {
    /**
     * Loại nội dung: "paragraph", "example_block", "list".
     * Dùng để xác định cách hiển thị trong Adapter.
     */
    @SerializedName("type")
    public String type;

    /**
     * Nội dung văn bản (dùng cho type="paragraph").
     */
    @SerializedName("text")
    public String text;

    /**
     * Danh sách các mục (dùng cho type="list").
     */
    @SerializedName("items")
    public List<String> items;

    /**
     * Danh sách các ví dụ (dùng cho type="example_block").
     */
    @SerializedName("examples")
    public List<Example> examples;
}
