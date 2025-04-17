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

// *** THAY ĐỔI: Thêm DictionaryEntry vào entities và tăng version ***
@Database(entities = {Word.class, DictionaryEntry.class}, version = 4, exportSchema = false) // Tăng version lên 4
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    public abstract WordDao wordDao();
    // *** THAY ĐỔI: Thêm phương thức abstract cho DictionaryDao ***
    public abstract DictionaryDao dictionaryDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migration từ version 3 (có SRS) sang 4 (thêm bảng dictionary)
    // Chỉ cần tạo bảng mới, không cần thay đổi bảng words
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.i(LOG_TAG, "Migrating database from version 3 to 4");
            // Tạo bảng dictionary mới
            database.execSQL("CREATE TABLE IF NOT EXISTS `dictionary` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL COLLATE NOCASE, `pronunciation` TEXT, `definition` TEXT NOT NULL)");
            // Tạo index cho bảng dictionary
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dictionary_word` ON `dictionary` (`word`)");
            Log.i(LOG_TAG, "Database migration 3-4 finished.");
        }
    };

    // Migration cũ từ 2 sang 3 (giữ lại nếu cần)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Log.i(LOG_TAG, "Migrating database from version 2 to 3");
            database.execSQL("ALTER TABLE words ADD COLUMN nextReviewTimestamp INTEGER NOT NULL DEFAULT " + System.currentTimeMillis());
            database.execSQL("ALTER TABLE words ADD COLUMN reviewIntervalDays INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE words ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5");
            Log.i(LOG_TAG, "Database migration 2-3 finished.");
        }
    };


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.i(LOG_TAG, "Creating new database instance");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "word_database")
                            // *** THAY ĐỔI: Thêm createFromAsset ***
                            .createFromAsset("database/dictionary.db") // Đường dẫn trong thư mục assets
                            // Thêm các migrations cần thiết
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // Thêm migration mới
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
    