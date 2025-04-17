package com.example.learning_english.activity;

import androidx.appcompat.app.AlertDialog; // Giữ lại nếu cần cho các dialog khác
import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.content.ContextCompat; // Không cần nữa nếu highlight ở Utils
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Typeface; // Giữ lại nếu cần cho quiz
import android.os.Bundle;
// import android.text.Spannable; // Không cần nữa
// import android.text.style.BackgroundColorSpan; // Không cần nữa
import android.util.Log;
// import android.view.GestureDetector; // Không cần nữa
import android.view.MenuItem;
// import android.view.MotionEvent; // Không cần nữa
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Import VolleyError để xử lý lỗi từ ApiClient (vẫn cần cho callGeminiApiViaClient)
import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.NetworkError;
import com.android.volley.ServerError;
import com.android.volley.ParseError;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.learning_english.R;
import com.example.learning_english.databinding.ActivityStoryBinding;
import com.example.learning_english.db.Word;
import com.example.learning_english.network.ApiClient;
import com.example.learning_english.network.ApiResponseListener;
import com.example.learning_english.utils.Utils;
import com.example.learning_english.utils.TranslateUtils; // *** THÊM IMPORT TranslateUtils ***
import com.example.learning_english.viewmodel.VocabularyViewModel;

public class StoryActivity extends AppCompatActivity {

    private static final String TAG = "StoryActivity";
    private ActivityStoryBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private List<Word> reviewWords = null;
    private ApiClient apiClient;

