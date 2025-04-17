package com.example.learning_english.utils;

import com.example.learning_english.db.Word;
import java.util.concurrent.TimeUnit;

public class SpacedRepetitionScheduler {

    // Constants for recall ratings (nên đồng bộ với các nút trên UI)
    public static final int RATING_AGAIN = 1; // Người dùng quên/nhấn "Lặp lại"
    public static final int RATING_HARD = 2;  // Người dùng thấy khó
    public static final int RATING_GOOD = 3;  // Người dùng nhớ (đáp án đúng)
    public static final int RATING_EASY = 4;  // Người dùng thấy rất dễ

    private static final float MIN_EASE_FACTOR = 1.3f;
    private static final long ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final long TEN_MINUTES_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final long ONE_MINUTE_MILLIS = TimeUnit.MINUTES.toMillis(1);


    /**
     * Calculates the next review state for a word based on user rating.
     * Implements a simplified SM-2 like algorithm.
     *
     * @param word   The word being reviewed.
     * @param rating The user's recall rating (e.g., RATING_AGAIN, RATING_GOOD, RATING_EASY).
     * @return The same Word object with updated SRS fields (nextReviewTimestamp, reviewIntervalDays, easeFactor).
     */
    public static Word calculateNextReview(Word word, int rating) {
        long now = System.currentTimeMillis();
        int currentIntervalDays = word.getReviewIntervalDays();
        float currentEaseFactor = word.getEaseFactor();

        int nextIntervalDays;
        float nextEaseFactor = currentEaseFactor; // Start with current ease

        if (rating == RATING_AGAIN) {
            // Quên: Reset interval, giảm ease factor
            nextIntervalDays = 1; // Ôn lại vào ngày mai
            nextEaseFactor = Math.max(MIN_EASE_FACTOR, currentEaseFactor - 0.2f); // Giảm ease
            // Đặt thời gian ôn lại sớm hơn (ví dụ 10 phút sau) thay vì chờ ngày mai
            word.setNextReviewTimestamp(now + TEN_MINUTES_MILLIS);

        } else if (rating == RATING_HARD) {
            // Khó: Interval tăng ít, giảm nhẹ ease factor
            // Interval giữ nguyên hoặc tăng nhẹ, ví dụ: max(1, currentInterval * 1.0)
            nextIntervalDays = Math.max(1, (int) Math.round(currentIntervalDays * 1.0)); // Hoặc giữ nguyên currentIntervalDays
            nextEaseFactor = Math.max(MIN_EASE_FACTOR, currentEaseFactor - 0.15f);
            word.setNextReviewTimestamp(now + (long)nextIntervalDays * ONE_DAY_MILLIS);

        } else if (rating == RATING_GOOD) {
            // Nhớ: Tăng interval dựa trên ease factor
            if (currentIntervalDays <= 1) {
                nextIntervalDays = 3; // Lần đầu nhớ thì 3 ngày sau ôn lại
            } else {
                nextIntervalDays = Math.max(currentIntervalDays + 1, (int) Math.round(currentIntervalDays * currentEaseFactor));
            }
            // Ease factor giữ nguyên hoặc thay đổi nhẹ (không thay đổi trong SM-2 gốc khi rating=good)
            // nextEaseFactor = currentEaseFactor;
            word.setNextReviewTimestamp(now + (long)nextIntervalDays * ONE_DAY_MILLIS);

        } else if (rating == RATING_EASY) {
            // Dễ: Tăng interval nhiều, tăng ease factor
            if (currentIntervalDays <= 1) {
                nextIntervalDays = 4; // Lần đầu thấy dễ thì 4 ngày sau ôn lại
            } else {
                nextIntervalDays = Math.max(currentIntervalDays + 1, (int) Math.round(currentIntervalDays * currentEaseFactor * 1.3)); // Tăng nhiều hơn
            }
            nextEaseFactor = currentEaseFactor + 0.15f;
            word.setNextReviewTimestamp(now + (long)nextIntervalDays * ONE_DAY_MILLIS);

        } else {
            // Trường hợp rating không hợp lệ, không thay đổi gì
            return word;
        }

        // Cập nhật lại các trường SRS cho đối tượng Word
        word.setReviewIntervalDays(nextIntervalDays);
        word.setEaseFactor(nextEaseFactor);
        // nextReviewTimestamp đã được set trong các điều kiện if/else

        return word;
    }
}
