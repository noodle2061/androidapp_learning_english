package com.example.learning_english.model.grammar;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp gốc đại diện cho toàn bộ tệp JSON của một chủ đề ngữ pháp.
 */
public class GrammarTopic {
    @SerializedName("topicTitle")
    public String topicTitle; // Tiêu đề chính của chủ đề

    @SerializedName("sections")
    public List<Section> sections; // Danh sách các phần lớn trong chủ đề

    /**
     * Trường tạm thời (transient) để lưu tên tệp JSON gốc.
     * Gson sẽ bỏ qua trường này khi parse.
     * Hữu ích để biết tệp nào đã được load hoặc để điều hướng.
     */
    public transient String sourceFileName;
}
