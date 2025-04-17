package com.example.learning_english.network;

import android.content.Context;
import android.util.Log;

// QUAN TRỌNG: Thêm import cho BuildConfig
import com.example.learning_english.BuildConfig;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp Singleton để quản lý các lệnh gọi API đến Gemini và MyMemory.
 * Phiên bản này lấy API key từ BuildConfig một cách an toàn.
 */
public class ApiClient {

    private static final String TAG = "ApiClient";
    private static ApiClient instance;
    private RequestQueue requestQueue;

    // --- Thông tin API ---
    // KHÔNG khai báo GEMINI_API_KEY ở đây nữa. Sẽ lấy từ BuildConfig khi cần.
    // private final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY; // <--- XÓA DÒNG NÀY

    // Giữ lại các hằng số khác
    private final String GEMINI_MODEL_STORY = "gemini-2.0-flash"; // Model cho tạo truyện (Cập nhật lên 1.5 flash)
    private final String GEMINI_MODEL_TRANSLATE = "gemini-2.0-flash"; // Model cho dịch (Cập nhật lên 1.5 flash)
    private final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";
    // --------------------

    private static final int API_TIMEOUT_MS = 60000; // 60 giây

    // Constructor riêng tư
    private ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    // Lấy instance (Singleton)
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    /**
     * Gọi API Gemini để tạo nội dung (ví dụ: tạo truyện).
     * @param prompt Prompt yêu cầu nội dung.
     * @param listener Callback để nhận kết quả hoặc lỗi.
     */
    public void generateGeminiContent(String prompt, ApiResponseListener listener) {
        // 1. Lấy API key từ BuildConfig bên trong phương thức
        String apiKey = BuildConfig.GEMINI_API_KEY;

        // 2. Kiểm tra API key trước khi sử dụng
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            Log.e(TAG, "Gemini API Key is missing or not configured in local.properties/BuildConfig!");
            if (listener != null) {
                listener.onError(new VolleyError("API Key not configured"));
            }
            return; // Dừng lại nếu không có key hợp lệ
        }

        // 3. Sử dụng API key để tạo URL
        String apiUrl = GEMINI_API_URL_BASE + GEMINI_MODEL_STORY + ":generateContent?key=" + apiKey;
        Log.d(TAG, "Calling Gemini Content API: " + apiUrl);
        Log.d(TAG, "Prompt (first 100 chars): " + (prompt != null ? prompt.substring(0, Math.min(prompt.length(), 100)) + "..." : "null"));

