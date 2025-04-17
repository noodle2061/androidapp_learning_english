package com.example.learning_english.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Word word);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertAll(List<Word> words);

    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    LiveData<List<Word>> getAllWords();

    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    List<Word> getAllWordsList();

    @Query("DELETE FROM words")
    void deleteAll();

    @Query("SELECT * FROM words WHERE englishWord = :englishWord LIMIT 1")
    Word findByEnglishWord(String englishWord);

    @Delete
    void deleteWords(List<Word> words);

    @Query("UPDATE words SET vietnameseTranslation = :translation WHERE englishWord = :englishWord")
    void updateTranslation(String englishWord, String translation);

    @Update
    void updateWord(Word word); // Dùng để cập nhật toàn bộ thông tin từ, bao gồm cả SRS

    // --- SRS Queries ---

    // Lấy danh sách các từ cần ôn tập (đã đến hạn)
    @Query("SELECT * FROM words WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    LiveData<List<Word>> getDueReviewWords(long currentTime);

    // Lấy số lượng từ cần ôn tập (đã đến hạn)
    @Query("SELECT COUNT(id) FROM words WHERE nextReviewTimestamp <= :currentTime")
    LiveData<Integer> getDueReviewWordsCount(long currentTime);

    // -----------------


    // (Tùy chọn) Lấy danh sách các từ được đánh dấu ôn tập thủ công (isForReview = 1)
    // Giữ lại nếu bạn vẫn muốn dùng song song cả isForReview và SRS
    @Query("SELECT * FROM words WHERE isForReview = 1 ORDER BY timestamp DESC")
    LiveData<List<Word>> getManuallyMarkedReviewWords();

}
