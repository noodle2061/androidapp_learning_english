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
public interface VocabularyPackageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(VocabularyPackage pkg); // Returns the new rowId

    @Update
    void update(VocabularyPackage pkg);

    @Delete
    void delete(VocabularyPackage pkg);

    @Query("SELECT * FROM vocabulary_package ORDER BY name ASC")
    LiveData<List<VocabularyPackage>> getAllPackages();

    @Query("SELECT * FROM vocabulary_package WHERE id = :id")
    VocabularyPackage getPackageById(int id); // Non-LiveData version

    @Query("SELECT * FROM vocabulary_package WHERE name = :name LIMIT 1")
    VocabularyPackage getPackageByName(String name); // Find by name if needed

    @Query("DELETE FROM vocabulary_package WHERE id = :packageId")
    void deletePackageById(int packageId);

    // You might add more specific queries if needed
}
