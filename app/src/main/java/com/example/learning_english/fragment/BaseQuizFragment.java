package com.example.learning_english.fragment;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
// Bỏ import Drawable không cần thiết nữa nếu dùng setBackgroundResource
// import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewbinding.ViewBinding;

import com.android.volley.VolleyError;
import com.example.learning_english.R;
import com.example.learning_english.model.QuizQuestion;
import com.example.learning_english.network.ApiClient;
import com.example.learning_english.network.ApiResponseListener;
import com.example.learning_english.utils.TranslateUtils;
import com.example.learning_english.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseQuizFragment<VB extends ViewBinding> extends Fragment implements View.OnClickListener {

    // --- Abstract Methods ---
    protected abstract VB initializeBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);
    protected abstract String getFragmentTag();
    protected abstract String buildApiPrompt();
    protected abstract int getResultNavigationActionId();
    protected abstract void initializeQuizParameters();

    // --- Constants ---
    protected static final int NUM_QUESTIONS = 5;
    protected static final String EXPLANATION_SEPARATOR = "\n---EXPLANATION---\n";

    // --- Views ---
    protected VB binding;
    protected TextView[] optionTextViews;
    protected Button buttonPrevious;
    protected Button buttonNext;
    protected Button buttonSubmit;
    protected TextView textQuestion;
    protected TextView textExplanation;
    protected ProgressBar progressBarExercise;
    protected LinearLayout layoutQuizContent;
    protected LinearLayout navigationLayout;
    protected TextView textExerciseError;
    // private Drawable defaultOptionBackground; // Không cần lưu trữ nữa

    // --- Data & State ---
    protected List<QuizQuestion> questions = new ArrayList<>();
    protected int currentQuestionIndex = 0;
    protected String practiceTopicName = "Luyện tập";

    // --- Dependencies ---
    protected ApiClient apiClient;
    protected NavController navController;

    // --- Lifecycle ---
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getFragmentTag(), "onCreate called");
        if (getContext() != null) {
            apiClient = ApiClient.getInstance(requireContext());
            // Không cần lấy drawable mặc định ở đây nữa
        } else {
            Log.e(getFragmentTag(), "Context is null in onCreate!");
        }
        initializeQuizParameters();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(getFragmentTag(), "onCreateView called");
        binding = initializeBinding(inflater, container);
        setupCommonUI();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(getFragmentTag(), "onViewCreated called");
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(getFragmentTag(), "NavController not found for this fragment", e);
        }
        setupTranslation();
        if (questions.isEmpty()) {
            loadAllQuizQuestions();
        } else {
            Log.d(getFragmentTag(), "Questions already loaded.");
            displayCurrentQuestion();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(getFragmentTag(), "onDestroyView called");
        binding = null;
    }

    // --- UI Setup ---
    protected void setupCommonUI() {
        // Giữ nguyên logic setupCommonUI
        try {
            View root = binding.getRoot();
            optionTextViews = new TextView[]{
                    root.findViewById(R.id.textview_option_a),
                    root.findViewById(R.id.textview_option_b),
                    root.findViewById(R.id.textview_option_c),
                    root.findViewById(R.id.textview_option_d)
            };
            buttonPrevious = root.findViewById(R.id.button_previous);
            buttonNext = root.findViewById(R.id.button_next);
            buttonSubmit = root.findViewById(R.id.button_submit);
            textQuestion = root.findViewById(R.id.text_question);
            textExplanation = root.findViewById(R.id.text_explanation);
            progressBarExercise = root.findViewById(R.id.progress_bar_exercise);
            layoutQuizContent = root.findViewById(R.id.layout_quiz_content);
            navigationLayout = root.findViewById(R.id.navigation_layout);
            textExerciseError = root.findViewById(R.id.text_exercise_error);

            for (TextView tv : optionTextViews) {
                if (tv != null) tv.setOnClickListener(this);
                else Log.e(getFragmentTag(), "Option TextView is null during setup");
            }
            if (buttonPrevious != null) buttonPrevious.setOnClickListener(this); else Log.e(getFragmentTag(), "buttonPrevious is null");
            if (buttonNext != null) buttonNext.setOnClickListener(this); else Log.e(getFragmentTag(), "buttonNext is null");
            if (buttonSubmit != null) buttonSubmit.setOnClickListener(this); else Log.e(getFragmentTag(), "buttonSubmit is null");

            if (textExplanation != null) {
                textExplanation.setMovementMethod(new ScrollingMovementMethod());
            } else {
                Log.e(getFragmentTag(), "textExplanation is null during setup");
            }

        } catch (Exception e) {
            Log.e(getFragmentTag(), "Error setting up common UI elements. Check layout IDs.", e);
        }
    }

    protected void setupTranslation() {
        // Giữ nguyên logic setupTranslation
        if (apiClient != null && binding != null && getContext() != null) {
            try {
                if (textQuestion != null) {
                    TranslateUtils.setupDoubleClickTranslate(requireContext(), textQuestion, apiClient);
                } else {
                    Log.e(getFragmentTag(), "textQuestion view not found for translation setup.");
                }
                if (optionTextViews != null) {
                    for (TextView optionTv : optionTextViews) {
                        if (optionTv != null) {
                            TranslateUtils.setupDoubleClickTranslate(requireContext(), optionTv, apiClient);
                        }
                    }
                }
                if (textExplanation != null) {
                    TranslateUtils.setupDoubleClickTranslate(requireContext(), textExplanation, apiClient);
                }

            } catch (Exception e) {
                Log.e(getFragmentTag(), "Error setting up translation.", e);
            }
        } else {
            Log.e(getFragmentTag(), "Cannot setup translation: ApiClient, Binding or Context is null.");
        }
    }

    // --- Quiz Logic ---

    protected void loadAllQuizQuestions() {
        // Giữ nguyên logic loadAllQuizQuestions
        Log.i(getFragmentTag(), "Attempting to fetch all " + NUM_QUESTIONS + " questions.");
        showLoading(true);
        String prompt = buildApiPrompt();
        Log.d(getFragmentTag(), "Calling API with prompt: " + prompt);
        if (apiClient == null) { Log.e(getFragmentTag(), "ApiClient is null"); showError("Lỗi kết nối."); return; }

        apiClient.generateGeminiContent(prompt, new ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(getFragmentTag(), "API Response Raw Received: " + response.toString());
                final List<QuizQuestion> parsedQuestions = parseMultipleQuestionsResponseWithExplanation(response);
                questions = parsedQuestions;
                if (getView() != null) {
                    getView().post(() -> {
                        if (!questions.isEmpty()) {
                            currentQuestionIndex = 0;
                            displayCurrentQuestion();
                            showLoading(false);
                            if(layoutQuizContent != null) layoutQuizContent.setVisibility(View.VISIBLE);
                        } else { Log.e(getFragmentTag(), "Parsed question list is empty."); showError("Lỗi phân tích câu hỏi từ API."); }
                    });
                }
            }
            @Override
            public void onError(VolleyError error) {
                Log.e(getFragmentTag(), "API Error fetching questions: ", error);
                if (getView() != null) { getView().post(() -> showError("Lỗi API khi tải câu hỏi.")); }
            }
        });
    }

    protected List<QuizQuestion> parseMultipleQuestionsResponseWithExplanation(JSONObject response) {
        // Giữ nguyên logic parseMultipleQuestionsResponseWithExplanation
        List<QuizQuestion> localQuestions = new ArrayList<>();
        String extractedContent = null;
        try {
            JSONArray candidates = response.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject content = candidates.optJSONObject(0).optJSONObject("content");
                JSONArray parts = (content != null) ? content.optJSONArray("parts") : null;
                if (parts != null && parts.length() > 0) {
                    String rawText = parts.optJSONObject(0).optString("text", "");
                    extractedContent = Utils.extractTextFromMarkdownCodeBlock(rawText, "txt");

                    if (extractedContent != null) {
                        localQuestions = parseQuestionsFromStringWithExplanation(extractedContent);
                    } else {
                        Log.w(getFragmentTag(), "Could not extract content from ```txt block. Parsing raw text.");
                        localQuestions = parseQuestionsFromStringWithExplanation(rawText);
                        if (localQuestions.isEmpty()){ Log.e(getFragmentTag(), "Failed to parse questions from raw text either."); }
                    }
                } else { Log.e(getFragmentTag(), "No 'parts' found."); }
            } else {
                Log.e(getFragmentTag(), "No 'candidates' found.");
                JSONObject promptFeedback = response.optJSONObject("promptFeedback");
                if (promptFeedback != null && promptFeedback.has("blockReason")) {
                    String reason = promptFeedback.optString("blockReason", "Unknown");
                    Log.e(getFragmentTag(), "API request blocked. Reason: " + reason);
                    if (getView() != null) { getView().post(() -> showError("Yêu cầu bị chặn: " + reason)); }
                }
            }
        } catch (Exception e) { Log.e(getFragmentTag(), "Error processing content", e); }
        Log.i(getFragmentTag(), "Parsing finished. Questions parsed: " + localQuestions.size());
        return localQuestions;
    }

    protected List<QuizQuestion> parseQuestionsFromStringWithExplanation(String text) {
        // Giữ nguyên logic parseQuestionsFromStringWithExplanation
        List<QuizQuestion> parsedList = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) { return parsedList; }
        Pattern blockPattern = Pattern.compile(
                "^\\d+\\.\\s*(.*?)\\n^(?:==)?A\\.\\s*(.*?)\\n^(?:==)?B\\.\\s*(.*?)\\n^(?:==)?C\\.\\s*(.*?)\\n^(?:==)?D\\.\\s*(.*?)\\n" + Pattern.quote(EXPLANATION_SEPARATOR.trim()) + "(.*)",
                Pattern.DOTALL | Pattern.MULTILINE
        );
        Pattern correctAnswerPattern = Pattern.compile("^==([A-D])\\.", Pattern.MULTILINE);
        String[] potentialBlocks = text.trim().split("\\n\\s*\\n(?=\\d+\\.)");

        for (String block : potentialBlocks) {
            block = block.trim(); if (block.isEmpty()) continue;
            Matcher blockMatcher = blockPattern.matcher(block);
            if (blockMatcher.find()) {
                String questionText = blockMatcher.group(1).trim();
                String optionAText = blockMatcher.group(2).trim();
                String optionBText = blockMatcher.group(3).trim();
                String optionCText = blockMatcher.group(4).trim();
                String optionDText = blockMatcher.group(5).trim();
                String rawExplanation = blockMatcher.group(6);
                String explanation = (rawExplanation != null) ? rawExplanation.trim() : "";
                List<String> options = Arrays.asList("A. " + optionAText, "B. " + optionBText, "C. " + optionCText, "D. " + optionDText);
                Matcher correctMatcher = correctAnswerPattern.matcher(block);
                String correctAnswerLetter = null;
                if (correctMatcher.find()) { correctAnswerLetter = correctMatcher.group(1); }
                if (correctAnswerLetter != null) {
                    parsedList.add(new QuizQuestion(questionText, options, correctAnswerLetter, explanation));
                } else { Log.e(getFragmentTag(), "Could not find correct answer marker in block: \n" + block); }
            } else { Log.w(getFragmentTag(), "Block did not match pattern: \n" + block); }
            if (parsedList.size() >= NUM_QUESTIONS) { break; }
        }
        return parsedList;
    }

    protected void displayCurrentQuestion() {
        Log.d(getFragmentTag(), "displayCurrentQuestion: index=" + currentQuestionIndex);
        if (binding == null || getContext() == null) return;

        TextView textQuestionProgress = binding.getRoot().findViewById(R.id.text_question_progress);

        if (textQuestionProgress == null || textQuestion == null || textExplanation == null || optionTextViews == null) {
            Log.e(getFragmentTag(), "Required views are null in displayCurrentQuestion.");
            showError("Lỗi giao diện."); return;
        }

        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            Log.e(getFragmentTag(), "Invalid index: " + currentQuestionIndex);
            updateNavigationButtonsState();
            if (questions.isEmpty()) showError("Danh sách câu hỏi rỗng.");
            else showError("Lỗi hiển thị câu hỏi.");
            return;
        }

        QuizQuestion currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion == null) { Log.e(getFragmentTag(), "Question object is null"); showError("Lỗi tải dữ liệu câu hỏi."); return; }

        textQuestionProgress.setText(getString(R.string.question_progress_format, currentQuestionIndex + 1, questions.size()));
        textQuestion.setText(currentQuestion.getQuestionText() != null ? currentQuestion.getQuestionText() : "");
        List<String> options = currentQuestion.getOptions();


        if (currentQuestion.isAnswered()) {
            Log.d(getFragmentTag(), "Restoring answered state");
            int userAnswerIndex = currentQuestion.getUserAnswerIndex();
            String correctAnswerLetter = currentQuestion.getCorrectAnswer();
            String explanation = currentQuestion.getExplanation();

            if (explanation != null && !explanation.isEmpty()) {
                textExplanation.setText(getString(R.string.explanation_prefix) + explanation);
                textExplanation.setVisibility(View.VISIBLE);
                textExplanation.scrollTo(0, 0);
            } else { textExplanation.setVisibility(View.GONE); }

            if (options != null && options.size() == 4) {
                for (int i = 0; i < optionTextViews.length; i++) {
                    TextView tv = optionTextViews[i];
                    if (tv != null) {
                        tv.setText(options.get(i) != null ? options.get(i) : "Lỗi");
                        tv.setVisibility(View.VISIBLE);
                        String optionLetter = String.valueOf((char)('A' + i));

                        // *** THAY ĐỔI: Sử dụng setBackgroundResource ***
                        int backgroundResId;
                        int textColor;
                        if (optionLetter.equalsIgnoreCase(correctAnswerLetter)) {
                            backgroundResId = R.drawable.quiz_option_background_correct;
                            textColor = Color.WHITE;
                        } else if (i == userAnswerIndex) {
                            backgroundResId = R.drawable.quiz_option_background_incorrect;
                            textColor = Color.WHITE;
                        } else {
                            backgroundResId = R.drawable.quiz_option_background_disabled;
                            boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                            textColor = isNightMode ? Color.WHITE : Color.DKGRAY; // Hoặc màu khác cho text disabled
                        }
                        tv.setBackgroundResource(backgroundResId);
                        tv.setTextColor(textColor);
                        // Giữ lại clickable và enabled để dịch
                        // tv.setEnabled(false);
                        // tv.setClickable(false);
                    }
                }
            } else { Log.e(getFragmentTag(), "Error restoring options state."); }
        } else {
            Log.d(getFragmentTag(), "Resetting state for unanswered question");
            textExplanation.setVisibility(View.GONE);
            textExplanation.setText("");
            if (options != null && options.size() == 4) {
                for (int i = 0; i < optionTextViews.length; i++) {
                    TextView tv = optionTextViews[i];
                    if (tv != null) {
                        tv.setText(options.get(i) != null ? options.get(i) : "Lỗi");
                        tv.setEnabled(true);
                        tv.setClickable(true);
                        tv.setVisibility(View.VISIBLE);
                        // *** THAY ĐỔI: Reset background về selector mặc định ***
                        tv.setBackgroundResource(R.drawable.quiz_option_background_selector);
                        tv.setTextColor(getTextColorFromAttr(android.R.attr.textColorPrimary));
                    }
                }
            } else { Log.e(getFragmentTag(), "Error resetting options state."); }
        }
        updateNavigationButtonsState();
        Log.d(getFragmentTag(), "Finished displaying question");
    }

    protected void updateNavigationButtonsState() {
        // Giữ nguyên
        if (buttonPrevious != null) buttonPrevious.setEnabled(currentQuestionIndex > 0);
        if (buttonNext != null) buttonNext.setEnabled(currentQuestionIndex < questions.size() - 1);
        if (buttonSubmit != null) buttonSubmit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(getFragmentTag(), "onClick detected: view ID=" + id + ", Index=" + currentQuestionIndex);

        if (id == R.id.button_previous) { if (currentQuestionIndex > 0) { currentQuestionIndex--; displayCurrentQuestion(); } return; }
        if (id == R.id.button_next) { if (currentQuestionIndex < questions.size() - 1) { currentQuestionIndex++; displayCurrentQuestion(); } return; }
        if (id == R.id.button_submit) { showSubmitConfirmationDialog(); return; }

        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) { Log.e(getFragmentTag(), "onClick answer ignored: Invalid index"); return; }
        if (binding == null || optionTextViews == null || getContext() == null) { Log.e(getFragmentTag(), "onClick answer ignored: Binding, options or context null"); return; }
        QuizQuestion currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion == null || currentQuestion.isAnswered()) { Log.w(getFragmentTag(), "Question null or already answered."); return; }

        int clickedIndex = -1;
        if (id == R.id.textview_option_a) clickedIndex = 0;
        else if (id == R.id.textview_option_b) clickedIndex = 1;
        else if (id == R.id.textview_option_c) clickedIndex = 2;
        else if (id == R.id.textview_option_d) clickedIndex = 3;

        if (clickedIndex == -1) { Log.e(getFragmentTag(), "Could not determine clicked answer TextView."); return; }

        String selectedAnswerLetter = String.valueOf((char)('A' + clickedIndex));
        Log.d(getFragmentTag(), "User clicked option: " + selectedAnswerLetter);
        currentQuestion.setAnsweredState(clickedIndex);

        String correctAnswerLetter = currentQuestion.getCorrectAnswer();
        boolean isCorrect = selectedAnswerLetter.equalsIgnoreCase(correctAnswerLetter);
        Log.d(getFragmentTag(), "Correct: " + correctAnswerLetter + ". User correct: " + isCorrect);

        if (textExplanation != null) {
            String explanation = currentQuestion.getExplanation();
            if (explanation != null && !explanation.isEmpty()) {
                textExplanation.setText(getString(R.string.explanation_prefix) + explanation);
                textExplanation.setVisibility(View.VISIBLE);
                textExplanation.scrollTo(0, 0);
            } else { textExplanation.setVisibility(View.GONE); }
        }

        for (int i = 0; i < optionTextViews.length; i++) {
            TextView tv = optionTextViews[i]; if (tv == null) continue;
            // Giữ nguyên clickable và enabled
            String optionLetter = String.valueOf((char)('A' + i));
            // *** THAY ĐỔI: Sử dụng setBackgroundResource ***
            int backgroundResId;
            int textColor;
            boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (optionLetter.equalsIgnoreCase(correctAnswerLetter)) {
                backgroundResId = R.drawable.quiz_option_background_correct;
                textColor = Color.WHITE;
            } else if (i == clickedIndex) {
                backgroundResId = R.drawable.quiz_option_background_incorrect;
                textColor = Color.WHITE;
            } else {
                backgroundResId = R.drawable.quiz_option_background_disabled;
                textColor = isNightMode ? Color.WHITE : Color.DKGRAY;
            }
            tv.setBackgroundResource(backgroundResId);
            tv.setTextColor(textColor);
        }
    }

    protected void showSubmitConfirmationDialog() {
        // Giữ nguyên
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_submission_title)
                .setMessage(R.string.confirm_submission_message)
                .setPositiveButton(R.string.submit, (dialog, which) -> { Log.d(getFragmentTag(), "Submit confirmed."); calculateAndNavigateToResult(); })
                .setNegativeButton(R.string.cancel, (dialog, which) -> { Log.d(getFragmentTag(), "Submit cancelled."); dialog.dismiss(); })
                .show();
    }

    protected void calculateAndNavigateToResult() {
        // Giữ nguyên
        int score = 0; int totalQuestions = questions.size();
        if (totalQuestions > 0) {
            for (QuizQuestion q : questions) {
                if (q.isAnswered()) {
                    int userAnswerIndex = q.getUserAnswerIndex(); String correctAnswerLetter = q.getCorrectAnswer(); int correctAnswerIndex = -1;
                    if (correctAnswerLetter != null) {
                        switch (correctAnswerLetter.toUpperCase()) {
                            case "A": correctAnswerIndex = 0; break; case "B": correctAnswerIndex = 1; break;
                            case "C": correctAnswerIndex = 2; break; case "D": correctAnswerIndex = 3; break;
                        }
                    }
                    if (userAnswerIndex != -1 && userAnswerIndex == correctAnswerIndex) { score++; }
                }
            }
        }
        Log.d(getFragmentTag(), "Calculated score: " + score + "/" + totalQuestions);
        Bundle args = new Bundle(); args.putInt("score", score); args.putInt("totalQuestions", totalQuestions); args.putString("practiceTopicName", practiceTopicName); args.putString("originFragment", this.getClass().getName());
        if (navController != null) {
            try { int actionId = getResultNavigationActionId(); navController.navigate(actionId, args); Log.d(getFragmentTag(), "Navigating to ResultFragment with action ID: " + actionId); }
            catch (IllegalArgumentException e) { Log.e(getFragmentTag(), "Navigation to ResultFragment failed.", e); if (getContext() != null) Toast.makeText(getContext(), R.string.error_navigating_to_result, Toast.LENGTH_SHORT).show(); }
        } else { Log.e(getFragmentTag(), "NavController is null."); if (getContext() != null) Toast.makeText(getContext(), R.string.error_navigating, Toast.LENGTH_SHORT).show(); }
    }

    // --- Utility Methods ---
    protected void showLoading(boolean isLoading) {
        // Giữ nguyên
        if (binding == null) return;
        if (getView() != null) { getView().post(() -> showLoadingInternal(isLoading)); }
        else if (getActivity() != null) { getActivity().runOnUiThread(() -> showLoadingInternal(isLoading)); }
    }
    private void showLoadingInternal(boolean isLoading) {
        // Giữ nguyên
        if (binding == null) return;
        try {
            if (progressBarExercise != null) progressBarExercise.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            boolean isErrorVisible = (textExerciseError != null && textExerciseError.getVisibility() == View.VISIBLE);
            if (layoutQuizContent != null) layoutQuizContent.setVisibility(isLoading || isErrorVisible ? View.INVISIBLE : View.VISIBLE);
            if (navigationLayout != null) navigationLayout.setVisibility(isLoading || isErrorVisible ? View.INVISIBLE : View.VISIBLE);
            if (isLoading && textExerciseError != null) { textExerciseError.setVisibility(View.GONE); }
        } catch (Exception e) { Log.e(getFragmentTag(), "Error updating loading state UI", e); }
    }
    protected void showError(String message) {
        // Giữ nguyên
        if (binding == null) return;
        if (getView() != null) { getView().post(() -> showErrorInternal(message)); }
        else if (getActivity() != null) { getActivity().runOnUiThread(() -> showErrorInternal(message)); }
    }
    private void showErrorInternal(String message) {
        // Giữ nguyên
        Log.e(getFragmentTag(), "showErrorInternal: " + message);
        if (binding == null) return;
        try {
            showLoading(false);
            if (layoutQuizContent != null) layoutQuizContent.setVisibility(View.INVISIBLE);
            if (navigationLayout != null) navigationLayout.setVisibility(View.GONE);
            if (textExerciseError != null) { textExerciseError.setText(message); textExerciseError.setVisibility(View.VISIBLE); }
            if(textExplanation != null) textExplanation.setVisibility(View.GONE);
        } catch (Exception e) { Log.e(getFragmentTag(), "Error updating error state UI", e); }
    }
    protected int getTextColorFromAttr(int attr) {
        // Giữ nguyên
        android.util.TypedValue typedValue = new android.util.TypedValue();
        try {
            if (getContext() != null && requireContext().getTheme() != null) {
                requireContext().getTheme().resolveAttribute(attr, typedValue, true);
                if (typedValue.type >= android.util.TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= android.util.TypedValue.TYPE_LAST_COLOR_INT) {
                    return typedValue.data;
                } else if (getContext() != null) {
                    return ContextCompat.getColor(requireContext(), typedValue.resourceId);
                }
            }
        } catch (Exception e) { Log.e(getFragmentTag(), "Error resolving theme attribute color", e); }
        Log.w(getFragmentTag(), "Failed to resolve text color, returning fallback black.");
        return Color.BLACK;
    }
}
    