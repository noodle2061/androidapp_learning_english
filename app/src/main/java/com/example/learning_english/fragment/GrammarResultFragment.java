package com.example.learning_english.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.learning_english.R;
import com.example.learning_english.databinding.FragmentGrammarResultBinding;

public class GrammarResultFragment extends Fragment {

    private static final String TAG = "GrammarResultFragment";
    private FragmentGrammarResultBinding binding;
    private NavController navController;
    private int score = 0;
    private int totalQuestions = 0;
    private String practiceTopicName = ""; // Thêm để truyền lại khi làm tiếp

    private String originFragment = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy dữ liệu điểm số và chủ đề từ arguments
        if (getArguments() != null) {
            score = getArguments().getInt("score", 0);
            totalQuestions = getArguments().getInt("totalQuestions", 0);
            practiceTopicName = getArguments().getString("practiceTopicName", ""); // Lấy tên chủ đề
            Log.d(TAG, "Received score: " + score + "/" + totalQuestions + ", Topic: " + practiceTopicName);
        } else {
            Log.e(TAG, "Arguments bundle is null!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGrammarResultBinding.inflate(inflater, container, false);
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found for this fragment", e);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị điểm số
        if (binding != null) {
            binding.textScore.setText(String.format("%d/%d", score, totalQuestions));

            // Xử lý nút "Làm tiếp"
            binding.buttonRetry.setOnClickListener(v -> {
                if (navController != null) {
                    Log.d(TAG, "Retry button clicked. Navigating back to Exercise with topic: " + practiceTopicName);
                    // Tạo bundle mới chứa tên chủ đề để bắt đầu lại bài tập
                    Bundle args = new Bundle();
                    args.putString("practiceTopicName", practiceTopicName);
                    // Điều hướng đến một instance MỚI của GrammarExerciseFragment
                    // Sử dụng action đã định nghĩa trong navigation graph
                    try {
                        // Chúng ta cần action từ Result -> Exercise
                        // Ví dụ: R.id.action_grammarResultFragment_to_grammarExerciseFragment
                        // Cần định nghĩa action này trong mobile_navigation.xml
                        navController.navigate(R.id.action_grammarResultFragment_to_grammarExerciseFragment, args);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation failed for Retry button. Check action ID.", e);
                        // Fallback: Quay lại danh sách luyện tập nếu action lỗi
                        navController.popBackStack(R.id.navigation_grammar_practice_list, false);
                    }
                }
            });

            // Xử lý nút "Quay lại màn hình Ngữ pháp"
            binding.buttonBackToGrammar.setOnClickListener(v -> {
                if (navController != null) {
                    Log.d(TAG, "Back to Grammar button clicked.");
                    // Pop back stack đến màn hình GrammarFragment (ID: navigation_grammar)
                    // false: không bao gồm navigation_grammar trong việc pop
                    try {
                        navController.popBackStack(R.id.navigation_grammar, false);
                    } catch (Exception e) {
                        Log.e(TAG, "Error popping back stack to GrammarFragment", e);
                        // Fallback nếu lỗi
                        navController.popBackStack();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh memory leak
    }
}
