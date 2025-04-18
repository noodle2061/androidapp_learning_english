package com.example.learning_english.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// 1. Add VocabularyPackage.class to entities
// 2. Increment version number (e.g., from 4 to 5 if previous was 4)
@Database(entities = {Word.class, DictionaryEntry.class, VocabularyPackage.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    public abstract WordDao wordDao();
    public abstract DictionaryDao dictionaryDao();
    // 3. Add abstract method for the new DAO
    public abstract VocabularyPackageDao vocabularyPackageDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // --- MIGRATIONS ---
    // Keep existing migrations

    // Migration from 3 to 4 (Example - kept from previous state)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.i(LOG_TAG, "Migrating database from version 3 to 4");
            database.execSQL("CREATE TABLE IF NOT EXISTS `dictionary` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL COLLATE NOCASE, `pronunciation` TEXT, `definition` TEXT NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dictionary_word` ON `dictionary` (`word`)");
            Log.i(LOG_TAG, "Database migration 3-4 finished.");
        }
    };

    // 4. Define the new migration (e.g., version 4 to 5)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.i(LOG_TAG, "Migrating database from version 4 to 5");

            // Step 1: Create the new vocabulary_package table
            database.execSQL("CREATE TABLE IF NOT EXISTS `vocabulary_package` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
            Log.i(LOG_TAG, "Created vocabulary_package table.");

            // Step 2: Add the packageId column to the words table
            // Provide a default value (e.g., 1, assuming you'll insert a default package)
            // Or handle nullability if words can exist without a package initially
            database.execSQL("ALTER TABLE `words` ADD COLUMN `packageId` INTEGER NOT NULL DEFAULT 1");
            Log.i(LOG_TAG, "Added packageId column to words table.");

            // Step 3: (Optional but Recommended) Create an index on the new column for performance
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_words_packageId` ON `words` (`packageId`)");
            Log.i(LOG_TAG, "Created index on words.packageId.");

            // Step 4: (Crucial if using default value) Insert a default package if needed
            // Make sure the default package ID (e.g., 1) matches the default value above
            database.execSQL("INSERT OR IGNORE INTO `vocabulary_package` (`id`, `name`) VALUES (1, 'Mặc định')"); // 'Mặc định' = Default
            Log.i(LOG_TAG, "Inserted default package (ID=1).");


            // Step 5: Recreate the words table with the foreign key constraint (Complex way)
            // This is the robust way to add foreign keys to existing tables in SQLite.
            // a. Create a temporary table with the new schema including the foreign key
            /*
            database.execSQL("CREATE TABLE `words_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `englishWord` TEXT NOT NULL, " +
                             "`vietnameseTranslation` TEXT, `timestamp` INTEGER NOT NULL, `isForReview` INTEGER NOT NULL DEFAULT 0, " +
                             "`nextReviewTimestamp` INTEGER NOT NULL DEFAULT 0, `reviewIntervalDays` INTEGER NOT NULL DEFAULT 1, " +
                             "`easeFactor` REAL NOT NULL DEFAULT 2.5, `packageId` INTEGER NOT NULL DEFAULT 1, " +
                             "FOREIGN KEY(`packageId`) REFERENCES `vocabulary_package`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            Log.i(LOG_TAG, "Created temporary words_new table.");

            // b. Copy data from the old table to the new table
            database.execSQL("INSERT INTO `words_new` (id, englishWord, vietnameseTranslation, timestamp, isForReview, nextReviewTimestamp, reviewIntervalDays, easeFactor, packageId) " +
                             "SELECT id, englishWord, vietnameseTranslation, timestamp, isForReview, nextReviewTimestamp, reviewIntervalDays, easeFactor, packageId FROM `words`");
            Log.i(LOG_TAG, "Copied data to words_new table.");

            // c. Drop the old table
            database.execSQL("DROP TABLE `words`");
            Log.i(LOG_TAG, "Dropped old words table.");

            // d. Rename the new table to the original name
            database.execSQL("ALTER TABLE `words_new` RENAME TO `words`");
            Log.i(LOG_TAG, "Renamed words_new to words.");

            // e. Recreate indexes on the new table (englishWord unique index)
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_words_englishWord` ON `words` (`englishWord`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_words_packageId` ON `words` (`packageId`)"); // Recreate packageId index too
            Log.i(LOG_TAG, "Recreated indexes on words table.");
            */
            // Note: The complex way (recreating table) is generally safer for adding constraints.
            // If you only added the column and index without the Foreign Key constraint initially,
            // you might need to use the complex method in a subsequent migration if you want the constraint enforced by SQLite.
            // For this migration, adding the column and index might be sufficient if FK enforcement isn't strictly needed immediately.

            Log.i(LOG_TAG, "Database migration 4-5 finished.");
        }
    };
    // ---------------

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.i(LOG_TAG, "Creating new database instance");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "word_database")
                            // Remove createFromAsset if you are managing the schema entirely with Room
                            // .createFromAsset("database/dictionary.db") // Keep if dictionary.db is pre-populated and static
                            // 5. Add the new migration
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5) // Add MIGRATION_4_5
                            // Consider fallbackToDestructiveMigration() only during development
                            // .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
