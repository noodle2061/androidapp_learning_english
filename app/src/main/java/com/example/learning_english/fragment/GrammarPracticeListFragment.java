package com.example.learning_english.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Đảm bảo import đúng

import com.example.learning_english.R;
// Sử dụng PracticeTopicAdapter và PracticeTopic model
import com.example.learning_english.adapter.PracticeTopicAdapter;
import com.example.learning_english.model.PracticeTopic;
import com.example.learning_english.databinding.FragmentGrammarPracticeListBinding;

import java.util.ArrayList;
import java.util.List;

// Implement listener của PracticeTopicAdapter
public class GrammarPracticeListFragment extends Fragment implements PracticeTopicAdapter.OnPracticeTopicClickListener {

    private static final String TAG = "GrammarPracticeList";
    private FragmentGrammarPracticeListBinding binding;
    // Thay đổi Adapter và kiểu dữ liệu của danh sách
    private PracticeTopicAdapter adapter;
    private List<PracticeTopic> practiceTopics = new ArrayList<>();
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGrammarPracticeListBinding.inflate(inflater, container, false);
        // Thiết lập RecyclerView với Adapter mới
        setupRecyclerView();
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found", e);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load dữ liệu đã được cấu trúc lại
        loadStructuredGrammarPracticeTopics();
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter mới với danh sách PracticeTopic và listener là Fragment này
        adapter = new PracticeTopicAdapter(practiceTopics, this);
        binding.recyclerviewGrammarPractice.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewGrammarPractice.setAdapter(adapter);
    }

    // Load và cấu trúc lại danh sách chủ đề luyện tập ngữ pháp
    private void loadStructuredGrammarPracticeTopics() {
        // Danh sách raw từ input của bạn
        String[] rawTopics = {
                "Danh từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Danh từ: Luyện tập: Nouns: countable, uncountable (Danh từ: đếm được, không đếm được)",
                "Danh từ: Luyện tập: Plural nouns (Danh từ số nhiều)",
                "Danh từ: Luyện tập: Plurals: mix (Số nhiều: tổng hợp)",
                "Danh từ: Luyện tập: Indefinite article (Mạo từ không xác định)",
                "Danh từ: Luyện tập: Definite article (Mạo từ xác định)",
                "Danh từ: Luyện tập: Few, less, little, much, many (Ít / nhiều)",
                "Danh từ: Luyện tập: Zero article (Không dùng mạo từ)",
                "Danh từ: Luyện tập: Quantity - mix (Số lượng - tổng hợp)",
                "Danh từ: Luyện tập: Articles: mix (Mạo từ: tổng hợp)",
                "Đại từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Đại từ: Luyện tập: Personal pronouns (Đại từ nhân xưng)",
                "Đại từ: Luyện tập: Reflexive pronouns (Đại từ phản thân)",
                "Đại từ: Luyện tập: Some, any, no, every (Một số, bất kỳ, không, mọi thứ)",
                "Đại từ: Luyện tập: Possessive pronouns (Đại từ sở hữu)",
                "Đại từ: Luyện tập: Both, all, whole, every, each (Cả hai, tất cả, toàn bộ, mọi, mỗi)",
                "Đại từ: Luyện tập: This / these / that / those",
                "Đại từ: Luyện tập: Another, other, either, neither (Cái khác, cũng không, mỗi)",
                "Tính từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Tính từ: Luyện tập: Too vs. enough",
                "Thì: Luyện tập: Trắc nghiệm format TOEIC",
                "Thì: Luyện tập: Present simple tense (Thì hiện tại đơn)",
                "Thì: Luyện tập: Present continuous (Thì hiện tại tiếp diễn)",
                "Thì: Luyện tập: Past simple tense (Thì quá khứ đơn)",
                "Thì: Luyện tập: Present tense: questions and negatives (Thì hiện tại: câu hỏi và phủ định)",
                "Thì: Luyện tập: Past tense: questions and negatives (Thì quá khứ: câu hỏi và phủ định)",
                "Thì: Luyện tập: Future simple tense (Thì tương lai đơn)",
                "Thì: Luyện tập: Present simple vs. present continuous (Thì hiện tại đơn vs. Thì hiện tại tiếp diễn)",
                "Thì: Luyện tập: Past continuous (Thì quá khứ tiếp diễn)",
                "Thì: Luyện tập: Future tenses: continuous vs. perfect (Các thì tương lai: tiếp diễn vs. hoàn thành)",
                "Thì: Luyện tập: Used to (Đã từng)",
                "Thì: Luyện tập: Present perfect tense (Thì hiện tại hoàn thành)",
                "Thì: Luyện tập: Will vs. going to",
                "Thì: Luyện tập: Tenses: mix (Các thì: tổng hợp)",
                "Thì: Luyện tập: Present continuous - double consonant (Hiện tại tiếp diễn - phụ âm đôi)",
                "Thì: Luyện tập: Talking about the present: mix (Nói về hiện tại: tổng hợp)",
                "Thì: Luyện tập: Past perfect (Thì quá khứ hoàn thành)",
                "Thì: Luyện tập: To do, to have, to be in present simple (To do, to have, to be: thì hiện tại)",
                "Thì: Luyện tập: Past simple vs. past continuous (Quá khứ đơn vs. quá khứ tiếp diễn)",
                "Thì: Luyện tập: To do, to have, to be: questions and negatives (To do, to have, to be: câu hỏi và phủ định)",
                "Thì: Luyện tập: Modal verbs and infinitive (Động từ khuyết thiếu và động từ nguyên thể)",
                "Thì: Luyện tập: Past simple vs. present perfect (Quá khứ đơn vs. hiện tại hoàn thành)",
                "Thì: Luyện tập: To do, to have, to be in past simple (To do, to have, to be: thì quá khứ)",
                "Thì: Luyện tập: Modal verbs: present simple (Động từ khuyết thiếu: hiện tại đơn)",
                "Thì: Luyện tập: Present perfect: simple vs. continuous (Hiện tại hoàn thành: HTHT đơn vs. HTHT tiếp diễn)",
                "Thì: Luyện tập: Modal verbs: past simple (Động từ khuyết thiếu: quá khứ đơn)",
                "Thì: Luyện tập: Verb to be (Động từ to be)",
                "Thì: Luyện tập: Past simple vs. past perfect (Quá khứ đơn vs. quá khứ hoàn thành)",
                "Thì: Luyện tập: Can / could (Có thể)",
                "Thì: Luyện tập: To be in past simple (Luyện tập: to be - thì quá khứ)",
                "Thì: Luyện tập: There is, there are (Có)",
                "Thì: Luyện tập: Must / have to / can (Phải / có thể)",
                "Thì: Luyện tập: Pictures from the past (Hình ảnh trong quá khứ)",
                "Thì: Luyện tập: Talking about the past: mix (Nói về quá khứ: tổng hợp)",
                "Thì: Luyện tập: Be, have, do: mix (Be, have, do: tổng hợp)",
                "Thì: Luyện tập: Should (Nên)",
                "Thì: Luyện tập: Modal verbs: mix (Động từ khuyết thiếu: tổng hợp)",
                "Thể: Luyện tập: Trắc nghiệm format TOEIC",
                "Thể: Luyện tập: Passive voice (Thể bị động)",
                "Thể: Luyện tập: Have something done (Hoàn thành điều gì đó)",
                "Động từ nguyên mẫu: Luyện tập: Make / let",
                "Động từ nguyên mẫu có 'to': Luyện tập: Trắc nghiệm format TOEIC",
                "Động từ nguyên mẫu có 'to': Luyện tập: Verb + object + to infinitive (Động từ + tân ngữ + ĐT nguyên mẫu có 'to')",
                "Danh động từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Danh động từ: Luyện tập: Gerund vs. infinitive (Danh động từ vs. động từ nguyên mẫu)",
                "Danh động từ: Luyện tập: Verb patterns: mix (Các dạng động từ: tổng hợp)",
                "Phân từ và cấu trúc phân từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Trạng từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Trạng từ: Luyện tập: Positions of adverbs in sentences (Vị trí của trạng từ trong câu)",
                "Trạng từ: Luyện tập: Adjective or adverb? (Tính từ hay trạng từ?)",
                "Trạng từ: Luyện tập: Adjectives and adverbs: mix (Tính từ và trạng từ: tổng hợp)",
                "Giới từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Giới từ: Luyện tập: Prepositions of time (Giới từ chỉ thời gian)",
                "Giới từ: Luyện tập: Prepositions of place (Giới từ chỉ địa điểm)",
                "Giới từ: Luyện tập: Prepositions of direction (Giới từ chỉ hướng)",
                "Giới từ: Luyện tập: Prepositions: other (Giới từ: khác)",
                "Giới từ: Luyện tập: Prepositions: mix (Giới từ: tổng hợp)",
                "Liên từ: Luyện tập: Trắc nghiệm format TOEIC",
                "Mệnh đề quan hệ: Luyện tập: Trắc nghiệm format TOEIC",
                "Câu điều kiện: Luyện tập: Trắc nghiệm format TOEIC",
                "Câu điều kiện: Luyện tập: First conditional (Điều kiện loại 1)",
                "Câu điều kiện: Luyện tập: Second and third conditional (Điều kiện loại 2 và 3)",
                "Câu điều kiện: Luyện tập: Wish, unless, if only, if not (Ước gì, trừ khi, nếu chỉ, nếu không)",
                "Câu điều kiện: Luyện tập: Conditionals: mix (Điều kiện: tổng hợp)",
                "Cấu trúc phân từ: Luyện tập: Trắc nghiệm format TOEIC", // Có vẻ lặp lại "Phân từ và cấu trúc phân từ"? Giữ lại theo input.
                "Cấu trúc so sánh: Luyện tập: Trắc nghiệm format TOEIC",
                "Cấu trúc so sánh: Luyện tập: Comparative / superlative adjectives (Tính từ so sánh hơn / so sánh nhất)",
                "Cấu trúc so sánh: Luyện tập: Comparative / superlative adverbs (Trạng từ so sánh hơn / so sánh nhất)",
                "Cấu trúc so sánh: Luyện tập: Than vs then"
        };

        practiceTopics.clear(); // Xóa danh sách hiện tại (nếu có)
        String currentHeader = null;

        for (String rawTopic : rawTopics) {
            String[] parts = rawTopic.split(":", 2); // Tách theo dấu ':' đầu tiên
            if (parts.length == 2) {
                String header = parts[0].trim(); // Phần trước dấu ':' là header
                String itemDisplayTitle = parts[1].trim(); // Phần sau dấu ':' là tiêu đề hiển thị

                // Trích xuất tên chủ đề thực tế để truyền đi (bỏ "Luyện tập: ")
                String actualExerciseTopic = itemDisplayTitle.replaceFirst("^Luyện tập:\\s*", "").trim();

                // Nếu header thay đổi, thêm header mới vào danh sách
                if (currentHeader == null || !currentHeader.equals(header)) {
                    currentHeader = header;
                    practiceTopics.add(new PracticeTopic(currentHeader)); // Type = TYPE_HEADER
                }

                // Thêm mục luyện tập vào danh sách
                practiceTopics.add(new PracticeTopic(itemDisplayTitle, actualExerciseTopic)); // Type = TYPE_ITEM

            } else {
                Log.w(TAG, "Không thể phân tích chủ đề ngữ pháp: " + rawTopic);
                // Có thể thêm như một item đơn lẻ nếu muốn
                // practiceTopics.add(new PracticeTopic(rawTopic, rawTopic));
            }
        }

        // Cập nhật adapter và hiển thị RecyclerView
        adapter.notifyDataSetChanged();
        binding.recyclerviewGrammarPractice.setVisibility(View.VISIBLE);
        binding.textviewPracticeListPlaceholder.setVisibility(View.GONE); // Ẩn placeholder
    }

    // Xử lý khi một mục luyện tập được click
    @Override
    public void onPracticeTopicClick(PracticeTopic topic) {
        // Chỉ xử lý khi click vào item (không phải header)
        if (topic.type == PracticeTopic.TYPE_ITEM) {
            // topic.id chứa tên chủ đề thực tế đã trích xuất (ví dụ: "Trắc nghiệm format TOEIC")
            String actualTopicName = topic.id;
            Log.d(TAG, "Clicked Grammar Practice Topic: '" + topic.title + "' -> Navigating with actual topic: '" + actualTopicName + "'");

            if (navController != null && actualTopicName != null) {
                Bundle args = new Bundle();
                // Truyền tên chủ đề thực tế sang GrammarExerciseFragment
                args.putString("practiceTopicName", actualTopicName);
                try {
                    // Sử dụng action ID đã định nghĩa trong navigation graph
                    navController.navigate(R.id.action_grammarPracticeListFragment_to_grammarExerciseFragment, args);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG,"Navigation action/arguments error.", e);
                    Toast.makeText(getContext(), R.string.error_navigating_to_exercise, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "NavController is null or actualTopicName is null, cannot navigate.");
                Toast.makeText(getContext(), R.string.error_navigating, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh memory leak
    }
}