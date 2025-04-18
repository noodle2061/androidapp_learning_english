package com.example.learning_english.viewmodel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Import VolleyError để xử lý lỗi từ ApiClient
import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.NetworkError;
import com.android.volley.ServerError;
import com.android.volley.ParseError;

import com.example.learning_english.R;
import com.example.learning_english.db.AppDatabase;
import com.example.learning_english.db.Word;
import com.example.learning_english.db.WordDao;
// Import các lớp network mới
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
    private LiveData<List<Word>> mAllWords;
    private LiveData<List<Word>> mReviewWords;
    private ExecutorService mExecutorService;
    // --- XÓA RequestQueue ---
    private ApiClient apiClient; // Thêm ApiClient

    private MutableLiveData<Boolean> mIsLoadingTranslation = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadingTranslation() { return mIsLoadingTranslation; }

    private LiveData<Integer> mDueReviewCount;
    private LiveData<List<Word>> mDueReviewWords;

    // --- XÓA API Info ---

    public VocabularyViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        mWordDao = db.wordDao();
        mAllWords = mWordDao.getAllWords();
        mReviewWords = mWordDao.getManuallyMarkedReviewWords();
        mExecutorService = AppDatabase.databaseWriteExecutor;
        // --- XÓA Khởi tạo RequestQueue ---
        apiClient = ApiClient.getInstance(application); // Khởi tạo ApiClient

        long currentTime = System.currentTimeMillis();
        mDueReviewCount = mWordDao.getDueReviewWordsCount(currentTime);
        mDueReviewWords = mWordDao.getDueReviewWords(currentTime);
    }

    // Getters giữ nguyên
    public LiveData<List<Word>> getAllWords() { return mAllWords; }
    public LiveData<List<Word>> getReviewWords() { return mReviewWords; }
    public LiveData<Integer> getDueReviewCount() { return mDueReviewCount; }
    public LiveData<List<Word>> getDueReviewWords() { return mDueReviewWords; }

    public void addOrUpdateWordsFromString(String inputText) {
        Log.d(TAG, "addOrUpdateWordsFromString called with input: " + inputText);
        mExecutorService.execute(() -> {
            // Phần kiểm tra từ trùng lặp và chuẩn bị danh sách giữ nguyên
            List<Word> existingWords = mWordDao.getAllWordsList();
            Set<String> existingEnglishWords = new HashSet<>();
            Map<String, Word> existingWordMap = new HashMap<>();
            for (Word w : existingWords) {
                String lowerCaseWord = w.englishWord.toLowerCase().trim();
                existingEnglishWords.add(lowerCaseWord);
                existingWordMap.put(lowerCaseWord, w);
            }

            String[] lines = inputText.split("\\r?\\n");
            List<String> wordsToTranslate = new ArrayList<>();
            String noTranslationPlaceholder = getApplication().getString(R.string.no_translation_placeholder);
            List<Word> wordsToInsertImmediately = new ArrayList<>();

            for (String line : lines) {
                String englishWord = line.trim();
                if (!englishWord.isEmpty()) {
                    String lowerCaseWord = englishWord.toLowerCase();
                    if (!existingEnglishWords.contains(lowerCaseWord)) {
                        Word newWord = new Word(englishWord, noTranslationPlaceholder);
                        wordsToInsertImmediately.add(newWord);
                        wordsToTranslate.add(englishWord);
                        existingEnglishWords.add(lowerCaseWord);
                        Log.d(TAG, "New word prepared: " + englishWord);
                    } else {
                        Word existingWord = existingWordMap.get(lowerCaseWord);
                        if (existingWord != null && (existingWord.getVietnameseTranslation() == null ||
                                existingWord.getVietnameseTranslation().isEmpty() ||
                                existingWord.getVietnameseTranslation().startsWith("Lỗi") ||
                                existingWord.getVietnameseTranslation().equals(noTranslationPlaceholder))) {
                            // Chỉ thêm vào danh sách cần dịch nếu bản dịch hiện tại không hợp lệ
                            wordsToTranslate.add(englishWord);
                            Log.d(TAG, "Existing word needs re-translation: " + englishWord);
                        } else {
                            Log.d(TAG, "Word already exists with translation: " + englishWord);
                        }
                    }
                }
            }

            // Insert các từ mới ngay lập tức (nếu có)
            if (!wordsToInsertImmediately.isEmpty()) {
                mWordDao.insertAll(wordsToInsertImmediately);
                Log.i(TAG, "Inserted " + wordsToInsertImmediately.size() + " new words.");
            }

            // Gọi API dịch nếu có từ cần dịch
            if (!wordsToTranslate.isEmpty()) {
                Log.i(TAG, "Found " + wordsToTranslate.size() + " words needing translation. Calling API via ApiClient...");
                // Cập nhật trạng thái loading trên Main Thread
                ContextCompat.getMainExecutor(getApplication()).execute(() -> mIsLoadingTranslation.setValue(true));
                // Gọi API qua ApiClient
                callTranslationApiViaClient(wordsToTranslate);
            } else {
                Log.i(TAG, "No words need API translation call.");
                // Nếu không cần gọi API, đảm bảo trạng thái loading là false
                if (mIsLoadingTranslation.getValue() != null && mIsLoadingTranslation.getValue()) {
                    ContextCompat.getMainExecutor(getApplication()).execute(() -> mIsLoadingTranslation.setValue(false));
                }
            }
        });
    }

    // --- Hàm gọi API dịch qua ApiClient ---
    private void callTranslationApiViaClient(List<String> englishWords) {
        apiClient.translateWordsWithGemini(englishWords, new ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "Translation API Response Received via ApiClient.");
                parseAndSaveTranslations(response, englishWords); // Hàm parse giữ nguyên logic
                // Cập nhật trạng thái loading trên Main Thread
                ContextCompat.getMainExecutor(getApplication()).execute(() -> mIsLoadingTranslation.setValue(false));
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Translation API Error via ApiClient: " + error.toString());
                handleApiError(error); // Gọi hàm xử lý lỗi tập trung
                // Cập nhật trạng thái loading trên Main Thread
                ContextCompat.getMainExecutor(getApplication()).execute(() -> mIsLoadingTranslation.setValue(false));
                // Cập nhật bản dịch thành lỗi cho những từ đang chờ dịch
                updateWordsWithError(englishWords, error);
            }
        });
    }
    // --------------------------------------

    // Hàm parse và lưu bản dịch (Giữ nguyên logic)
    private void parseAndSaveTranslations(JSONObject response, List<String> originalWords) {
        try {
            JSONArray candidates = response.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject content = candidates.optJSONObject(0).optJSONObject("content");
                JSONArray parts = (content != null) ? content.optJSONArray("parts") : null;
                if (parts != null && parts.length() > 0) {
                    String rawText = parts.optJSONObject(0).optString("text", "");
                    // Trích xuất nội dung từ khối ```txt
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
                                    // Chỉ cập nhật nếu bản dịch không rỗng
                                    if (!vietnamese.isEmpty()) {
                                        mWordDao.updateTranslation(english, vietnamese);
                                        updatedCount++;
                                    } else {
                                        // Nếu API trả về rỗng, có thể ghi lại placeholder hoặc lỗi
                                        mWordDao.updateTranslation(english, noTranslationPlaceholder + " (API returned empty)");
                                        Log.w(TAG, "API returned empty translation for: " + english);
                                    }
                                }
                                Log.i(TAG, "Updated translations for " + updatedCount + " words via ApiClient.");
                                if (updatedCount > 0) {
                                    showToastOnMainThread(getApplication().getString(R.string.words_saved));
                                } else if (translations.length > 0) {
                                    // Trường hợp API trả về nhưng tất cả đều rỗng
                                    showToastOnMainThread("API trả về bản dịch rỗng.");
                                }
                            });
                        } else {
                            Log.e(TAG, "Translation count mismatch: Expected " + originalWords.size() + ", Got " + translations.length + " in ```txt block.");
                            showToastOnMainThread(getApplication().getString(R.string.api_error_translation_mismatch));
                            // Cập nhật lỗi cho các từ
                            updateWordsWithError(originalWords, new VolleyError("Translation count mismatch"));
                        }
                    } else {
                        Log.e(TAG, "Could not extract translations from ```txt block in API response.");
                        showToastOnMainThread(getApplication().getString(R.string.api_error_no_txt_block));
                        updateWordsWithError(originalWords, new VolleyError("No ```txt block found"));
                    }
                    return; // Đã xử lý thành công hoặc lỗi parsing
                }
            }
            // Nếu không có candidates hoặc parts hợp lệ
            Log.e(TAG, "No valid candidates/parts found in translation API response.");
            showToastOnMainThread(getApplication().getString(R.string.api_error_no_content));
            updateWordsWithError(originalWords, new VolleyError("No valid candidates/parts"));

        } catch (Exception e) { // Bắt Exception chung để đề phòng lỗi không mong muốn
            Log.e(TAG, "Error parsing translation JSON response: ", e);
            showToastOnMainThread(getApplication().getString(R.string.api_error_parsing));
            updateWordsWithError(originalWords, new VolleyError("JSON parsing error: " + e.getMessage()));
        }
    }
    // --------------------------------------

    // --- Hàm xử lý lỗi API tập trung ---
    private void handleApiError(VolleyError error) {
        String specificErrorMsg;
        if (error instanceof TimeoutError) {
            specificErrorMsg = getApplication().getString(R.string.api_error_timeout);
        } else if (error instanceof ServerError && error.networkResponse != null) {
            specificErrorMsg = "Lỗi máy chủ API dịch (Code: " + error.networkResponse.statusCode + ").";
            // Cố gắng đọc chi tiết lỗi từ response body
            if (error.networkResponse.data != null) {
                try {
                    String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    Log.e(TAG, "API Server Error Body: " + errorData);
                    try {
                        JSONObject errorJson = new JSONObject(errorData);
                        if (errorJson.has("error") && errorJson.getJSONObject("error").has("message")) {
                            specificErrorMsg += "\nChi tiết: " + errorJson.getJSONObject("error").getString("message");
                        }
                    } catch (JSONException jsonEx) {
                        Log.w(TAG, "Error body is not JSON or invalid format.");
                        specificErrorMsg += "\n" + errorData; // Hiển thị raw data nếu không parse được
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading API error details", e);
                }
            }
        } else if (error instanceof NetworkError) {
            specificErrorMsg = "Lỗi kết nối mạng khi dịch.";
        } else if (error instanceof ParseError) {
            specificErrorMsg = getApplication().getString(R.string.api_error_parsing);
        } else if (error.getMessage() != null && error.getMessage().contains("Error creating request body")) {
            specificErrorMsg = "Lỗi nội bộ: Không thể tạo yêu cầu API dịch.";
        } else if (error.getMessage() != null && error.getMessage().contains("Word list is empty")) {
            specificErrorMsg = "Danh sách từ cần dịch rỗng."; // Không cần hiển thị toast này thường xuyên
            Log.w(TAG, specificErrorMsg);
            return; // Không hiển thị toast cho lỗi này
        }
        else {
            specificErrorMsg = getApplication().getString(R.string.api_error_generic);
            if(error.getMessage() != null){
                specificErrorMsg += ": " + error.getMessage();
            }
        }
        showToastOnMainThread(specificErrorMsg);
    }
    // ---------------------------------

    // --- Hàm cập nhật lỗi cho các từ ---
    private void updateWordsWithError(List<String> words, VolleyError error) {
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
    // ---------------------------------

    // Các hàm xử lý từ vựng khác (delete, setReviewMark, updateWordReview) giữ nguyên
    public void deleteSingleWord(Word word) {
        if (word == null) return;
        mExecutorService.execute(() -> {
            mWordDao.deleteWords(List.of(word));
            String message = getApplication().getResources().getQuantityString(R.plurals.words_deleted_snackbar, 1, 1);
            showToastOnMainThread(message);
        });
    }

    public void setWordReviewMark(Word word, boolean isMarked) {
        if (word == null) return;
        mExecutorService.execute(() -> {
            word.setForReview(isMarked);
            mWordDao.updateWord(word);
            String message;
            if (isMarked) {
                message = getApplication().getResources().getQuantityString(R.plurals.words_marked_for_review_snackbar, 1, 1);
            } else {
                message = getApplication().getResources().getQuantityString(R.plurals.words_unmarked_for_review_snackbar, 1, 1);
            }
            showToastOnMainThread(message);
            Log.d(TAG, "Set isForReview mark for '" + word.getEnglishWord() + "' to " + isMarked);
        });
    }

    public void updateWordReview(Word word, int userRating) {
        if (word == null) {
            Log.e(TAG, "updateWordReview called with null word.");
            return;
        }
        mExecutorService.execute(() -> {
            Word updatedWord = SpacedRepetitionScheduler.calculateNextReview(word, userRating);
            mWordDao.updateWord(updatedWord);
            Log.d(TAG, "Updated SRS for '" + updatedWord.getEnglishWord() + "'. Next review: " + updatedWord.getNextReviewTimestamp());
        });
    }
    // ----------------------------------------------------------------------

    // Hàm tiện ích hiển thị Toast (giữ nguyên)
    private void showToastOnMainThread(String message) {
        ContextCompat.getMainExecutor(getApplication()).execute(() ->
                Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        );
    }

    // (Tùy chọn) Hủy các request khi ViewModel bị clear
    @Override
    protected void onCleared() {
        super.onCleared();
        // apiClient.cancelAllRequests(TAG); // Cần set tag phù hợp khi gọi request
        Log.d(TAG, "VocabularyViewModel cleared.");
    }
}