        // Tạo request body (giữ nguyên)
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray contentsArray = new JSONArray();
            JSONObject partsObject = new JSONObject();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            JSONArray partsArray = new JSONArray();
            partsArray.put(textPart);
            partsObject.put("parts", partsArray);
            contentsArray.put(partsObject);
            requestBody.put("contents", contentsArray);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 2048); // Tăng giới hạn token nếu cần
            requestBody.put("generationConfig", generationConfig);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating Gemini content JSON request body: ", e);
            if (listener != null) {
                listener.onError(new VolleyError("Error creating request body: " + e.getMessage()));
            }
            return;
        }

        // Tạo và gửi request (giữ nguyên)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                response -> { // Success Listener
                    Log.d(TAG, "Gemini Content API Response: Success"); // Tránh log toàn bộ response lớn
                    if (listener != null) {
                        listener.onSuccess(response);
                    }
                },
                error -> { // Error Listener
                    Log.e(TAG, "Gemini Content API Error: ", error);
                    if (listener != null) {
                        listener.onError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                API_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Gọi API Gemini để dịch danh sách từ.
     * @param englishWords Danh sách từ tiếng Anh cần dịch.
     * @param listener Callback để nhận kết quả hoặc lỗi.
     */
    public void translateWordsWithGemini(List<String> englishWords, ApiResponseListener listener) {
        if (englishWords == null || englishWords.isEmpty()) {
            Log.w(TAG, "translateWordsWithGemini called with empty list.");
            if (listener != null) {
                listener.onError(new VolleyError("Word list is empty."));
            }
            return;
        }

        // 1. Lấy API key từ BuildConfig
        String apiKey = BuildConfig.GEMINI_API_KEY;

        // 2. Kiểm tra API key
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            Log.e(TAG, "Gemini API Key is missing for translateWordsWithGemini!");
            if (listener != null) {
                listener.onError(new VolleyError("API Key not configured"));
            }
            return;
        }

        // 3. Sử dụng API key để tạo URL
        String apiUrl = GEMINI_API_URL_BASE + GEMINI_MODEL_TRANSLATE + ":generateContent?key=" + apiKey;
        Log.d(TAG, "Calling Gemini Translate API: " + apiUrl);

        // Tạo prompt (giữ nguyên)
        StringBuilder promptBuilder = new StringBuilder("trả cho tôi nghĩa tiếng việt phổ biến nhất của các từ này dưới dạng ```txt. mỗi nghĩa nằm trên một dòng tương ứng với từ đầu vào:\n");
        for (String word : englishWords) {
            promptBuilder.append(word).append("\n");
        }
        if (promptBuilder.length() > 0 && promptBuilder.charAt(promptBuilder.length() - 1) == '\n') {
            promptBuilder.setLength(promptBuilder.length() - 1);
        }
        String prompt = promptBuilder.toString();
        Log.d(TAG, "Translate Prompt: " + prompt);

        // Tạo request body (giữ nguyên)
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray contentsArray = new JSONArray();
            JSONObject partsObject = new JSONObject();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            JSONArray partsArray = new JSONArray();
            partsArray.put(textPart);
            partsObject.put("parts", partsArray);
            contentsArray.put(partsObject);
            requestBody.put("contents", contentsArray);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.2);
            requestBody.put("generationConfig", generationConfig);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating Gemini translate JSON request body: ", e);
            if (listener != null) {
                listener.onError(new VolleyError("Error creating request body: " + e.getMessage()));
            }
            return;
        }

        // Tạo và gửi request (giữ nguyên)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                response -> { // Success Listener
                    Log.d(TAG, "Gemini Translate API Response: Success");
                    if (listener != null) {
                        listener.onSuccess(response);
                    }
                },
                error -> { // Error Listener
                    Log.e(TAG, "Gemini Translate API Error: ", error);
                    if (listener != null) {
                        listener.onError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                API_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }


    /**
     * Gọi API MyMemory để dịch một từ.
     * @param wordToTranslate Từ cần dịch.
     * @param listener Callback để nhận kết quả hoặc lỗi.
     */
    public void translateWordWithMyMemory(String wordToTranslate, ApiResponseListener listener) {
        // Phương thức này không cần API key của Gemini nên giữ nguyên
        if (wordToTranslate == null || wordToTranslate.isEmpty()) {
            Log.w(TAG, "translateWordWithMyMemory called with empty word.");
            if (listener != null) {
                listener.onError(new VolleyError("Word to translate is empty."));
            }
            return;
        }

        String encodedWord;
        try {
            encodedWord = URLEncoder.encode(wordToTranslate, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "URL encoding failed for MyMemory", e);
            if (listener != null) {
                listener.onError(new VolleyError("URL encoding failed: " + e.getMessage()));
            }
            return;
        }

        String url = MYMEMORY_API_URL + "?q=" + encodedWord + "&langpair=en|vi";
        Log.d(TAG, "Calling MyMemory API: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "MyMemory API Response: Success");
                    if (listener != null) {
                        listener.onSuccess(response);
                    }
                },
                error -> {
                    Log.e(TAG, "MyMemory API Error: ", error);
                    if (listener != null) {
                        listener.onError(error);
                    }
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                API_TIMEOUT_MS / 2,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Hủy tất cả các request đang chờ trong queue (ví dụ khi Activity/Fragment bị hủy).
     * @param tag Tag của các request cần hủy (có thể set tag khi add request vào queue).
     */
    public void cancelAllRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
