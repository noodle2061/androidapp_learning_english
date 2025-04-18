package com.example.learning_english.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Import ArrayAdapter
import android.widget.Spinner; // Import Spinner
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
import com.example.learning_english.databinding.DialogAddWordsBinding; // Binding cũ, sẽ thay bằng view inflation
import com.example.learning_english.databinding.FragmentVocabularyBinding;
import com.example.learning_english.db.VocabularyPackage; // Import VocabularyPackage
import com.example.learning_english.viewmodel.VocabularyViewModel;

import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List

public class VocabularyFragment extends Fragment {

    private FragmentVocabularyBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private AlertDialog addWordsDialog;
    private List<VocabularyPackage> currentPackages = new ArrayList<>(); // Cache package list

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        vocabularyViewModel = new ViewModelProvider(requireActivity()).get(VocabularyViewModel.class);

        // Observe packages to populate the spinner later
        vocabularyViewModel.getAllPackages().observe(getViewLifecycleOwner(), packages -> {
            if (packages != null) {
                currentPackages = packages;
                Log.d("VocabFragment", "Updated package list size: " + currentPackages.size());
            } else {
                currentPackages = new ArrayList<>();
            }
            // If dialog is showing, maybe update spinner? Or just populate on dialog creation.
        });

        // --- THIẾT LẬP SỰ KIỆN CLICK ---
        binding.buttonAddNewWord.setOnClickListener(v -> showAddWordsDialog()); // Gọi hàm dialog đã cập nhật
        binding.buttonViewWordList.setOnClickListener(v -> openVocabularyList());
        binding.buttonCreateStory.setOnClickListener(v -> openCreateStory());
        binding.buttonToggleTheme.setOnClickListener(v -> toggleDayNightMode());
        binding.buttonFlashcards.setOnClickListener(v -> openFlashcards());
        // -------------------------------

        // --- QUAN SÁT SỐ TỪ CẦN ÔN TẬP ---
        vocabularyViewModel.getDueReviewCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                if (count > 0) {
                    String reviewText = getResources().getQuantityString(R.plurals.words_to_review_count, count, count);
                    binding.textviewReviewCount.setText(reviewText);
                    binding.textviewReviewCount.setVisibility(View.VISIBLE);
                } else {
                    binding.textviewReviewCount.setText(R.string.words_to_review_zero);
                    binding.textviewReviewCount.setVisibility(View.VISIBLE);
                }
            } else {
                binding.textviewReviewCount.setVisibility(View.GONE);
            }
        });
        // ---------------------------------

        return root;
    }

    // Hiển thị Dialog để thêm từ mới (ĐÃ CẬP NHẬT)
    private void showAddWordsDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        // Inflate layout mới
        View dialogView = inflater.inflate(R.layout.dialog_add_words, null);
        final Spinner packageSpinner = dialogView.findViewById(R.id.spinner_package_select);
        final android.widget.EditText wordsEditText = dialogView.findViewById(R.id.edit_text_words_input); // Sử dụng android.widget.EditText

        // --- Populate Spinner ---
        // Tạo ArrayAdapter sử dụng danh sách package đã cache và layout spinner mặc định
        // Hiển thị tên package trong Spinner bằng cách override toString() trong VocabularyPackage
        // Hoặc tạo custom adapter nếu cần hiển thị phức tạp hơn.
        ArrayAdapter<VocabularyPackage> packageAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item, // Layout cho item hiển thị
                currentPackages // Danh sách package đã lấy từ ViewModel
        );
        // Layout cho danh sách thả xuống
        packageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packageSpinner.setAdapter(packageAdapter);
        // -----------------------

        builder.setView(dialogView);
        builder.setTitle(R.string.add_new_vocabulary_title);
        // Không cần setMessage nữa vì layout đã có mô tả

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String inputText = wordsEditText.getText().toString();
            VocabularyPackage selectedPkg = (VocabularyPackage) packageSpinner.getSelectedItem();
            int selectedPackageId = VocabularyViewModel.DEFAULT_PACKAGE_ID; // ID mặc định

            if (selectedPkg != null) {
                selectedPackageId = selectedPkg.getId();
                Log.d("VocabFragment", "Package selected: " + selectedPkg.getName() + " (ID: " + selectedPackageId + ")");
            } else {
                // Xử lý trường hợp không có package nào (có thể không cho lưu hoặc lưu vào mặc định)
                if (currentPackages.isEmpty()) {
                    Toast.makeText(getContext(), R.string.error_no_packages_exist, Toast.LENGTH_LONG).show();
                    return; // Không cho lưu nếu chưa có package nào
                }
                Log.w("VocabFragment", "No package selected or spinner empty, using default ID: " + selectedPackageId);
                // Có thể hiển thị cảnh báo cho người dùng ở đây
            }

            if (!inputText.trim().isEmpty()) {
                // Gọi ViewModel với packageId đã chọn
                vocabularyViewModel.addOrUpdateWordsFromString(inputText, selectedPackageId);
                Toast.makeText(getContext(), R.string.processing_and_translating, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.please_enter_words, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        addWordsDialog = builder.create();
        addWordsDialog.show();
    }


    // --- Các phương thức khác giữ nguyên ---
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

    private void openVocabularyList() {
        Intent intent = new Intent(getActivity(), VocabularyListActivity.class);
        startActivity(intent);
    }

    private void openCreateStory() {
        Intent intent = new Intent(getActivity(), StoryActivity.class);
        startActivity(intent);
    }

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
