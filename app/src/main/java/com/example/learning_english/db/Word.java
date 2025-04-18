package com.example.learning_english.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey; // Import ForeignKey
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

// Add ForeignKey constraint and index for packageId
@Entity(tableName = "words",
        indices = {
                @Index(value = {"englishWord"}, unique = true),
                @Index(value = {"packageId"}) // Index for faster package lookups - GIỮ LẠI INDEX Ở ĐÂY
        },
        foreignKeys = @ForeignKey(entity = VocabularyPackage.class,
                parentColumns = "id", // Column in the VocabularyPackage table
                childColumns = "packageId", // Column in this Word table
                onDelete = ForeignKey.CASCADE) // Optional: Define behavior when a package is deleted
)
public class Word {

    // Constants for SRS default values
    private static final long ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final float DEFAULT_EASE_FACTOR = 2.5f;
    private static final int DEFAULT_PACKAGE_ID = 1; // Define default package ID

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String englishWord;

    public String vietnameseTranslation;

    public long timestamp;

    public boolean isForReview;

    // --- SRS Fields ---
    public long nextReviewTimestamp;
    public int reviewIntervalDays;
    public float easeFactor;
    // -----------------

    // --- NEW FIELD ---
    // *** XÓA index = true KHỎI ĐÂY ***
    @ColumnInfo(defaultValue = "" + DEFAULT_PACKAGE_ID) // Set default value for the column
    public int packageId;
    // ---------------

    // --- UPDATED CONSTRUCTOR ---
    // Make sure the constructor accepts packageId or sets a default
    // This constructor assumes you provide the packageId when creating a new Word object
    public Word(@NonNull String englishWord, String vietnameseTranslation, int packageId) {
        this.englishWord = englishWord;
        this.vietnameseTranslation = vietnameseTranslation;
        this.packageId = packageId; // Assign provided package ID
        this.timestamp = System.currentTimeMillis();
        this.isForReview = false;
        this.nextReviewTimestamp = System.currentTimeMillis(); // Review immediately initially
        this.reviewIntervalDays = 1;
        this.easeFactor = DEFAULT_EASE_FACTOR;
    }

    // --- Optional: Add a constructor that uses the default package ID ---
    // You might not need this if you always determine the package ID before creating a Word
    /*
    public Word(@NonNull String englishWord, String vietnameseTranslation) {
        this(englishWord, vietnameseTranslation, DEFAULT_PACKAGE_ID); // Call the main constructor with default ID
    }
    */
    // -------------------------


    // --- Getters ---
    public int getId() { return id; }
    @NonNull public String getEnglishWord() { return englishWord; }
    public String getVietnameseTranslation() { return vietnameseTranslation; }
    public long getTimestamp() { return timestamp; }
    public boolean isForReview() { return isForReview; }
    public long getNextReviewTimestamp() { return nextReviewTimestamp; }
    public int getReviewIntervalDays() { return reviewIntervalDays; }
    public float getEaseFactor() { return easeFactor; }
    public int getPackageId() { return packageId; } // Getter for packageId
    // ---------------

    // --- Setters ---
    public void setForReview(boolean forReview) { isForReview = forReview; }
    public void setVietnameseTranslation(String translation) { this.vietnameseTranslation = translation; }
    public void setNextReviewTimestamp(long nextReviewTimestamp) { this.nextReviewTimestamp = nextReviewTimestamp; }
    public void setReviewIntervalDays(int reviewIntervalDays) { this.reviewIntervalDays = reviewIntervalDays; }
    public void setEaseFactor(float easeFactor) { this.easeFactor = easeFactor; }
    // Setter for packageId might be needed if you allow moving words between packages
    public void setPackageId(int packageId) { this.packageId = packageId; }
    // ---------------


    // --- equals & hashCode (Updated) ---
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
                packageId == word.packageId && // Compare packageId
                englishWord.equals(word.englishWord) &&
                Objects.equals(vietnameseTranslation, word.vietnameseTranslation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, englishWord, vietnameseTranslation, timestamp, isForReview,
                nextReviewTimestamp, reviewIntervalDays, easeFactor, packageId); // Include packageId
    }
    // ---------------------------------
}
