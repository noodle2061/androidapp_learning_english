package com.example.learning_english.model.grammar;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp đại diện cho một câu ví dụ và ghi chú đi kèm.
 */
public class Example {
    @SerializedName("sentence")
    public String sentence; // Câu ví dụ

    @SerializedName("note")
    public String note; // Ghi chú hoặc bản dịch cho ví dụ (có thể rỗng)
}
