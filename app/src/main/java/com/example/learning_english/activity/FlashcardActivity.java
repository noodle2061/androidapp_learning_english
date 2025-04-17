package com.example.learning_english.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_english.R;
import com.example.learning_english.databinding.ActivityFlashcardBinding;
import com.example.learning_english.db.Word;
import com.example.learning_english.utils.SpacedRepetitionScheduler;
import com.example.learning_english.viewmodel.VocabularyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardActivity";
    private ActivityFlashcardBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private List<Word> reviewWordList = new ArrayList<>();
    private int currentWordIndex = 0;
    private int totalWordsInSession = 0;
    private boolean isShowingFront = true;
    private boolean sessionActive = false;

    // Animators
    private AnimatorSet frontAnim;
    private AnimatorSet backAnim;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlashcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.flashcard_activity_title);
        }

        // ViewModel
        vocabularyViewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);

        // Load animations
        loadAnimations();
        scale = getResources().getDisplayMetrics().density;
        binding.cardviewFlashcard.setCameraDistance(8000 * scale);
        binding.textviewEnglishWord.setCameraDistance(8000 * scale);
        binding.textviewVietnameseTranslation.setCameraDistance(8000 * scale);

        // Quan sát danh sách từ cần ôn tập
        vocabularyViewModel.getDueReviewWords().observe(this, words -> {
            if (!sessionActive) {
                if (words != null && !words.isEmpty()) {
                    Log.d(TAG, "Starting review session with " + words.size() + " words.");
                    reviewWordList = new ArrayList<>(words);
                    totalWordsInSession = reviewWordList.size();
                    currentWordIndex = 0;
                    isShowingFront = true;
                    sessionActive = true;
                    binding.textviewNoWords.setVisibility(View.GONE);
                    binding.cardviewFlashcard.setVisibility(View.VISIBLE);
                    binding.layoutSrsButtons.setVisibility(View.GONE);
                    binding.textviewCardsRemaining.setVisibility(View.VISIBLE);
                    binding.textviewEnglishWord.setVisibility(View.VISIBLE);
                    binding.textviewVietnameseTranslation.setVisibility(View.VISIBLE);
                    showCurrentWord();
                } else {
                    Log.d(TAG, "No words due for review.");
                    reviewWordList.clear();
                    totalWordsInSession = 0;
                    currentWordIndex = 0;
                    sessionActive = false;
                    showNoWordsMessage();
                }
            } else {
                Log.d(TAG, "Review session already active. Ignoring LiveData update for now.");
            }
        });

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void loadAnimations() {
        try {
            frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flashcard_flip_front_out);
            backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flashcard_flip_back_in);
        } catch (Exception e) {
            Log.e(TAG, "Error loading animations", e);
            Toast.makeText(this, "Lỗi load hiệu ứng lật thẻ", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Click vào thẻ để lật (CẬP NHẬT)
        binding.cardviewFlashcard.setOnClickListener(v -> {
            // Chỉ lật khi phiên đang hoạt động và animation đã load xong
            if (sessionActive && frontAnim != null && backAnim != null) {
                flipCard(); // Gọi hàm flipCard đã được cập nhật
            }
        });

        // Click vào các nút đánh giá SRS (Giữ nguyên)
        binding.buttonSrsAgain.setOnClickListener(v -> processRating(SpacedRepetitionScheduler.RATING_AGAIN));
        binding.buttonSrsHard.setOnClickListener(v -> processRating(SpacedRepetitionScheduler.RATING_HARD));
        binding.buttonSrsGood.setOnClickListener(v -> processRating(SpacedRepetitionScheduler.RATING_GOOD));
        binding.buttonSrsEasy.setOnClickListener(v -> processRating(SpacedRepetitionScheduler.RATING_EASY));
    }

    private void processRating(int rating) {
        if (!sessionActive || currentWordIndex >= reviewWordList.size()) return;

        Word currentWord = reviewWordList.get(currentWordIndex);
        Log.d(TAG, "Processing rating " + rating + " for word: " + currentWord.getEnglishWord());
        vocabularyViewModel.updateWordReview(currentWord, rating);
        showNextWord();
    }


    private void showNoWordsMessage() {
        binding.textviewNoWords.setText(R.string.no_words_to_review);
        binding.textviewNoWords.setVisibility(View.VISIBLE);
        binding.cardviewFlashcard.setVisibility(View.GONE);
        binding.layoutSrsButtons.setVisibility(View.GONE);
        binding.textviewCardsRemaining.setVisibility(View.GONE);
        sessionActive = false;
    }

    private void showCurrentWord() {
        if (!sessionActive || currentWordIndex >= reviewWordList.size()) {
            Log.d(TAG, "Review session complete or list empty.");
            binding.textviewNoWords.setText(R.string.review_complete);
            binding.textviewNoWords.setVisibility(View.VISIBLE);
            binding.cardviewFlashcard.setVisibility(View.GONE);
            binding.layoutSrsButtons.setVisibility(View.GONE);
            binding.textviewCardsRemaining.setVisibility(View.GONE);
            sessionActive = false;
            reviewWordList.clear();
            return;
        }

        Word currentWord = reviewWordList.get(currentWordIndex);
        binding.textviewEnglishWord.setText(currentWord.getEnglishWord());
        String translation = currentWord.getVietnameseTranslation();
        if (translation == null || translation.isEmpty() || translation.startsWith("Lỗi")) {
            translation = getString(R.string.no_translation_placeholder);
        }
        binding.textviewVietnameseTranslation.setText(translation);

        binding.textviewCardsRemaining.setText(String.format(Locale.getDefault(),"Còn lại: %d/%d", (totalWordsInSession - currentWordIndex), totalWordsInSession));

        // Reset về mặt trước, đảm bảo cả 2 view VISIBLE, dùng alpha để ẩn/hiện
        flipCardInstantly(true);
        binding.layoutSrsButtons.setVisibility(View.GONE); // Luôn ẩn nút SRS khi hiện từ mới
        binding.textviewFlipHint.setVisibility(View.VISIBLE); // Luôn hiện gợi ý lật khi hiện từ mới
        binding.cardviewFlashcard.setClickable(true); // Luôn cho phép nhấn thẻ khi hiện từ mới
    }

    // Hàm lật thẻ (ĐÃ CẬP NHẬT để xử lý 2 chiều)
    private void flipCard() {
        // Đảm bảo cả hai view đều VISIBLE trước khi bắt đầu animation
        binding.textviewEnglishWord.setVisibility(View.VISIBLE);
        binding.textviewVietnameseTranslation.setVisibility(View.VISIBLE);

        if (isShowingFront) {
            // --- Lật từ trước ra sau ---
            frontAnim.setTarget(binding.textviewEnglishWord);
            backAnim.setTarget(binding.textviewVietnameseTranslation);
            frontAnim.start();
            backAnim.start();
            isShowingFront = false;
            binding.layoutSrsButtons.setVisibility(View.VISIBLE); // Hiện nút đánh giá
            binding.textviewFlipHint.setVisibility(View.GONE); // Ẩn gợi ý
            // binding.cardviewFlashcard.setClickable(false); // Bỏ dòng này để cho phép nhấn lật lại
        } else {
            // --- Lật từ sau về trước ---
            // Dùng animation ngược lại
            frontAnim.setTarget(binding.textviewVietnameseTranslation); // Target là view đang hiện (mặt sau)
            backAnim.setTarget(binding.textviewEnglishWord);           // Target là view sẽ hiện (mặt trước)
            frontAnim.start();
            backAnim.start();
            isShowingFront = true;
            binding.layoutSrsButtons.setVisibility(View.GONE); // Ẩn nút đánh giá
            binding.textviewFlipHint.setVisibility(View.VISIBLE); // Hiện lại gợi ý
        }
        // Giữ cho thẻ luôn clickable
        binding.cardviewFlashcard.setClickable(true);
    }

    // Lật thẻ ngay lập tức (Giữ nguyên như lần sửa trước)
    private void flipCardInstantly(boolean showFront) {
        binding.textviewEnglishWord.setRotationY(0);
        binding.textviewVietnameseTranslation.setRotationY(0);
        binding.textviewEnglishWord.setVisibility(View.VISIBLE);
        binding.textviewVietnameseTranslation.setVisibility(View.VISIBLE);

        if (showFront) {
            binding.textviewEnglishWord.setAlpha(1f);
            binding.textviewVietnameseTranslation.setAlpha(0f);
            isShowingFront = true;
        } else {
            binding.textviewEnglishWord.setAlpha(0f);
            binding.textviewVietnameseTranslation.setAlpha(1f);
            isShowingFront = false;
        }
    }


    // Chuyển đến từ tiếp theo
    private void showNextWord() {
        currentWordIndex++;
        showCurrentWord();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
