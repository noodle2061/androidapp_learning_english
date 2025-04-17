package com.example.learning_english.utils; // Tạo package utils nếu chưa có

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Trích xuất nội dung văn bản từ một khối mã Markdown.
     * Ví dụ: ```lang\nNội dung\n```
     *
     * @param text     Văn bản đầy đủ chứa khối mã.
     * @param language Ngôn ngữ được chỉ định sau ``` (ví dụ: "txt", "java"). Nếu null hoặc rỗng, sẽ tìm khối ``` bất kỳ.
     * @return Nội dung bên trong khối mã, hoặc null nếu không tìm thấy.
     */
    public static String extractTextFromMarkdownCodeBlock(String text, String language) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        Pattern pattern;
        if (language != null && !language.isEmpty()) {
            // Pattern tìm khối ```lang ... ``` (?s) cho phép . khớp với newline
            pattern = Pattern.compile("```" + Pattern.quote(language) + "(.*?)\\n?```", Pattern.DOTALL);
        } else {
            // Pattern tìm khối ``` ... ``` bất kỳ
            pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        }

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // group(1) là nội dung bên trong cặp ```
            return matcher.group(1) != null ? matcher.group(1).trim() : "";
        }

        // Fallback: Nếu không tìm thấy khối ``` nào và không yêu cầu ngôn ngữ cụ thể,
        // trả về toàn bộ văn bản (giả định API có thể không dùng ```)
        if ((language == null || language.isEmpty()) && !text.contains("```")) {
            return text.trim();
        }


        return null; // Không tìm thấy khối phù hợp
    }
}