    private static final String QUIZ_SEPARATOR = "\n\n---QUESTIONS---\n\n";
    // --- XÓA CÁC BIẾN LIÊN QUAN ĐẾN HIGHLIGHT VÀ GESTURE ---
    // private BackgroundColorSpan highlightedSpan = null;
    // private GestureDetector gestureDetector;
    // ------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility") // Vẫn cần nếu có các listener khác
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.story_activity_title);
        }

        apiClient = ApiClient.getInstance(this);
        vocabularyViewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);

        vocabularyViewModel.getReviewWords().observe(this, words -> {
            reviewWords = words;
            binding.buttonLearnSelectedWords.setEnabled(reviewWords != null && !reviewWords.isEmpty());
        });

        setupButtons();

        // --- THAY ĐỔI: Gọi hàm tiện ích để thiết lập double-click translate ---
        TranslateUtils.setupDoubleClickTranslate(
                this, // Context
                binding.textviewStoryResultActivity, // TextView mục tiêu
                apiClient // ApiClient instance
        );
        // --- KẾT THÚC THAY ĐỔI ---

        // --- XÓA CODE CŨ LIÊN QUAN ĐẾN LISTENER VÀ GESTURE DETECTOR ---
        // setupDoubleClickDetector();
        // binding.textviewStoryResultActivity.setOnTouchListener(...);
        // -----------------------------------------------------------
    }

    // --- XÓA CÁC HÀM ĐÃ DI CHUYỂN SANG TranslateUtils ---
    // private void setupDoubleClickDetector() { ... }
    // private int findWordStart(String text, int offset) { ... }
    // private int findWordEnd(String text, int offset) { ... }
    // private void removeHighlightIfNeeded() { ... }
    // private void highlightWord(TextView textView, int start, int end) { ... }
    // private void removeHighlight() { ... }
    // private void translateWordWithMyMemoryApiClient(String wordToTranslate) { ... }
    // ---------------------------------------------------

    // --- Các hàm setupButtons, buildPrompt, callGeminiApiViaClient, handleApiError, displayQuiz, addQuestionToLayout, dpToPx, onOptionsItemSelected, showError, onDestroy giữ nguyên ---
    // Lưu ý: hàm `removeHighlight()` trong `callGeminiApiViaClient` và `handleApiError`
    // cần được thay thế bằng cách gọi `TranslateUtils.removeCurrentHighlight()` nếu bạn muốn
    // xóa highlight từ Utils class khi có lỗi API Gemini. Tuy nhiên, việc này có thể không cần thiết.
    // Hiện tại, các hàm này sẽ không còn tác dụng vì `removeHighlight()` đã bị xóa khỏi Activity này.
    // Bạn có thể xóa các lệnh gọi `removeHighlight()` trong các hàm này nếu muốn.

    private void setupButtons() {
        binding.buttonLearnSelectedWords.setOnClickListener(v -> {
            if (reviewWords == null || reviewWords.isEmpty()) {
                Toast.makeText(this, "Không có từ nào trong danh sách ôn tập.", Toast.LENGTH_SHORT).show();
                return;
            }
            String prompt = buildPrompt(reviewWords);
            callGeminiApiViaClient(prompt);
        });

        binding.buttonLearnRandomWords.setOnClickListener(v -> {
            String prompt = buildPrompt(null);
            callGeminiApiViaClient(prompt);
        });
    }

    private String buildPrompt(List<Word> wordsToInclude) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Hãy thực hiện các yêu cầu sau và trả về TOÀN BỘ kết quả bên trong một khối mã ```txt duy nhất:\n\n");
        promptBuilder.append("1. Tạo một câu chuyện song ngữ Anh-Việt theo từng đoạn văn.\n");
        promptBuilder.append("Yêu cầu về độ khó: Từ vựng và ngữ pháp phù hợp cho người đọc có trình độ TOEIC khoảng 600.\n");

        if (wordsToInclude != null && !wordsToInclude.isEmpty()) {
            promptBuilder.append("Câu chuyện PHẢI chứa các từ vựng sau đây (hoặc dạng khác của chúng): ");
            List<String> wordStrings = new ArrayList<>();
            for (Word w : wordsToInclude) {
                wordStrings.add(w.getEnglishWord());
            }
            promptBuilder.append(String.join(", ", wordStrings)).append(".\n");
        } else {
            promptBuilder.append("Chủ đề câu chuyện: Ngẫu nhiên, thú vị và phù hợp để học tiếng Anh.\n");
        }

        promptBuilder.append("\n2. Sau câu chuyện, hãy tạo chính xác 5 câu hỏi trắc nghiệm (A, B, C, D) về nội dung câu chuyện.\n");
        promptBuilder.append("Đánh dấu đáp án đúng bằng cách thêm '==' vào ngay trước lựa chọn đó (ví dụ: ==B. Đáp án đúng).\n");
        promptBuilder.append("Quan trọng: Phân tách rõ ràng giữa phần câu chuyện và phần câu hỏi bằng dấu phân cách duy nhất là: \"").append(QUIZ_SEPARATOR).append("\"\n");
        promptBuilder.append("Định dạng câu chuyện: Tiếng Anh (Tiếng Việt).\n");
        promptBuilder.append("Định dạng câu hỏi: Số thứ tự. Câu hỏi\nA. Lựa chọn A\nB. Lựa chọn B\n==C. Lựa chọn C\nD. Lựa chọn D\n");
        promptBuilder.append("\nHãy nhớ đặt toàn bộ phần truyện và câu hỏi vào trong khối ```txt.");

        return promptBuilder.toString();
    }

    private void callGeminiApiViaClient(String prompt) {
        binding.progressBarLoadingActivity.setVisibility(View.VISIBLE);
        binding.textviewStoryResultActivity.setText(R.string.story_placeholder);
        binding.layoutQuizContainer.removeAllViews();
        binding.dividerQuiz.setVisibility(View.GONE);
        binding.buttonLearnSelectedWords.setEnabled(false);
        binding.buttonLearnRandomWords.setEnabled(false);
        // TranslateUtils.removeCurrentHighlight(); // Có thể gọi nếu muốn xóa highlight khi tạo truyện mới

        apiClient.generateGeminiContent(prompt, new ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                binding.progressBarLoadingActivity.setVisibility(View.GONE);
                binding.buttonLearnSelectedWords.setEnabled(reviewWords != null && !reviewWords.isEmpty());
                binding.buttonLearnRandomWords.setEnabled(true);

                Log.d(TAG, "API Response via ApiClient: " + response.toString());
                try {
                    JSONArray candidates = response.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            String rawText = parts.getJSONObject(0).getString("text");
                            String extractedContent = Utils.extractTextFromMarkdownCodeBlock(rawText, "txt");

                            if (extractedContent != null) {
                                String[] contentParts = extractedContent.split(Pattern.quote(QUIZ_SEPARATOR), 2);
                                String storyText = contentParts[0].trim();
                                String quizText = (contentParts.length > 1) ? contentParts[1].trim() : null;

                                binding.textviewStoryResultActivity.setText(storyText);

                                if (quizText != null && !quizText.isEmpty()) {
                                    binding.dividerQuiz.setVisibility(View.VISIBLE);
                                    displayQuiz(quizText);
                                } else {
                                    Log.w(TAG, "Quiz part not found or empty after extraction.");
                                    binding.layoutQuizContainer.removeAllViews();
                                    binding.dividerQuiz.setVisibility(View.GONE);
                                }
                            } else {
                                Log.w(TAG, "Could not extract content from ```txt block. Displaying raw text.");
                                binding.textviewStoryResultActivity.setText(rawText);
                                binding.layoutQuizContainer.removeAllViews();
                                binding.dividerQuiz.setVisibility(View.GONE);
                                Toast.makeText(StoryActivity.this, "Lỗi: Không thể phân tích định dạng trả về từ API.", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            showError("Không tìm thấy nội dung trong phản hồi API.");
                        }
                    } else {
                        if (response.has("promptFeedback")) {
                            JSONObject promptFeedback = response.getJSONObject("promptFeedback");
                            if (promptFeedback.has("safetyRatings")) {
                                JSONArray safetyRatings = promptFeedback.getJSONArray("safetyRatings");
                                Log.w(TAG, "Prompt Feedback: " + safetyRatings.toString());
                                showError("Yêu cầu bị chặn do bộ lọc an toàn hoặc lý do khác.");
                            } else {
                                showError("Phản hồi API không chứa kết quả mong đợi (không có safetyRatings).");
                            }
                        } else {
                            showError("Phản hồi API không chứa kết quả mong đợi (không có promptFeedback).");
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Lỗi phân tích JSON response từ ApiClient: ", e);
                    showError("Lỗi xử lý phản hồi từ API.");
                }
            }

            @Override
            public void onError(VolleyError error) {
                binding.progressBarLoadingActivity.setVisibility(View.GONE);
                binding.buttonLearnSelectedWords.setEnabled(reviewWords != null && !reviewWords.isEmpty());
                binding.buttonLearnRandomWords.setEnabled(true);
                Log.e(TAG, "Lỗi gọi API qua ApiClient: ", error);
                handleApiError(error);
            }
        });
    }

    private void handleApiError(VolleyError error) {
        String errorMessage = "Lỗi kết nối mạng hoặc lỗi từ API.";

        if (error instanceof TimeoutError) {
            errorMessage = "Lỗi: Yêu cầu API quá thời gian chờ. Vui lòng thử lại.";
        } else if (error instanceof ServerError && error.networkResponse != null) {
            errorMessage = "Lỗi máy chủ API (Code: " + error.networkResponse.statusCode + ").";
            if (error.networkResponse.data != null) {
                try {
                    String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    JSONObject errorJson = new JSONObject(errorData);
                    if (errorJson.has("error") && errorJson.getJSONObject("error").has("message")) {
                        errorMessage += "\nChi tiết: " + errorJson.getJSONObject("error").getString("message");
                    } else {
                        errorMessage += "\n" + errorData;
                    }
                    Log.e(TAG, "API Server Error Body: " + errorData);
                } catch (Exception e) {
                    Log.w(TAG, "Không thể đọc chi tiết lỗi từ API response", e);
                }
            }
        } else if (error instanceof NetworkError) {
            errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra Internet.";
        } else if (error instanceof ParseError) {
            errorMessage = "Lỗi phân tích dữ liệu trả về từ API.";
        } else if (error.getMessage() != null && error.getMessage().contains("Error creating request body")) {
            errorMessage = "Lỗi nội bộ: Không thể tạo yêu cầu API.";
        }

        showError(errorMessage);
        // TranslateUtils.removeCurrentHighlight(); // Có thể gọi nếu muốn xóa highlight khi có lỗi API
    }

    // displayQuiz, addQuestionToLayout, dpToPx giữ nguyên
    private void displayQuiz(String quizText) {
        binding.layoutQuizContainer.removeAllViews();
        Pattern questionPattern = Pattern.compile("^\\d+\\.\\s*(.*)", Pattern.MULTILINE);
        Matcher questionMatcher = questionPattern.matcher(quizText);
        String[] questionsRaw = quizText.split("\\n(?=\\d+\\.)");

        for (String questionBlock : questionsRaw) {
            questionBlock = questionBlock.trim();
            if (questionBlock.isEmpty()) continue;

            String questionText = "";
            List<String> options = new ArrayList<>();
            String correctAnswerText = null;

            String[] lines = questionBlock.split("\\r?\\n");
            if (lines.length > 0) {
                Matcher qMatcher = Pattern.compile("^\\d+\\.\\s*(.*)").matcher(lines[0]);
                if (qMatcher.find()) {
                    questionText = qMatcher.group(1).trim();
                } else {
                    questionText = lines[0].trim();
                }

                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.matches("^(==)?[A-D]\\..*")) {
                        options.add(line);
                        if (line.startsWith("==")) {
                            correctAnswerText = line.replaceFirst("^==[A-D]\\.\\s*", "").trim();
                        }
                    }
                }
            }

            if (!questionText.isEmpty() && options.size() == 4 && correctAnswerText != null) {
                addQuestionToLayout(questionText, options, correctAnswerText);
            } else {
                Log.w(TAG, "Could not parse question block or missing correct answer:\n" + questionBlock);
                Log.w(TAG, "Parsed: question='" + questionText + "', options=" + options.size() + ", correct='" + correctAnswerText + "'");

            }
        }
    }

    private void addQuestionToLayout(String questionText, List<String> options, String correctAnswerText) {
        LinearLayout questionLayout = new LinearLayout(this);
        questionLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(24));
        questionLayout.setLayoutParams(params);

        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionTextView.setTextSize(16);
        questionTextView.setTypeface(questionTextView.getTypeface(), Typeface.BOLD); // Cách lấy Typeface hiện tại
        LinearLayout.LayoutParams qParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qParams.setMargins(0, 0, 0, dpToPx(8));
        questionTextView.setLayoutParams(qParams);
        questionLayout.addView(questionTextView);

        List<Button> optionButtons = new ArrayList<>();
        final String finalCorrectAnswerText = correctAnswerText;

        for (String option : options) {
            Button optionButton = new Button(this, null, androidx.appcompat.R.attr.buttonStyleSmall);
            String buttonText = option.replaceFirst("^(==)?[A-D]\\.\\s*", "").trim();
            boolean isCorrectOption = option.startsWith("==");
            optionButton.setText(option.replaceFirst("^==", ""));
            optionButton.setTag(isCorrectOption);
            optionButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            optionButton.setAllCaps(false);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
            optionButton.setLayoutParams(btnParams);

            optionButton.setOnClickListener(v -> {
                boolean clickedCorrect = (Boolean) v.getTag();
                for (Button btn : optionButtons) {
                    btn.setEnabled(false);
                    boolean isThisBtnCorrect = (Boolean) btn.getTag();

                    if (isThisBtnCorrect) {
                        btn.setBackgroundColor(getResources().getColor(R.color.correct_answer_green, getTheme()));
                        btn.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                    } else if (btn == v && !clickedCorrect) {
                        btn.setBackgroundColor(getResources().getColor(R.color.incorrect_answer_red, getTheme()));
                        btn.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                    } else {
                        btn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
                        btn.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                    }
                }
            });

            optionButtons.add(optionButton);
            questionLayout.addView(optionButton);
        }
        binding.layoutQuizContainer.addView(questionLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    // --------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showError(String message) {
        if (binding == null) return;
        binding.textviewStoryResultActivity.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        binding.progressBarLoadingActivity.setVisibility(View.GONE);
        binding.buttonLearnSelectedWords.setEnabled(reviewWords != null && !reviewWords.isEmpty());
        binding.buttonLearnRandomWords.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
