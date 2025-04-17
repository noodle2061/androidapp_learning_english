package com.example.learning_english.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Correct import

import com.example.learning_english.R;
import com.example.learning_english.adapter.GrammarDetailAdapter;
import com.example.learning_english.databinding.FragmentGrammarDetailBinding;
import com.example.learning_english.model.grammar.ContentItem;
import com.example.learning_english.model.grammar.GrammarTopic;
import com.example.learning_english.model.grammar.Section;
import com.example.learning_english.model.grammar.SubSection;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrammarDetailFragment extends Fragment {

    private static final String TAG = "GrammarDetailFragment";
    private static final String ARG_JSON_FILENAME = "json_filename";
    private static final String GRAMMAR_ASSETS_FOLDER = "grammar_theory";

    private FragmentGrammarDetailBinding binding;
    private GrammarDetailAdapter adapter;
    private List<Object> displayItems = new ArrayList<>();
    private String jsonFilename;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Background thread

    // --- Phương thức tạo instance mới cho Fragment ---
    // Không cần thiết nếu dùng Navigation Component Safe Args,
    // nhưng hữu ích nếu tạo thủ công
    public static GrammarDetailFragment newInstance(String jsonFilename) {
        GrammarDetailFragment fragment = new GrammarDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JSON_FILENAME, jsonFilename);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy filename từ arguments (được truyền từ NavController)
            jsonFilename = getArguments().getString(ARG_JSON_FILENAME);
        } else {
            Log.e(TAG, "JSON filename argument is missing!");
            jsonFilename = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGrammarDetailBinding.inflate(inflater, container, false);
        setupRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAndDisplayGrammarData(); // Load data khi view đã sẵn sàng
    }

    private void setupRecyclerView() {
        adapter = new GrammarDetailAdapter(getContext(), displayItems);
        binding.recyclerviewGrammarDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewGrammarDetail.setAdapter(adapter);
    }

    // --- Đọc và hiển thị dữ liệu ---
    private void loadAndDisplayGrammarData() {
        if (jsonFilename == null || jsonFilename.isEmpty()) {
            showError("Không tìm thấy tệp dữ liệu ngữ pháp.");
            return;
        }

        binding.progressBarDetail.setVisibility(View.VISIBLE); // Show progress bar
        binding.recyclerviewGrammarDetail.setVisibility(View.GONE);
        binding.textviewDetailError.setVisibility(View.GONE);

        // Thực hiện load và parse ở background thread
        executorService.execute(() -> {
            final GrammarTopic grammarTopic = loadGrammarTopicFromJsonAsset(jsonFilename);
            final List<Object> flattenedItems = new ArrayList<>();

            if (grammarTopic != null) {
                // Làm phẳng cấu trúc dữ liệu
                flattenDataStructure(grammarTopic, flattenedItems);
            }

            // Cập nhật UI trên Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.progressBarDetail.setVisibility(View.GONE);
                    if (grammarTopic != null && !flattenedItems.isEmpty()) {
                        // Cập nhật tiêu đề ActionBar
                        if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(grammarTopic.topicTitle);
                        }
                        displayItems.clear();
                        displayItems.addAll(flattenedItems);
                        adapter.notifyDataSetChanged();
                        binding.recyclerviewGrammarDetail.setVisibility(View.VISIBLE);
                    } else {
                        showError("Không thể tải hoặc phân tích dữ liệu ngữ pháp.");
                    }
                });
            }
        });
    }

    // --- Đọc JSON từ assets ---
    private GrammarTopic loadGrammarTopicFromJsonAsset(String filename) {
        Gson gson = new Gson();
        InputStreamReader reader = null;
        try {
            String filePath = GRAMMAR_ASSETS_FOLDER + "/" + filename;
            InputStream is = requireContext().getAssets().open(filePath);
            reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            GrammarTopic topic = gson.fromJson(reader, GrammarTopic.class);
            if (topic != null) {
                topic.sourceFileName = filename; // Gán lại tên file nếu cần
            }
            return topic;
        } catch (IOException e) {
            Log.e(TAG, "Error reading grammar JSON asset: " + filename, e);
            return null;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing grammar JSON asset: " + filename, e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset reader", e);
                }
            }
        }
    }

    // --- Làm phẳng cấu trúc dữ liệu lồng nhau ---
    private void flattenDataStructure(GrammarTopic topic, List<Object> targetList) {
        // Không cần clear ở đây vì targetList được truyền vào và là list mới
        if (topic == null || topic.sections == null) {
            return;
        }

        for (Section section : topic.sections) {
            if (section == null) continue; // Bỏ qua section null

            if (section.sectionTitle != null && !section.sectionTitle.isEmpty()) {
                targetList.add("SECTION_" + section.sectionTitle);
            }

            if (section.subSections != null) {
                for (SubSection subSection : section.subSections) {
                    if (subSection == null) continue; // Bỏ qua subSection null

                    // Luôn thêm subSectionTitle (có thể rỗng), adapter sẽ xử lý ẩn/hiện
                    targetList.add("SUBSECTION_" + (subSection.subSectionTitle != null ? subSection.subSectionTitle : ""));


                    if (subSection.content != null) {
                        for(ContentItem contentItem : subSection.content) {
                            if (contentItem != null && contentItem.type != null) { // Kiểm tra null
                                targetList.add(contentItem);
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Hiển thị lỗi ---
    private void showError(String message) {
        if (binding != null) { // Kiểm tra binding trước khi dùng
            binding.textviewDetailError.setText(message);
            binding.textviewDetailError.setVisibility(View.VISIBLE);
            binding.recyclerviewGrammarDetail.setVisibility(View.GONE);
            binding.progressBarDetail.setVisibility(View.GONE);
        }
        // Hiển thị Toast nếu cần
        // Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
    