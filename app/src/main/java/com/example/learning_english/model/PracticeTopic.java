// app/src/main/java/com/example/learning_english/model/PracticeTopic.java
// (Giữ nguyên như đã tạo trước đó)
package com.example.learning_english.model;

public class PracticeTopic {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public final int type;
    public final String title; // Tiêu đề hiển thị (cho cả header và item)
    public String id; // Dùng để lưu tên chủ đề thực tế cần truyền đi khi click item

    // Constructor for Header
    public PracticeTopic(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
        this.id = null; // Header không cần id
    }

    // Constructor for Item
    public PracticeTopic(String displayTitle, String actualTopicId) {
        this.type = TYPE_ITEM;
        this.title = displayTitle; // Tiêu đề hiển thị đầy đủ
        this.id = actualTopicId; // Tên chủ đề thực tế để điều hướng/xử lý
    }
}