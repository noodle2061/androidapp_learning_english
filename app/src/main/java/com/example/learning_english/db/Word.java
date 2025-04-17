package com.example.learning_english.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "words", indices = {@Index(value = {"englishWord"}, unique = true)})
public class Word {

    // Constants for SRS default values
    private static final long ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final float DEFAULT_EASE_FACTOR = 2.5f;

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String englishWord;

    public String vietnameseTranslation;

    public long timestamp; // Thời điểm từ được thêm vào

    // Gỡ bỏ defaultValue, migration sẽ xử lý nếu cột này được thêm trong quá khứ
    // @ColumnInfo(defaultValue = "0")
    public boolean isForReview;

    // --- SRS Fields ---
    // Gỡ bỏ defaultValue, MIGRATION_2_3 sẽ xử lý
    // @ColumnInfo(defaultValue = "0")
    public long nextReviewTimestamp;

    // Gỡ bỏ defaultValue, MIGRATION_2_3 sẽ xử lý
    // @ColumnInfo(defaultValue = "1")
    public int reviewIntervalDays;

    // Gỡ bỏ defaultValue, MIGRATION_2_3 sẽ xử lý
    // @ColumnInfo(defaultValue = "2.5")
    public float easeFactor;
    // -----------------

    // Constructor: Khởi tạo giá trị SRS ban đầu cho từ mới
    public Word(@NonNull String englishWord, String vietnameseTranslation) {
        this.englishWord = englishWord;
        this.vietnameseTranslation = vietnameseTranslation;
        this.timestamp = System.currentTimeMillis();
        this.isForReview = false; // Giá trị mặc định khi tạo đối tượng mới

        // Initialize SRS fields for new words
        this.nextReviewTimestamp = System.currentTimeMillis();
        this.reviewIntervalDays = 1;
        this.easeFactor = DEFAULT_EASE_FACTOR;
    }

    // Getters (Giữ nguyên)
    public int getId() { return id; }
    @NonNull public String getEnglishWord() { return englishWord; }
    public String getVietnameseTranslation() { return vietnameseTranslation; }
    public long getTimestamp() { return timestamp; }
    public boolean isForReview() { return isForReview; }
    public long getNextReviewTimestamp() { return nextReviewTimestamp; }
    public int getReviewIntervalDays() { return reviewIntervalDays; }
    public float getEaseFactor() { return easeFactor; }

    // Setters (Giữ nguyên)
    public void setForReview(boolean forReview) { isForReview = forReview; }
    public void setVietnameseTranslation(String translation) { this.vietnameseTranslation = translation; }
    public void setNextReviewTimestamp(long nextReviewTimestamp) { this.nextReviewTimestamp = nextReviewTimestamp; }
    public void setReviewIntervalDays(int reviewIntervalDays) { this.reviewIntervalDays = reviewIntervalDays; }
    public void setEaseFactor(float easeFactor) { this.easeFactor = easeFactor; }


    // equals & hashCode (Giữ nguyên)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return id == word.id &&
                timestamp == word.timestamp &&
                isForReview == word.isForReview &&
                nextReviewTimestamp == word.nextReviewTimestamp &&
                reviewIntervalDays == word.reviewIntervalDays &&
                Float.compare(word.easeFactor, easeFactor) == 0 &&
                englishWord.equals(word.englishWord) &&
                Objects.equals(vietnameseTranslation, word.vietnameseTranslation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, englishWord, vietnameseTranslation, timestamp, isForReview,
                nextReviewTimestamp, reviewIntervalDays, easeFactor);
    }
}
