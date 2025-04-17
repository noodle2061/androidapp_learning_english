package com.example.learning_english.db; // Hoặc package phù hợp

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

// Đặt tên bảng là 'dictionary' và tạo index trên cột 'word' để tìm kiếm nhanh
@Entity(tableName = "dictionary", indices = {@Index(value = {"word"}, unique = true)})
public class DictionaryEntry {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID tự tăng (không thực sự dùng để tra cứu nhưng cần cho Room)

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE) // Tìm kiếm không phân biệt hoa thường
    public String word; // Từ tiếng Anh (key tra cứu)

    @Nullable // Phiên âm có thể có hoặc không
    public String pronunciation; // Cột lưu phiên âm (tùy chọn)

    @NonNull
    public String definition; // Cột lưu toàn bộ phần định nghĩa và loại từ

    // Constructor
    public DictionaryEntry(@NonNull String word, @Nullable String pronunciation, @NonNull String definition) {
        this.word = word;
        this.pronunciation = pronunciation;
        this.definition = definition;
    }

    // Getters (Room cần để truy cập dữ liệu)
    public int getId() { return id; }
    @NonNull public String getWord() { return word; }
    @Nullable public String getPronunciation() { return pronunciation; }
    @NonNull public String getDefinition() { return definition; }
}
    