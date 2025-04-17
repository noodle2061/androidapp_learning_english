package com.example.learning_english.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View; // Giữ lại nếu cần

import com.example.learning_english.R;
import com.example.learning_english.adapter.WordListAdapter;
import com.example.learning_english.databinding.ActivityVocabularyListBinding;
import com.example.learning_english.db.Word;
import com.example.learning_english.viewmodel.VocabularyViewModel;


public class VocabularyListActivity extends AppCompatActivity implements WordListAdapter.OnWordInteractionListener {

    private ActivityVocabularyListBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private WordListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.vocabulary_list_title);
        }

        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new WordListAdapter(new WordListAdapter.WordDiff(), this, this);
        binding.recyclerviewWordList.setAdapter(adapter);
        binding.recyclerviewWordList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModel() {
        vocabularyViewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);
        vocabularyViewModel.getAllWords().observe(this, words -> {
            adapter.submitList(words);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- TRIỂN KHAI INTERFACE TỪ ADAPTER ---

    @Override
    public void onWordClick(Word word) {
        // (Giữ nguyên logic hiển thị nghĩa)
        if (word == null) return;
        String translation = word.getVietnameseTranslation();
        if (translation == null || translation.isEmpty() || translation.startsWith("Lỗi")) {
            translation = getString(R.string.no_translation_placeholder);
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_result_title, word.getEnglishWord()))
                .setMessage("Tiếng Anh: " + word.getEnglishWord() + "\nTiếng Việt: " + translation)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    @Override
    public void onWordDeleteClicked(Word word) {
        // (Giữ nguyên logic xóa)
        if (word == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getResources().getQuantityString(R.plurals.confirm_delete_message_single, 1, word.getEnglishWord()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    vocabularyViewModel.deleteSingleWord(word);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Xử lý khi checkbox được nhấn (ĐÃ THAY ĐỔI TRONG LẦN TRƯỚC)
    @Override
    public void onWordReviewToggled(Word word, boolean isChecked) {
        if (word == null) return;
        // Gọi phương thức mới trong ViewModel để chỉ cập nhật cờ isForReview
        // Đảm bảo dòng này gọi đúng phương thức setWordReviewMark
        vocabularyViewModel.setWordReviewMark(word, isChecked); // <-- KIỂM TRA KỸ DÒNG NÀY
    }
    // ---------------------------------------
}
