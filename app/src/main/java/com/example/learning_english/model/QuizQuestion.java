package com.example.learning_english.model; // Hoặc package phù hợp

import java.util.List;
import java.util.Objects; // Import Objects for equals/hashCode

/**
 * Lớp đại diện cho một câu hỏi trắc nghiệm.
 * (Phiên bản có giải thích và trạng thái trả lời)
 */
public class QuizQuestion {
    private String questionText;
    private List<String> options; // Danh sách 4 lựa chọn A, B, C, D
    private String correctAnswer; // Lưu trữ đáp án đúng (ví dụ: "A", "B", "C", "D")
    private String explanation;   // Giải thích cho đáp án đúng

    // Trường lưu trạng thái
    private boolean isAnswered;
    private int userAnswerIndex; // -1: Chưa trả lời, 0: A, 1: B, 2: C, 3: D

    // Constructor cập nhật
    public QuizQuestion(String questionText, List<String> options, String correctAnswer, String explanation) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = (explanation != null) ? explanation : ""; // Đảm bảo không null
        // Khởi tạo trạng thái ban đầu
        this.isAnswered = false;
        this.userAnswerIndex = -1;
    }

    // Getters
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
    public boolean isAnswered() { return isAnswered; }
    public int getUserAnswerIndex() { return userAnswerIndex; }

    // Setters cho trạng thái
    public void setAnsweredState(int userAnswerIndex) {
        this.isAnswered = true;
        this.userAnswerIndex = userAnswerIndex;
    }

    // (Optional) equals và hashCode nếu cần so sánh đối tượng
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizQuestion that = (QuizQuestion) o;
        return isAnswered == that.isAnswered &&
                userAnswerIndex == that.userAnswerIndex &&
                Objects.equals(questionText, that.questionText) &&
                Objects.equals(options, that.options) &&
                Objects.equals(correctAnswer, that.correctAnswer) &&
                Objects.equals(explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionText, options, correctAnswer, explanation, isAnswered, userAnswerIndex);
    }
}
