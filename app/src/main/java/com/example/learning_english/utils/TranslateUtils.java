package com.example.learning_english.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils; // Import TextUtils
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.learning_english.R;
import com.example.learning_english.db.AppDatabase;
import com.example.learning_english.db.DictionaryDao;
import com.example.learning_english.db.DictionaryEntry; // Import DictionaryEntry
import com.example.learning_english.network.ApiClient;
import com.example.learning_english.network.ApiResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;

public class TranslateUtils {

    private static final String TAG = "TranslateUtils";
    private static BackgroundColorSpan currentHighlightedSpan = null;
    private static TextView currentlyHighlightedTextView = null;
    private static final ExecutorService databaseReadExecutor = AppDatabase.databaseWriteExecutor;

    @SuppressLint("ClickableViewAccessibility")
    public static void setupDoubleClickTranslate(Context context, TextView textView, ApiClient apiClient) {
        // Giữ nguyên logic setupDoubleClickTranslate
        if (context == null || textView == null || apiClient == null) { return; }
        textView.setTextIsSelectable(true);
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int offset = textView.getOffsetForPosition(e.getX(), e.getY());
                CharSequence textChars = textView.getText();
                if (textChars == null) return true;
                String text = textChars.toString();
                int wordStart = findWordStart(text, offset);
                int wordEnd = findWordEnd(text, offset);
                if (wordStart != -1 && wordEnd != -1 && wordStart < wordEnd) {
                    String selectedText = text.substring(wordStart, wordEnd).trim();
                    String wordToLookup = selectedText.toLowerCase();
                    if (!wordToLookup.isEmpty() && !wordToLookup.contains(" ")) {
                        removeCurrentHighlight();
                        highlightWordInternal(context, textView, wordStart, wordEnd);
                        lookupWord(context, apiClient, wordToLookup, selectedText, textView);
                        return true;
                    } else { removeCurrentHighlight(); }
                } else { removeCurrentHighlight(); Toast.makeText(context, R.string.translate_hint_error, Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public boolean onSingleTapConfirmed(MotionEvent e) { removeHighlightIfNeededInternal(textView); return false; }
        });
        textView.setOnTouchListener((v, event) -> {
            boolean handledByGesture = gestureDetector.onTouchEvent(event);
            if (!handledByGesture && event.getAction() == MotionEvent.ACTION_UP) { v.postDelayed(() -> removeHighlightIfNeededInternal(textView), 100); }
            return false;
        });
    }

