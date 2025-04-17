package com.example.learning_english.db; // Hoặc package phù hợp

import androidx.room.Dao;
import androidx.room.Query;
import androidx.annotation.Nullable;

@Dao
public interface DictionaryDao {

    /**
     * Tìm kiếm toàn bộ mục từ điển offline dựa trên từ.
     * Sử dụng LIKE để tìm không phân biệt hoa thường (do đã có collate = NOCASE).
     * LIMIT 1 để lấy kết quả đầu tiên.
     * @param searchWord Từ cần tìm kiếm.
     * @return Đối tượng DictionaryEntry hoặc null nếu không tìm thấy.
     */
    // *** THAY ĐỔI: Đổi tên và kiểu trả về ***
    @Query("SELECT * FROM dictionary WHERE word = :searchWord LIMIT 1")
    @Nullable
    DictionaryEntry findEntryByWord(String searchWord);

        /* Bỏ phương thức cũ chỉ lấy definition nếu không cần nữa
        @Query("SELECT definition FROM dictionary WHERE word = :searchWord LIMIT 1")
        @Nullable
        String findDefinitionByWord(String searchWord);
        */

    // Thêm các phương thức truy vấn khác nếu cần
}
    