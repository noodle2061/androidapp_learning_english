package com.example.learning_english.viewmodel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations; // Import Transformations

import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.NetworkError;
import com.android.volley.ServerError;
import com.android.volley.ParseError;

import com.example.learning_english.R;
import com.example.learning_english.db.AppDatabase;
import com.example.learning_english.db.VocabularyPackage; // Import new entity
import com.example.learning_english.db.VocabularyPackageDao; // Import new DAO
import com.example.learning_english.db.Word;
import com.example.learning_english.db.WordDao;
import com.example.learning_english.network.ApiClient;
import com.example.learning_english.network.ApiResponseListener;
import com.example.learning_english.utils.SpacedRepetitionScheduler;
import com.example.learning_english.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class VocabularyViewModel extends AndroidViewModel {

    private static final String TAG = "VocabularyViewModel";
    private WordDao mWordDao;
    private VocabularyPackageDao mPackageDao; // Add Package DAO
    private LiveData<List<Word>> mAllWords; // Keep if needed elsewhere
    private LiveData<List<VocabularyPackage>> mAllPackages; // LiveData for packages
    private ExecutorService mExecutorService;
    private ApiClient apiClient;

    // LiveData for UI state
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> mMessage = new MutableLiveData<>(); // For toasts/snackbar messages

    // LiveData for selected package and its words
    private MutableLiveData<Integer> mSelectedPackageId = new MutableLiveData<>();
    private LiveData<List<Word>> mWordsForSelectedPackage;

    // LiveData for SRS review counts/words (keep existing)
    private LiveData<Integer> mDueReviewCount;
    private LiveData<List<Word>> mDueReviewWords;

    // Constants
    // *** Make DEFAULT_PACKAGE_ID public or accessible ***
    public static final int DEFAULT_PACKAGE_ID = 1; // ID for the default package

    public VocabularyViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        mWordDao = db.wordDao();
        mPackageDao = db.vocabularyPackageDao(); // Initialize Package DAO
        mExecutorService = AppDatabase.databaseWriteExecutor;
        apiClient = ApiClient.getInstance(application);

        // Initialize LiveData
        mAllWords = mWordDao.getAllWords(); // Keep if needed
        mAllPackages = mPackageDao.getAllPackages(); // Get all packages

        // Use Transformations.switchMap to observe words based on selectedPackageId
        mWordsForSelectedPackage = Transformations.switchMap(mSelectedPackageId, id -> {
            if (id == null || id <= 0) {
                // No package selected, return LiveData with empty list
                MutableLiveData<List<Word>> emptyListLiveData = new MutableLiveData<>();
                emptyListLiveData.setValue(new ArrayList<>());
                return emptyListLiveData;
            } else {
                // Package selected, return LiveData from DAO
                return mWordDao.getWordsByPackageId(id);
            }
        });

        // Initialize SRS LiveData (keep existing)
        long currentTime = System.currentTimeMillis();
        mDueReviewCount = mWordDao.getDueReviewWordsCount(currentTime);
        mDueReviewWords = mWordDao.getDueReviewWords(currentTime);
    }

    // --- Getters for LiveData ---
    public LiveData<List<VocabularyPackage>> getAllPackages() { return mAllPackages; }
    public LiveData<List<Word>> getWordsForSelectedPackage() { return mWordsForSelectedPackage; }
    public LiveData<Boolean> isLoading() { return mIsLoading; }
    public LiveData<String> getMessage() { return mMessage; }
    public LiveData<Integer> getDueReviewCount() { return mDueReviewCount; }
    public LiveData<List<Word>> getDueReviewWords() { return mDueReviewWords; }
    // public LiveData<List<Word>> getAllWords() { return mAllWords; } // Expose if needed

    // --- Package Management ---

    /** Selects a package to display its words. */
    public void selectPackage(int packageId) {
        mSelectedPackageId.setValue(packageId);
        Log.d(TAG, "Selected package ID: " + packageId);
    }

    /** Clears the current package selection. */
    public void clearSelectedPackage() {
        mSelectedPackageId.setValue(null); // Set to null to trigger empty list in switchMap
        Log.d(TAG, "Cleared package selection");
    }

    /** Adds a new empty package. */
    public void addEmptyPackage(String name) {
        mExecutorService.execute(() -> {
            try {
                // Optional: Check if package name already exists
                VocabularyPackage existing = mPackageDao.getPackageByName(name);
                if (existing != null) {
                    postMessage("Gói '" + name + "' đã tồn tại.");
                    return;
                }
                VocabularyPackage newPackage = new VocabularyPackage(name);
                long newId = mPackageDao.insert(newPackage);
                if (newId > 0) {
                    postMessage("Đã thêm gói mới: " + name);
                    Log.i(TAG, "Added new package: " + name + " with ID: " + newId);
                } else {
                    postMessage("Lỗi khi thêm gói mới.");
                    Log.e(TAG, "Error inserting new package: " + name);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception adding package", e);
                postMessage("Lỗi: " + e.getMessage());
            }
        });
    }

    /** Updates the 'isForReview' status for all words in a package. */
    public void updatePackageReviewStatus(int packageId, boolean isChecked) {
        mExecutorService.execute(() -> {
            try {
                mWordDao.updateReviewStatusForPackage(packageId, isChecked);
                Log.i(TAG, "Updated review status for package ID " + packageId + " to " + isChecked);
                // No message needed here, Activity shows a toast immediately
                // Optional: Trigger refresh of word list if necessary, though LiveData should handle it.
            } catch (Exception e) {
                Log.e(TAG, "Exception updating package review status", e);
                postMessage("Lỗi cập nhật trạng thái gói: " + e.getMessage());
            }
        });
    }

    // --- Word Management ---

    /**
     * Adds words from input string to the specified package.
     * Fetches translations if needed.
     * @param inputText The string containing words (one per line).
     * @param targetPackageId The ID of the package to add words to.
     */
    public void addOrUpdateWordsFromString(String inputText, int targetPackageId) { // Added targetPackageId parameter
        Log.d(TAG, "addOrUpdateWordsFromString called for package ID: " + targetPackageId);

        postLoading(true); // Indicate loading start
        mExecutorService.execute(() -> {
            try {
                // Get all existing words to check for duplicates efficiently
                List<Word> allExistingWords = mWordDao.getAllWordsList();
                Set<String> existingEnglishWords = new HashSet<>();
                Map<String, Word> existingWordMap = new HashMap<>();
                for (Word w : allExistingWords) {
                    String lowerCaseWord = w.englishWord.toLowerCase().trim();
                    existingEnglishWords.add(lowerCaseWord);
                    existingWordMap.put(lowerCaseWord, w);
                }

                String[] lines = inputText.split("\\r?\\n");
                List<String> wordsToTranslate = new ArrayList<>();
                List<Word> wordsToInsertImmediately = new ArrayList<>();
                String noTranslationPlaceholder = getApplication().getString(R.string.no_translation_placeholder);

                for (String line : lines) {
                    String englishWord = line.trim();
                    if (!englishWord.isEmpty()) {
                        String lowerCaseWord = englishWord.toLowerCase();
                        if (!existingEnglishWords.contains(lowerCaseWord)) {
                            // New word, prepare for insertion and translation
                            // *** Use the targetPackageId passed to the method ***
                            Word newWord = new Word(englishWord, noTranslationPlaceholder, targetPackageId);
                            wordsToInsertImmediately.add(newWord);
                            wordsToTranslate.add(englishWord);
                            existingEnglishWords.add(lowerCaseWord); // Add to set to avoid duplicates within the input
                            Log.d(TAG, "New word prepared: " + englishWord + " for package " + targetPackageId);
                        } else {
                            // Word exists, check if translation is needed
                            Word existingWord = existingWordMap.get(lowerCaseWord);
                            if (existingWord != null && (existingWord.getVietnameseTranslation() == null ||
                                    existingWord.getVietnameseTranslation().isEmpty() ||
                                    existingWord.getVietnameseTranslation().startsWith("Lỗi") ||
                                    existingWord.getVietnameseTranslation().equals(noTranslationPlaceholder))) {
                                // Add to translation list only if current translation is invalid/missing
                                wordsToTranslate.add(englishWord);
                                Log.d(TAG, "Existing word needs re-translation: " + englishWord);
                                // Optional: Update packageId if the word exists in a different package?
                                if (existingWord.getPackageId() != targetPackageId) {
                                    Log.w(TAG, "Word '" + englishWord + "' exists in package " + existingWord.getPackageId() + ". Keeping it there for now.");
                                    // To move it:
                                    // existingWord.setPackageId(targetPackageId);
                                    // mWordDao.updateWord(existingWord);
                                }
                            } else {
                                Log.d(TAG, "Word already exists with translation: " + englishWord);
                                // Optional: Move word to current package if it exists elsewhere?
                                if (existingWord != null && existingWord.getPackageId() != targetPackageId) {
                                    Log.w(TAG, "Word '" + englishWord + "' exists in package " + existingWord.getPackageId() + ". Keeping it there for now.");
                                    // To move it:
                                    // existingWord.setPackageId(targetPackageId);
                                    // mWordDao.updateWord(existingWord);
                                }
                            }
                        }
                    }
                }

                // Insert new words immediately
                if (!wordsToInsertImmediately.isEmpty()) {
                    mWordDao.insertAll(wordsToInsertImmediately);
                    Log.i(TAG, "Inserted " + wordsToInsertImmediately.size() + " new words into package " + targetPackageId);
                }

                // Call translation API if needed
                if (!wordsToTranslate.isEmpty()) {
                    Log.i(TAG, "Found " + wordsToTranslate.size() + " words needing translation. Calling API...");
                    callTranslationApiViaClient(wordsToTranslate); // API call will handle setting loading to false
                } else {
                    Log.i(TAG, "No words need API translation call.");
                    postLoading(false); // Set loading false if no API call
                    if (!wordsToInsertImmediately.isEmpty()) {
                        postMessage(getApplication().getString(R.string.words_saved_no_translation_needed));
                    } else {
                        postMessage(getApplication().getString(R.string.no_new_words_added));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing words from string", e);
                postMessage("Lỗi xử lý từ: " + e.getMessage());
                postLoading(false);
            }
        });
    }


    /** Deletes a single word. */
    public void deleteSingleWord(Word word) {
        if (word == null) return;
        mExecutorService.execute(() -> {
            try {
                mWordDao.deleteWords(List.of(word)); // deleteWords accepts a list
                String message = getApplication().getResources().getQuantityString(R.plurals.words_deleted_snackbar, 1, 1);
                postMessage(message);
                Log.i(TAG, "Deleted word: " + word.getEnglishWord());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting word", e);
                postMessage("Lỗi xóa từ: " + e.getMessage());
            }
        });
    }

    /** Sets the manual review mark for a word. */
    public void setWordReviewMark(Word word, boolean isMarked) {
        if (word == null) return;
        mExecutorService.execute(() -> {
            try {
                word.setForReview(isMarked);
                mWordDao.updateWord(word);
                // Message is handled by Activity/Adapter interaction
                Log.d(TAG, "Set isForReview mark for '" + word.getEnglishWord() + "' to " + isMarked);
            } catch (Exception e) {
                Log.e(TAG, "Error updating review mark", e);
                postMessage("Lỗi đánh dấu từ: " + e.getMessage());
            }
        });
    }

    /** Updates word's SRS data based on review rating. */
    public void updateWordReview(Word word, int userRating) {
        if (word == null) {
            Log.e(TAG, "updateWordReview called with null word.");
            return;
        }
        mExecutorService.execute(() -> {
            try {
                Word updatedWord = SpacedRepetitionScheduler.calculateNextReview(word, userRating);
                mWordDao.updateWord(updatedWord);
                Log.d(TAG, "Updated SRS for '" + updatedWord.getEnglishWord() + "'. Next review: " + updatedWord.getNextReviewTimestamp());
            } catch (Exception e) {
                Log.e(TAG, "Error updating SRS data", e);
                postMessage("Lỗi cập nhật lịch ôn tập: " + e.getMessage());
            }
        });
    }


    // --- Private Helper Methods ---

    /** Calls the translation API using ApiClient. */
    private void callTranslationApiViaClient(List<String> englishWords) {
        // Loading state is already set to true before calling this
        apiClient.translateWordsWithGemini(englishWords, new ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "Translation API Response Received.");
                parseAndSaveTranslations(response, englishWords); // Parse and save
                postLoading(false); // Set loading false after processing
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Translation API Error: " + error.toString());
                handleApiError(error); // Handle specific errors
                updateWordsWithError(englishWords, error); // Mark words as failed
                postLoading(false); // Set loading false on error
            }
        });
    }

    /** Parses API response and saves translations. */
    private void parseAndSaveTranslations(JSONObject response, List<String> originalWords) {
        // (Keep the existing logic from the previous version of this method)
        try {
            JSONArray candidates = response.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject content = candidates.optJSONObject(0).optJSONObject("content");
                JSONArray parts = (content != null) ? content.optJSONArray("parts") : null;
                if (parts != null && parts.length() > 0) {
                    String rawText = parts.optJSONObject(0).optString("text", "");
                    String translationsText = Utils.extractTextFromMarkdownCodeBlock(rawText, "txt");

                    if (translationsText != null) {
                        String[] translations = translationsText.trim().split("\\r?\\n");
                        String noTranslationPlaceholder = getApplication().getString(R.string.no_translation_placeholder);

                        if (translations.length == originalWords.size()) {
                            mExecutorService.execute(() -> {
                                int updatedCount = 0;
                                for (int i = 0; i < originalWords.size(); i++) {
                                    String english = originalWords.get(i);
                                    String vietnamese = translations[i].trim();
                                    if (!vietnamese.isEmpty()) {
                                        mWordDao.updateTranslation(english, vietnamese);
                                        updatedCount++;
                                    } else {
                                        mWordDao.updateTranslation(english, noTranslationPlaceholder + " (API rỗng)");
                                        Log.w(TAG, "API returned empty translation for: " + english);
                                    }
                                }
                                Log.i(TAG, "Updated translations for " + updatedCount + " words.");
                                if (updatedCount > 0) {
                                    postMessage(getApplication().getString(R.string.words_saved_with_translation, updatedCount));
                                } else if (translations.length > 0) {
                                    postMessage("API trả về bản dịch rỗng.");
                                }
                            });
                        } else {
                            Log.e(TAG, "Translation count mismatch: Expected " + originalWords.size() + ", Got " + translations.length);
                            postMessage(getApplication().getString(R.string.api_error_translation_mismatch));
                            updateWordsWithError(originalWords, new VolleyError("Translation count mismatch"));
                        }
                    } else {
                        Log.e(TAG, "Could not extract translations from ```txt block.");
                        postMessage(getApplication().getString(R.string.api_error_no_txt_block));
                        updateWordsWithError(originalWords, new VolleyError("No ```txt block found"));
                    }
                    return;
                }
            }
            Log.e(TAG, "No valid candidates/parts found in translation response.");
            postMessage(getApplication().getString(R.string.api_error_no_content));
            updateWordsWithError(originalWords, new VolleyError("No valid candidates/parts"));

        } catch (Exception e) {
            Log.e(TAG, "Error parsing translation JSON response: ", e);
            postMessage(getApplication().getString(R.string.api_error_parsing));
            updateWordsWithError(originalWords, new VolleyError("JSON parsing error: " + e.getMessage()));
        }
    }

    /** Handles specific API errors and posts messages. */
    private void handleApiError(VolleyError error) {
        // (Keep the existing logic from the previous version of this method)
        String specificErrorMsg;
        if (error instanceof TimeoutError) {
            specificErrorMsg = getApplication().getString(R.string.api_error_timeout);
        } else if (error instanceof ServerError && error.networkResponse != null) {
            specificErrorMsg = "Lỗi máy chủ API dịch (Code: " + error.networkResponse.statusCode + ").";
            if (error.networkResponse.data != null) {
                try {
                    String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    Log.e(TAG, "API Server Error Body: " + errorData);
                    // Try to parse detailed error message from Gemini
                    try {
                        JSONObject errorJson = new JSONObject(errorData);
                        if (errorJson.has("error") && errorJson.getJSONObject("error").has("message")) {
                            specificErrorMsg += "\nChi tiết: " + errorJson.getJSONObject("error").getString("message");
                        }
                    } catch (JSONException jsonEx) {
                        specificErrorMsg += "\n" + errorData; // Show raw data if not JSON
                    }
                } catch (Exception e) { Log.e(TAG, "Error reading API error details", e); }
            }
        } else if (error instanceof NetworkError) {
            specificErrorMsg = "Lỗi kết nối mạng khi dịch.";
        } else if (error instanceof ParseError) {
            specificErrorMsg = getApplication().getString(R.string.api_error_parsing);
        } else if (error.getMessage() != null && error.getMessage().contains("Error creating request body")) {
            specificErrorMsg = "Lỗi nội bộ: Không thể tạo yêu cầu API dịch.";
        } else if (error.getMessage() != null && error.getMessage().contains("Word list is empty")) {
            specificErrorMsg = "Danh sách từ cần dịch rỗng.";
            Log.w(TAG, specificErrorMsg);
            return; // Don't show toast for this expected case
        }
        else {
            specificErrorMsg = getApplication().getString(R.string.api_error_generic);
            if(error.getMessage() != null){
                specificErrorMsg += ": " + error.getMessage();
            }
        }
        postMessage(specificErrorMsg);
    }

    /** Updates words with an error message in their translation field. */
    private void updateWordsWithError(List<String> words, VolleyError error) {
        // (Keep the existing logic from the previous version of this method)
        mExecutorService.execute(() -> {
            String errorMsg = "Lỗi dịch";
            if (error instanceof TimeoutError) errorMsg += ": Timeout";
            else if (error.networkResponse != null) errorMsg += ": Server " + error.networkResponse.statusCode;
            else if (error instanceof NetworkError) errorMsg += ": Network";
            else errorMsg += ": Unknown";

            for (String word : words) {
                mWordDao.updateTranslation(word, errorMsg);
            }
            Log.w(TAG, "Updated " + words.size() + " words with error state: " + errorMsg);
        });
    }

    /** Safely posts a message to the mMessage LiveData. */
    private void postMessage(String message) {
        Log.d(TAG, "Posting message: " + message);
        mMessage.postValue(message);
    }

    /** Safely posts loading state to the mIsLoading LiveData. */
    private void postLoading(boolean isLoading) {
        Log.d(TAG, "Posting loading state: " + isLoading);
        mIsLoading.postValue(isLoading);
    }

    /** Clears the message LiveData. */
    public void clearMessage() {
        mMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "VocabularyViewModel cleared.");
        // Cancel any ongoing operations if necessary
    }
}
