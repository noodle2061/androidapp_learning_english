package com.example.learning_english.fragment;

import android.content.res.AssetManager;
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

import com.example.learning_english.R;
import com.example.learning_english.adapter.GrammarListAdapter;
import com.example.learning_english.databinding.FragmentGrammarTheoryListBinding;
import com.example.learning_english.model.grammar.GrammarTopic;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.Comparator; // Import Comparator
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrammarTheoryListFragment extends Fragment implements GrammarListAdapter.OnTopicClickListener {

    private static final String TAG = "GrammarTheoryList";
    private static final String GRAMMAR_ASSETS_FOLDER = "grammar_theory";

    private FragmentGrammarTheoryListBinding binding;
    private GrammarListAdapter adapter;
    private List<GrammarTopic> grammarTopics = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGrammarTheoryListBinding.inflate(inflater, container, false);
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
        loadGrammarTopicsFromAssets();
    }

    private void setupRecyclerView() {
        adapter = new GrammarListAdapter(grammarTopics, this);
        binding.recyclerviewGrammarTheoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewGrammarTheoryList.setAdapter(adapter);
    }

    // Load danh sách file JSON từ assets và parse tiêu đề
    private void loadGrammarTopicsFromAssets() {
        binding.progressBarTheoryList.setVisibility(View.VISIBLE);
        binding.recyclerviewGrammarTheoryList.setVisibility(View.GONE);
        binding.textviewTheoryListPlaceholder.setVisibility(View.GONE);

        executorService.execute(() -> {
            AssetManager assetManager = requireContext().getAssets();
            List<GrammarTopic> loadedTopics = new ArrayList<>();
            Gson gson = new Gson();
            try {
                String[] files = assetManager.list(GRAMMAR_ASSETS_FOLDER);
                if (files != null) {
                    for (String filename : files) {
                        if (filename.endsWith(".json")) {
                            InputStreamReader reader = null;
                            try {
                                InputStream is = assetManager.open(GRAMMAR_ASSETS_FOLDER + "/" + filename);
                                reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                                GrammarTopic topic = gson.fromJson(reader, GrammarTopic.class);
                                if (topic != null && topic.topicTitle != null) {
                                    topic.sourceFileName = filename;
                                    loadedTopics.add(topic);
                                } else {
                                    Log.w(TAG, "Skipping file: " + filename);
                                }
                            } catch (IOException | JsonSyntaxException e) {
                                Log.e(TAG, "Error reading/parsing asset: " + filename, e);
                            } finally {
                                if (reader != null) {
                                    try { reader.close(); } catch (IOException ignored) {}
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error listing assets: " + GRAMMAR_ASSETS_FOLDER, e);
            }

            // --- THÊM LOGIC SẮP XẾP Ở ĐÂY ---
            // Sắp xếp danh sách loadedTopics theo topicTitle (Alphabetical)
            // Sử dụng Comparator để so sánh không phân biệt hoa thường và bỏ qua số ở đầu nếu có
            Collections.sort(loadedTopics, new Comparator<GrammarTopic>() {
                @Override
                public int compare(GrammarTopic t1, GrammarTopic t2) {
                    // Lấy tiêu đề, loại bỏ số và dấu chấm ở đầu (nếu có) để so sánh chuẩn hơn
                    String title1 = t1.topicTitle.trim();
                    String title2 = t2.topicTitle.trim();
                    // So sánh không phân biệt hoa thường
                    return title1.compareToIgnoreCase(title2);
                }
            });
            // ---------------------------------

            // Cập nhật UI trên Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.progressBarTheoryList.setVisibility(View.GONE);
                    if (loadedTopics.isEmpty()) {
                        binding.textviewTheoryListPlaceholder.setText("Không tìm thấy chủ đề ngữ pháp nào.");
                        binding.textviewTheoryListPlaceholder.setVisibility(View.VISIBLE);
                        binding.recyclerviewGrammarTheoryList.setVisibility(View.GONE);
                    } else {
                        grammarTopics.clear();
                        grammarTopics.addAll(loadedTopics); // Thêm danh sách đã sắp xếp
                        adapter.notifyDataSetChanged();
                        binding.recyclerviewGrammarTheoryList.setVisibility(View.VISIBLE);
                        binding.textviewTheoryListPlaceholder.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onTopicClick(GrammarTopic topic) {
        if (topic.sourceFileName != null && navController != null) {
            Bundle args = new Bundle();
            args.putString("json_filename", topic.sourceFileName);
            args.putString("topicTitle", topic.topicTitle);
            try {
                navController.navigate(R.id.action_grammarTheoryListFragment_to_grammarDetailFragment, args);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Navigation action/arguments error.", e);
                Toast.makeText(getContext(), "Lỗi điều hướng", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Cannot navigate: filename=" + topic.sourceFileName + ", navController=" + (navController != null));
            Toast.makeText(getContext(), "Không thể mở chủ đề này", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
