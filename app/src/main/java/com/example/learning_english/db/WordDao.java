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

    // Keep existing insert/update/delete methods for single words
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Word word);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertAll(List<Word> words);

    @Update
    void updateWord(Word word);

    @Delete
    void deleteWords(List<Word> words); // For deleting multiple selected words

    @Query("DELETE FROM words")
    void deleteAll(); // Keep if needed

    // Keep methods for finding/updating individual words
    @Query("SELECT * FROM words WHERE englishWord = :englishWord LIMIT 1")
    Word findByEnglishWord(String englishWord);

    @Query("UPDATE words SET vietnameseTranslation = :translation WHERE englishWord = :englishWord")
    void updateTranslation(String englishWord, String translation);

    // --- UPDATED/NEW QUERIES FOR PACKAGES ---

    // Get all words (might not be needed often if using package view)
    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    LiveData<List<Word>> getAllWords();

    // Get words for a specific package
    @Query("SELECT * FROM words WHERE packageId = :packageId ORDER BY englishWord ASC")
    LiveData<List<Word>> getWordsByPackageId(int packageId);

    // Update review status for all words in a specific package
    @Query("UPDATE words SET isForReview = :isForReview WHERE packageId = :packageId")
    void updateReviewStatusForPackage(int packageId, boolean isForReview);

    // Get all words (non-LiveData version) - useful for background checks
    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    List<Word> getAllWordsList();

    // --- SRS QUERIES (Potentially update if needed) ---
    // These might need filtering by package if you want SRS per package

    @Query("SELECT * FROM words WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    LiveData<List<Word>> getDueReviewWords(long currentTime);

    @Query("SELECT COUNT(id) FROM words WHERE nextReviewTimestamp <= :currentTime")
    LiveData<Integer> getDueReviewWordsCount(long currentTime);

    // Get manually marked words (might need package filter too)
    @Query("SELECT * FROM words WHERE isForReview = 1 ORDER BY timestamp DESC")
    LiveData<List<Word>> getManuallyMarkedReviewWords();

    // --- Optional: Query to get words NOT in any specific package ---
    // @Query("SELECT * FROM words WHERE packageId IS NULL OR packageId <= 0")
    // LiveData<List<Word>> getUnpackagedWords();
}
