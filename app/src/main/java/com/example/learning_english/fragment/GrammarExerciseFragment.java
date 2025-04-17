package com.example.learning_english.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.learning_english.R;
import com.example.learning_english.databinding.FragmentGrammarExerciseQuizBinding; // Sử dụng binding mới

// Kế thừa từ BaseQuizFragment và chỉ định loại ViewBinding
public class GrammarExerciseFragment extends BaseQuizFragment<FragmentGrammarExerciseQuizBinding> {

    private static final String ARG_PRACTICE_TOPIC_NAME = "practiceTopicName"; // Key argument

    // --- Triển khai các phương thức abstract từ BaseQuizFragment ---

    @Override
    protected FragmentGrammarExerciseQuizBinding initializeBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Trả về instance của ViewBinding cụ thể
        return FragmentGrammarExerciseQuizBinding.inflate(inflater, container, false);
        // Lưu ý: Đảm bảo tên class Binding khớp với tên tệp layout XML (fragment_grammar_exercise_quiz.xml)
    }

    @Override
    protected String getFragmentTag() {
        return "GrammarExerciseFragment"; // Trả về TAG cụ thể
    }

    @Override
    protected void initializeQuizParameters() {
        // Lấy tên chủ đề từ arguments và gán vào biến practiceTopicName của Base class
        if (getArguments() != null) {
            practiceTopicName = getArguments().getString(ARG_PRACTICE_TOPIC_NAME, "Luyện tập Ngữ pháp");
        } else {
            practiceTopicName = "Luyện tập Ngữ pháp"; // Giá trị mặc định
        }
        Log.d(getFragmentTag(), "Initialized Params: Name=" + practiceTopicName);
    }


    @Override
    protected String buildApiPrompt() {
        // Tạo prompt API dựa trên tên chủ đề ngữ pháp
        return String.format(
                "Tạo chính xác %d câu hỏi trắc nghiệm (A, B, C, D) về chủ đề ngữ pháp tiếng Anh sau: '%s'. " +
                        "Yêu cầu quan trọng: Các câu hỏi phải có độ khó tăng dần, 2 câu cuối cùng nên là câu nâng cao hoặc bẫy. phần giải thích được diễn đạt bằng tiếng Việt. " +
                        "Với mỗi câu hỏi, hãy cung cấp một giải thích ngắn gọn và rõ ràng cho đáp án đúng. " +
                        "Định dạng trả về:\n" +
                        "1. Câu hỏi\n" +
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
                EXPLANATION_SEPARATOR,
                EXPLANATION_SEPARATOR,
                NUM_QUESTIONS
        );
    }

    @Override
    protected int getResultNavigationActionId() {
        // Trả về ID của action điều hướng từ fragment này sang ResultFragment
        // Đảm bảo action này được định nghĩa trong mobile_navigation.xml
        return R.id.action_grammarExerciseFragment_to_grammarResultFragment;
    }

    // --- Các phương thức khác ---
    // Không cần override các phương thức đã có trong BaseQuizFragment.
}
    