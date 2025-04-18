package com.example.learning_english.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "vocabulary_package") // Tên bảng
public class VocabularyPackage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    // Optional: Add timestamp or other fields if needed
    // public long creationTimestamp;

    // Constructor
    public VocabularyPackage(@NonNull String name) {
        this.name = name;
        // this.creationTimestamp = System.currentTimeMillis();
    }

    // Getters (needed by Room)
    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    // equals() and hashCode() for DiffUtil and comparisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VocabularyPackage that = (VocabularyPackage) o;
        return id == that.id && name.equals(that.name);
        // Add other fields to comparison if they exist
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
        // Add other fields to hash if they exist
    }

    // *** ADD THIS METHOD ***
    /**
     * Returns the package name for display in Spinners or other UI elements.
     * ArrayAdapter uses this method by default.
     */
    @NonNull
    @Override
    public String toString() {
        return this.name; // Return the actual package name
    }
    // **********************
}
