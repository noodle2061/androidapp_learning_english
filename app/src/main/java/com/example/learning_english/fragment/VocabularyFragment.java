package com.example.learning_english.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// Import ProgressBar nếu bạn dùng

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_english.MainApplication;
import com.example.learning_english.R;
import com.example.learning_english.activity.FlashcardActivity;
import com.example.learning_english.activity.StoryActivity;
import com.example.learning_english.activity.VocabularyListActivity;
import com.example.learning_english.databinding.DialogAddWordsBinding;
import com.example.learning_english.databinding.FragmentVocabularyBinding;
import com.example.learning_english.viewmodel.VocabularyViewModel;

public class VocabularyFragment extends Fragment {

    private FragmentVocabularyBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private AlertDialog addWordsDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        vocabularyViewModel = new ViewModelProvider(requireActivity()).get(VocabularyViewModel.class); // Dùng requireActivity() để lấy ViewModel chung

        // --- THIẾT LẬP SỰ KIỆN CLICK ---
        binding.buttonAddNewWord.setOnClickListener(v -> showAddWordsDialog());
        binding.buttonViewWordList.setOnClickListener(v -> openVocabularyList());
        binding.buttonCreateStory.setOnClickListener(v -> openCreateStory());
        binding.buttonToggleTheme.setOnClickListener(v -> toggleDayNightMode());
        binding.buttonFlashcards.setOnClickListener(v -> openFlashcards());
        // -------------------------------

        // --- QUAN SÁT SỐ TỪ CẦN ÔN TẬP ---
        vocabularyViewModel.getDueReviewCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                if (count > 0) {
                    // Sử dụng plurals resource để hiển thị đúng ngữ pháp
                    String reviewText = getResources().getQuantityString(R.plurals.words_to_review_count, count, count);
                    binding.textviewReviewCount.setText(reviewText);
                    binding.textviewReviewCount.setVisibility(View.VISIBLE);
                    // Có thể thay đổi text của nút Flashcard
                    // binding.buttonFlashcards.setText(getString(R.string.start_review_session) + " (" + count + ")");
                } else {
                    binding.textviewReviewCount.setText(R.string.words_to_review_zero);
                    binding.textviewReviewCount.setVisibility(View.VISIBLE); // Vẫn hiển thị thông báo không có từ
                    // binding.buttonFlashcards.setText(R.string.flashcards); // Reset text nút
                }
            } else {
                binding.textviewReviewCount.setVisibility(View.GONE); // Ẩn nếu count là null
            }
        });
        // ---------------------------------

        return root;
    }

    // Hàm chuyển đổi chế độ Ngày/Đêm (Giữ nguyên)
    private void toggleDayNightMode() {
        // ... (logic như cũ) ...
        Context context = getActivity();
        if (context == null) return;

        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            int currentUiMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (currentUiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                currentNightMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                currentNightMode = AppCompatDelegate.MODE_NIGHT_NO;
            }
        }

        int newNightMode;
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        editor.putInt(MainApplication.PREF_NIGHT_MODE, newNightMode);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(newNightMode);
    }


    // Hiển thị Dialog để thêm từ mới (Giữ nguyên)
    private void showAddWordsDialog() {
        // ... (logic như cũ) ...
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogAddWordsBinding dialogBinding = DialogAddWordsBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        builder.setTitle(R.string.add_new_vocabulary_title);
        builder.setMessage(R.string.add_new_vocabulary_message);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String inputText = dialogBinding.editTextWordsInput.getText().toString();
            if (!inputText.trim().isEmpty()) {
                vocabularyViewModel.addOrUpdateWordsFromString(inputText);
                Toast.makeText(getContext(), R.string.processing_and_translating, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.please_enter_words, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        addWordsDialog = builder.create();
        addWordsDialog.show();
    }

    // Mở màn hình danh sách từ vựng (Giữ nguyên)
    private void openVocabularyList() {
        Intent intent = new Intent(getActivity(), VocabularyListActivity.class);
        startActivity(intent);
    }

    // Mở màn hình tạo truyện (Giữ nguyên)
    private void openCreateStory() {
        Intent intent = new Intent(getActivity(), StoryActivity.class);
        startActivity(intent);
    }

    // Mở màn hình Flashcards (giờ đây sẽ là màn hình ôn tập SRS)
    private void openFlashcards() {
        Intent intent = new Intent(getActivity(), FlashcardActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (addWordsDialog != null && addWordsDialog.isShowing()) {
            addWordsDialog.dismiss();
        }
        addWordsDialog = null;
    }
}