    private static void lookupWord(Context context, ApiClient apiClient, String wordToLookup, String originalWord, TextView targetTextView) {
        databaseReadExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
            DictionaryDao dao = db.dictionaryDao();
            // *** THAY ĐỔI: Lấy toàn bộ Entry ***
            DictionaryEntry offlineEntry = null;
            try {
                offlineEntry = dao.findEntryByWord(wordToLookup); // Gọi hàm DAO mới
            } catch (Exception e) {
                Log.e(TAG, "Error querying offline dictionary for: " + wordToLookup, e);
            }

            // *** THAY ĐỔI: Lấy definition và pronunciation từ Entry ***
            final DictionaryEntry finalEntry = offlineEntry; // Biến final để dùng trong lambda

            if (targetTextView != null && targetTextView.getHandler() != null) {
                targetTextView.post(() -> {
                    if (finalEntry != null) { // Tìm thấy Entry offline
                        Log.i(TAG, "Word '" + wordToLookup + "' found offline.");
                        // Truyền cả definition và pronunciation
                        showDefinitionDialog(context, originalWord, finalEntry.getPronunciation(), finalEntry.getDefinition(), true);
                    } else {
                        // Không tìm thấy offline
                        Log.i(TAG, "Word '" + wordToLookup + "' not found offline. Checking network...");
                        if (isNetworkAvailable(context)) {
                            Log.i(TAG, "Network available. Calling online API...");
                            translateWordWithMyMemoryApiClientInternal(context, apiClient, originalWord);
                        } else {
                            Log.w(TAG, "Network not available.");
                            Toast.makeText(context, R.string.error_offline_not_found_no_network, Toast.LENGTH_LONG).show();
                            removeCurrentHighlight();
                        }
                    }
                });
            } else { Log.e(TAG, "lookupWord: targetTextView or its Handler is null."); }
        });
    }

    // *** THAY ĐỔI: Thêm tham số pronunciation ***
    private static void showDefinitionDialog(Context context, String originalWord, @Nullable String pronunciation, String definition, boolean isOffline) {
        String title = context.getString(R.string.translation_result_title, originalWord);
        if (isOffline) {
            title += " (Offline)";
        }

        Spanned formattedDefinition = formatDefinitionToHtml(definition);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_definition_layout, null);
        TextView definitionTextView = dialogView.findViewById(R.id.textview_definition_content);
        // *** THÊM: Tìm TextView phiên âm ***
        TextView pronunciationTextView = dialogView.findViewById(R.id.textview_pronunciation);

        definitionTextView.setText(formattedDefinition);
        definitionTextView.setTextIsSelectable(true);

        // *** THÊM: Hiển thị phiên âm nếu có ***
        if (pronunciationTextView != null) {
            if (!TextUtils.isEmpty(pronunciation)) {
                pronunciationTextView.setText(pronunciation);
                pronunciationTextView.setVisibility(View.VISIBLE); // Hiện TextView lên
            } else {
                pronunciationTextView.setVisibility(View.GONE); // Ẩn nếu không có phiên âm
            }
        }
        // ************************************

        try {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.close, (dialog, which) -> removeCurrentHighlight())
                    .setOnCancelListener(dialog -> removeCurrentHighlight())
                    .show();
        } catch (Exception ex) {
            Log.e(TAG, "Error showing definition dialog", ex);
            removeCurrentHighlight();
        }
    }

    private static Spanned formatDefinitionToHtml(String rawDefinition) {
        // Giữ nguyên logic formatDefinitionToHtml
        if (rawDefinition == null || rawDefinition.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { return Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY); }
            else { return Html.fromHtml(""); }
        }
        StringBuilder htmlBuilder = new StringBuilder();
        String[] lines = rawDefinition.split("\\n");
        for (String line : lines) {
            line = line.trim(); if (line.isEmpty()) continue;
            if (line.startsWith("*")) { htmlBuilder.append("<br/><b>").append(line).append("</b><br/>"); }
            else if (line.startsWith("-")) { htmlBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;• ").append(line.substring(1).trim()).append("<br/>"); }
            else if (line.startsWith("=")) { htmlBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;<i>").append(line).append("</i><br/>"); }
            else if (line.startsWith("!")) { htmlBuilder.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;<b>").append(line).append("</b><br/>"); }
            else { htmlBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(line).append("<br/>"); }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { return Html.fromHtml(htmlBuilder.toString(), Html.FROM_HTML_MODE_LEGACY); }
        else { return Html.fromHtml(htmlBuilder.toString()); }
    }

    private static void translateWordWithMyMemoryApiClientInternal(Context context, ApiClient apiClient, String originalWord) {
        // Giữ nguyên logic gọi API
        Toast.makeText(context, context.getString(R.string.translating_word_online, originalWord), Toast.LENGTH_SHORT).show();
        apiClient.translateWordWithMyMemory(originalWord, new ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject responseData = response.getJSONObject("responseData");
                    String translatedText = responseData.getString("translatedText");
                    // *** THAY ĐỔI: Truyền null cho pronunciation khi gọi từ API ***
                    showDefinitionDialog(context, originalWord, null, "- " + translatedText, false);
                } catch (JSONException e) { Log.e(TAG, "Error parsing MyMemory JSON response", e); Toast.makeText(context, R.string.translation_error_parsing, Toast.LENGTH_SHORT).show(); removeCurrentHighlight(); }
                catch (Exception ex) { Log.e(TAG, "Error showing translation dialog after API call", ex); removeCurrentHighlight(); }
            }
            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "MyMemory API Error: ", error); String errorMsg = context.getString(R.string.translation_error_api);
                if (error.networkResponse != null) { errorMsg += " (Code: " + error.networkResponse.statusCode + ")"; }
                else if (error instanceof TimeoutError) { errorMsg = context.getString(R.string.translation_error_timeout); }
                else if (error.getMessage() != null && error.getMessage().contains("URL encoding failed")) { errorMsg = context.getString(R.string.translation_error_encoding); }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show(); removeCurrentHighlight();
            }
        });
    }

    // Các hàm tiện ích khác giữ nguyên
    private static boolean isNetworkAvailable(Context context) { ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo ni = cm.getActiveNetworkInfo(); return ni != null && ni.isConnected(); }
    private static int findWordStart(String text, int offset) { if (text == null || text.isEmpty() || offset < 0 || offset > text.length()) { return -1; } if (offset == text.length() || (offset < text.length() && !Character.isLetterOrDigit(text.charAt(offset)))) { if (offset > 0 && Character.isLetterOrDigit(text.charAt(offset - 1))) { offset--; } else { return -1; } } int start = offset; while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) { start--; } return start; }
    private static int findWordEnd(String text, int offset) { if (text == null || text.isEmpty() || offset < 0 || offset > text.length()) { return -1; } if (offset == text.length()) { if (offset > 0 && Character.isLetterOrDigit(text.charAt(offset - 1))) { return offset; } else { return -1; } } if (!Character.isLetterOrDigit(text.charAt(offset))) { if (offset < text.length() - 1 && Character.isLetterOrDigit(text.charAt(offset + 1))) { offset++; } else if (offset > 0 && Character.isLetterOrDigit(text.charAt(offset - 1))) { /* OK */ } else { return -1; } } int end = offset; while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) { end++; } return end; }
    private static void highlightWordInternal(Context context, TextView textView, int start, int end) { if (textView == null || !(textView.getText() instanceof Spannable)) return; Spannable spannable = (Spannable) textView.getText(); removeCurrentHighlight(); BackgroundColorSpan newSpan = new BackgroundColorSpan(ContextCompat.getColor(context, R.color.highlight_color)); if (start >= 0 && end >= 0 && start < end && end <= spannable.length()) { try { spannable.setSpan(newSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); currentHighlightedSpan = newSpan; currentlyHighlightedTextView = textView; } catch (IndexOutOfBoundsException e) { Log.e(TAG, "Error applying highlight span", e); } } else { Log.w(TAG, "Invalid range for highlight"); } }
    public static void removeCurrentHighlight() { if (currentlyHighlightedTextView != null && currentHighlightedSpan != null && currentlyHighlightedTextView.getText() instanceof Spannable) { try { ((Spannable) currentlyHighlightedTextView.getText()).removeSpan(currentHighlightedSpan); } catch (Exception e) { Log.e(TAG, "Error removing highlight span", e); } } currentHighlightedSpan = null; currentlyHighlightedTextView = null; }
    private static void removeHighlightIfNeededInternal(TextView textView) { if (textView != null && textView.getSelectionStart() == textView.getSelectionEnd()) { if (textView == currentlyHighlightedTextView) { removeCurrentHighlight(); } } }

}
    