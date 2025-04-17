package com.example.learning_english.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.learning_english.R;
import com.example.learning_english.databinding.FragmentToeicPart5QuizBinding; // Sử dụng binding mới

// Kế thừa từ BaseQuizFragment và chỉ định loại ViewBinding
public class ToeicPart5Fragment extends BaseQuizFragment<FragmentToeicPart5QuizBinding> {

    // TAG và các hằng số khác có thể được định nghĩa lại ở đây hoặc dùng từ Base
    private static final String ARG_PRACTICE_TOPIC_ID = "practiceTopicId";
    // private static final String ARG_PRACTICE_TOPIC_NAME = "practiceTopicName"; // Đã có trong Base
    private static final String PREFS_PRACTICE_NAME = "PracticePrefs";
    private static final String PREF_TOEIC_LEVEL = "ToeicLevel";
    private static final int DEFAULT_TOEIC_LEVEL = 500;

    // Các biến cục bộ nếu cần
    private String practiceTopicId;
    private int userToeicLevel;
    private SharedPreferences sharedPreferences;

    // --- Triển khai các phương thức abstract từ BaseQuizFragment ---

    @Override
    protected FragmentToeicPart5QuizBinding initializeBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Trả về instance của ViewBinding cụ thể
        return FragmentToeicPart5QuizBinding.inflate(inflater, container, false);
        // Lưu ý: Đảm bảo tên class Binding khớp với tên tệp layout XML (fragment_toeic_part5_quiz.xml)
    }

    @Override
    protected String getFragmentTag() {
        return "ToeicPart5Fragment"; // Trả về TAG cụ thể
    }

    @Override
    protected void initializeQuizParameters() {
        // Lấy arguments và SharedPreferences
        if (getArguments() != null) {
            practiceTopicId = getArguments().getString(ARG_PRACTICE_TOPIC_ID);
            // Lấy practiceTopicName từ base class (nếu cần) hoặc từ arguments
            practiceTopicName = getArguments().getString("practiceTopicName", "Luyện tập Part 5");
        } else {
            practiceTopicName = "Luyện tập Part 5"; // Giá trị mặc định
            Log.w(getFragmentTag(), "Arguments are null!");
        }

        if (getContext() != null) {
            sharedPreferences = requireContext().getSharedPreferences(PREFS_PRACTICE_NAME, Context.MODE_PRIVATE);
            userToeicLevel = sharedPreferences.getInt(PREF_TOEIC_LEVEL, DEFAULT_TOEIC_LEVEL);
        } else {
            userToeicLevel = DEFAULT_TOEIC_LEVEL;
        }
        Log.d(getFragmentTag(), "Initialized Params: ID=" + practiceTopicId + ", Name=" + practiceTopicName + ", Level=" + userToeicLevel);
    }


    @Override
    protected String buildApiPrompt() {
        // Tạo prompt API dựa trên thông tin của TOEIC Part 5
        String difficulty;
        if (userToeicLevel <= 400) difficulty = "rất cơ bản";
        else if (userToeicLevel <= 550) difficulty = "cơ bản";
        else if (userToeicLevel <= 700) difficulty = "trung cấp";
        else if (userToeicLevel <= 850) difficulty = "khá";
        else difficulty = "nâng cao";

        return String.format(
                "Tạo chính xác %d câu hỏi trắc nghiệm dạng TOEIC Part 5 (Incomplete Sentences) về chủ đề: '%s'. " +
                        "Độ khó của câu hỏi nên phù hợp với người học có mục tiêu TOEIC khoảng %d điểm (%s). " +
                        "Yêu cầu quan trọng: Với mỗi câu hỏi, hãy cung cấp một giải thích ngắn gọn bằng tiếng Việt cho đáp án đúng. " +
                        "Định dạng trả về:\n" +
                        "1. Câu hỏi (có chỗ trống ____ hoặc ...)\n" +
                        "A. Lựa chọn A\n" +
                        "==B. Lựa chọn B (đáp án đúng có dấu == phía trước)\n" +
                        "C. Lựa chọn C\n" +
                        "D. Lựa chọn D\n" +
                        "%s" +
                        "Giải thích cho câu hỏi 1...\n" +
                        "\n" +
                        "2. Câu hỏi tiếp theo...\n...\n%s" +
                        "Giải thích cho câu hỏi 2...\n" +
                        "\n... (tiếp tục cho đủ %d câu)\n" +
                        "Hãy đặt TOÀN BỘ nội dung vào bên trong một khối mã ```txt duy nhất.",
                NUM_QUESTIONS,
                practiceTopicName, // Sử dụng tên chủ đề đã lấy từ arguments
                userToeicLevel,
                difficulty,
                EXPLANATION_SEPARATOR,
                EXPLANATION_SEPARATOR,
                NUM_QUESTIONS
        );
    }

    @Override
    protected int getResultNavigationActionId() {
        // Trả về ID của action điều hướng từ fragment này sang ResultFragment
        // Đảm bảo action này được định nghĩa trong mobile_navigation.xml
        return R.id.action_toeicPart5Fragment_to_resultFragment;
    }

    // --- Các phương thức khác ---
    // Không cần override các phương thức đã có trong BaseQuizFragment
    // trừ khi bạn muốn thay đổi hành vi cụ thể nào đó cho TOEIC Part 5.
}
    