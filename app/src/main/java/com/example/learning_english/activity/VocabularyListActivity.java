package com.example.learning_english.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learning_english.R;
import com.example.learning_english.adapter.VocabularyPackageAdapter; // Import new adapter
import com.example.learning_english.adapter.WordListAdapter;
import com.example.learning_english.databinding.ActivityVocabularyListBinding;
import com.example.learning_english.db.VocabularyPackage; // Import new entity
import com.example.learning_english.db.Word;
import com.example.learning_english.viewmodel.VocabularyViewModel;

import java.util.ArrayList;

public class VocabularyListActivity extends AppCompatActivity implements
        WordListAdapter.OnWordInteractionListener,
        VocabularyPackageAdapter.OnPackageInteractionListener { // Implement new listener

    private static final String TAG = "VocabListActivity";
    private ActivityVocabularyListBinding binding;
    private VocabularyViewModel vocabularyViewModel;
    private WordListAdapter wordAdapter;
    private VocabularyPackageAdapter packageAdapter; // Add package adapter
    private VocabularyPackage selectedPackage = null; // Track selected package

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Remove ActionBar if using custom header
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setTitle(R.string.vocabulary_list_title);
        // }

        setupRecyclerViews();
        setupViewModel();
        setupButtons();
    }

    private void setupRecyclerViews() {
        // Package RecyclerView
        packageAdapter = new VocabularyPackageAdapter(new VocabularyPackageAdapter.PackageDiff(), this);
        binding.recyclerviewPackageList.setAdapter(packageAdapter);
        binding.recyclerviewPackageList.setLayoutManager(new LinearLayoutManager(this));

        // Word RecyclerView
        wordAdapter = new WordListAdapter(new WordListAdapter.WordDiff(), this, this);
        binding.recyclerviewWordList.setAdapter(wordAdapter);
        binding.recyclerviewWordList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModel() {
        vocabularyViewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);

        // Observe Packages
        vocabularyViewModel.getAllPackages().observe(this, packages -> {
            Log.d(TAG, "Packages updated: " + (packages != null ? packages.size() : "null"));
            packageAdapter.submitList(packages);
            // Restore selection if needed, or clear word list if selected package is gone
            if (selectedPackage != null && (packages == null || !packages.contains(selectedPackage))) {
                clearWordList();
            } else if (selectedPackage != null && packages != null) {
                // Re-find the selected package by ID in the new list to update adapter selection
                int newPosition = -1;
                for (int i = 0; i < packages.size(); i++) {
                    if (packages.get(i).getId() == selectedPackage.getId()) {
                        newPosition = i;
                        break;
                    }
                }
                if (newPosition != -1) {
                    packageAdapter.setSelectedPosition(newPosition);
                } else {
                    clearWordList(); // Selected package no longer exists
                }
            }
        });

        // Observe Words for the selected package
        vocabularyViewModel.getWordsForSelectedPackage().observe(this, words -> {
            if (selectedPackage != null) {
                Log.d(TAG, "Words updated for package " + selectedPackage.getName() + ": " + (words != null ? words.size() : "null"));
                wordAdapter.submitList(words);
                binding.recyclerviewWordList.setVisibility(View.VISIBLE);
                binding.textviewNoPackageSelected.setVisibility(View.GONE);
            } else {
                // Should not happen if logic is correct, but clear list just in case
                clearWordList();
            }
        });

        // Observe Loading state (optional)
        vocabularyViewModel.isLoading().observe(this, isLoading -> {
            binding.progressBarVocabList.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupButtons() {
        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonAddPackage.setOnClickListener(v -> showAddPackageDialog());
    }

    private void clearWordList() {
        selectedPackage = null;
        wordAdapter.submitList(new ArrayList<>()); // Clear word list
        binding.textviewSelectedPackageTitle.setVisibility(View.GONE);
        binding.recyclerviewWordList.setVisibility(View.GONE);
        binding.textviewNoPackageSelected.setVisibility(View.VISIBLE);
        packageAdapter.setSelectedPosition(RecyclerView.NO_POSITION); // Clear selection in package list
        vocabularyViewModel.clearSelectedPackage(); // Tell ViewModel to clear selection
    }


    private void showAddPackageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_package, null); // Create this layout
        final EditText editTextPackageName = dialogView.findViewById(R.id.edit_text_package_name); // Add this ID to the layout

        builder.setView(dialogView)
                .setTitle(R.string.add_new_package_title) // Add this string resource
                .setPositiveButton(R.string.add, (dialog, id) -> {
                    String packageName = editTextPackageName.getText().toString().trim();
                    if (!TextUtils.isEmpty(packageName)) {
                        vocabularyViewModel.addEmptyPackage(packageName); // Call ViewModel method
                    } else {
                        Toast.makeText(this, R.string.error_package_name_empty, Toast.LENGTH_SHORT).show(); // Add string
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        builder.create().show();
    }


    // --- WordListAdapter.OnWordInteractionListener Callbacks ---

    @Override
    public void onWordClick(Word word) {
        // Same as before: Show definition dialog
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
        // Same as before: Show confirmation and call ViewModel
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

    @Override
    public void onWordReviewToggled(Word word, boolean isChecked) {
        // Same as before: Call ViewModel to update individual word's review status
        if (word == null) return;
        vocabularyViewModel.setWordReviewMark(word, isChecked);
    }

    // --- VocabularyPackageAdapter.OnPackageInteractionListener Callbacks ---

    @Override
    public void onPackageClicked(VocabularyPackage pkg, int position) {
        Log.d(TAG, "Package clicked: " + pkg.getName() + " at position " + position);
        if (selectedPackage == null || selectedPackage.getId() != pkg.getId()) {
            selectedPackage = pkg;
            // Update selected item visual in adapter
            packageAdapter.setSelectedPosition(position);
            // Update title and trigger word loading in ViewModel
            binding.textviewSelectedPackageTitle.setText(getString(R.string.words_in_package_title, pkg.getName()));
            binding.textviewSelectedPackageTitle.setVisibility(View.VISIBLE);
            binding.textviewNoPackageSelected.setVisibility(View.GONE);
            vocabularyViewModel.selectPackage(pkg.getId()); // Tell ViewModel to load words for this package
        }
        // If the same package is clicked again, do nothing or maybe collapse/clear?
    }

    @Override
    public void onPackageCheckboxChanged(VocabularyPackage pkg, boolean isChecked) {
        Log.d(TAG, "Package checkbox changed: " + pkg.getName() + ", isChecked: " + isChecked);
        // Call ViewModel to update the review status for ALL words in this package
        vocabularyViewModel.updatePackageReviewStatus(pkg.getId(), isChecked);
        // Optionally show a toast
        String message = getString(isChecked ? R.string.package_marked_for_review : R.string.package_unmarked_for_review, pkg.getName());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --- Other Methods ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Clean up binding
    }
}
