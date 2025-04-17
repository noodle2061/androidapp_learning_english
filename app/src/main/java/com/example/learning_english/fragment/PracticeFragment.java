package com.example.learning_english.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.learning_english.R;
import com.example.learning_english.adapter.PracticeTopicAdapter;
import com.example.learning_english.databinding.FragmentPracticeBinding;
import com.example.learning_english.model.PracticeTopic;

import java.util.ArrayList;
import java.util.List;

public class PracticeFragment extends Fragment implements PracticeTopicAdapter.OnPracticeTopicClickListener {

    private static final String TAG = "PracticeFragment";
    private static final String PREFS_PRACTICE_NAME = "PracticePrefs";
    private static final String PREF_TOEIC_LEVEL = "ToeicLevel";
    private static final int DEFAULT_TOEIC_LEVEL = 500;
    private static final int MIN_TOEIC = 0;
    private static final int MAX_TOEIC = 990;


    private FragmentPracticeBinding binding;
    private PracticeTopicAdapter adapter;
    private List<PracticeTopic> practiceTopics = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_PRACTICE_NAME, Context.MODE_PRIVATE);
        setupRecyclerView();
        loadPracticeTopics();
        setupLevelButton();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayCurrentLevel();
    }

    private void setupRecyclerView() {
        adapter = new PracticeTopicAdapter(practiceTopics, this);
        binding.recyclerviewPracticeTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewPracticeTopics.setAdapter(adapter);
    }

    private void setupLevelButton() {
        binding.buttonSetLevel.setOnClickListener(v -> showSetLevelDialog());
    }

    private void displayCurrentLevel() {
        int currentLevel = sharedPreferences.getInt(PREF_TOEIC_LEVEL, DEFAULT_TOEIC_LEVEL);
        binding.textCurrentLevelValue.setText(String.valueOf(currentLevel));
    }

    private void showSetLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_level, null);
        final EditText editTextLevel = dialogView.findViewById(R.id.edit_text_toeic_level);

        // Set max length programmatically if needed (or use XML maxLength)
        // editTextLevel.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});

        // Set current level in EditText
        editTextLevel.setText(String.valueOf(sharedPreferences.getInt(PREF_TOEIC_LEVEL, DEFAULT_TOEIC_LEVEL)));
        editTextLevel.requestFocus(); // Focus on the input field
        editTextLevel.selectAll(); // Select all text


        builder.setView(dialogView)
                .setTitle(R.string.set_your_toeic_level)
                .setPositiveButton(R.string.save, (dialog, id) -> {
                    String levelStr = editTextLevel.getText().toString();
                    if (!TextUtils.isEmpty(levelStr)) {
                        try {
                            int level = Integer.parseInt(levelStr);
                            if (level >= MIN_TOEIC && level <= MAX_TOEIC) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(PREF_TOEIC_LEVEL, level);
                                editor.apply();
                                displayCurrentLevel(); // Update UI
                                Log.d(TAG, "Saved TOEIC Level: " + level);
                            } else {
                                Toast.makeText(getContext(), getString(R.string.error_level_out_of_range, MIN_TOEIC, MAX_TOEIC), Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), R.string.error_invalid_number, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.error_level_empty, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        builder.create().show();
    }


    private void loadPracticeTopics() {
        practiceTopics.clear();

        // Part 5
        practiceTopics.add(new PracticeTopic(getString(R.string.practice_header_part5)));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_word_type), "p5_word_type"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_tense), "p5_tense"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_voice), "p5_voice"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_verb_form), "p5_verb_form"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_conjunction_pronoun), "p5_conj_pron"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part5_item_vocabulary), "p5_vocab"));

        // Part 6
        practiceTopics.add(new PracticeTopic(getString(R.string.practice_header_part6)));
        practiceTopics.add(new PracticeTopic(getString(R.string.part6_item_fill_sentence), "p6_fill_sentence"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part6_item_mixed_practice), "p6_mixed"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part6_item_by_text_type), "p6_text_type"));

        // Part 7
        practiceTopics.add(new PracticeTopic(getString(R.string.practice_header_part7)));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_main_idea), "p7_main_idea"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_detail), "p7_detail"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_inference), "p7_inference"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_vocabulary), "p7_vocab")); // Synonym
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_sentence_meaning), "p7_sentence_meaning"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_not_true), "p7_not_true"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_fill_sentence), "p7_fill_sentence"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_structure_single), "p7_struct_single"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_structure_multiple), "p7_struct_multi"));
        practiceTopics.add(new PracticeTopic(getString(R.string.part7_item_by_text_type), "p7_text_type"));


        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPracticeTopicClick(PracticeTopic topic) {
        if (topic.type == PracticeTopic.TYPE_ITEM) { // Chỉ xử lý item
            Log.d(TAG, "Clicked Practice Topic: " + topic.title + " (ID: " + topic.id + ")");

            NavController navController = NavHostFragment.findNavController(this); // Lấy NavController ở đây
            if (navController != null && topic.id != null) {
                Bundle args = new Bundle();
                args.putString("practiceTopicId", topic.id); // Truyền ID
                args.putString("practiceTopicName", topic.title); // Truyền tên hiển thị

                try {
                    // Kiểm tra ID để quyết định điều hướng
                    if (topic.id.startsWith("p5_")) { // Giả sử ID của Part 5 bắt đầu bằng "p5_"
                        navController.navigate(R.id.action_practiceFragment_to_toeicPart5Fragment, args);
                    } else if (topic.id.startsWith("p6_")) {
                        Toast.makeText(getContext(), "Luyện tập Part 6: " + topic.title + " (Chưa làm)", Toast.LENGTH_SHORT).show();
                        // TODO: navController.navigate(R.id.action_practiceFragment_to_toeicPart6Fragment, args);
                    } else if (topic.id.startsWith("p7_")) {
                        Toast.makeText(getContext(), "Luyện tập Part 7: " + topic.title + " (Chưa làm)", Toast.LENGTH_SHORT).show();
                        // TODO: navController.navigate(R.id.action_practiceFragment_to_toeicPart7Fragment, args);
                    } else {
                        Toast.makeText(getContext(), "Chủ đề không xác định: " + topic.title, Toast.LENGTH_SHORT).show();
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Navigation error for topic ID " + topic.id, e);
                    Toast.makeText(getContext(), R.string.error_navigating, Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e(TAG, "NavController is null or topic ID is null.");
                Toast.makeText(getContext(), R.string.error_navigating, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}