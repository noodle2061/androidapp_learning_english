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
import androidx.navigation.NavController; // Import NavController
import androidx.navigation.fragment.NavHostFragment; // Import NavHostFragment

import com.example.learning_english.databinding.FragmentGrammarBinding;
import com.example.learning_english.R; // Đảm bảo import R

public class GrammarFragment extends Fragment {

    private FragmentGrammarBinding binding;
    private NavController navController; // Thêm NavController

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGrammarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Lấy NavController
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e("GrammarFragment", "NavController not found for this fragment", e);
            // Xử lý lỗi nếu không tìm thấy NavController (ví dụ: không nằm trong NavHost)
        }


        // Thiết lập sự kiện click cho nút Lý thuyết
        binding.buttonGrammarTheory.setOnClickListener(v -> {
            if (navController != null) {
                // Sử dụng action đã định nghĩa trong mobile_navigation.xml
                // để điều hướng đến GrammarTheoryListFragment
                navController.navigate(R.id.action_grammarFragment_to_grammarTheoryListFragment);
            } else {
                Toast.makeText(getContext(), "Lỗi điều hướng", Toast.LENGTH_SHORT).show();
            }
        });

        // Thiết lập sự kiện click cho nút Luyện tập
        binding.buttonGrammarPractice.setOnClickListener(v -> {
            if (navController != null) {
                // Điều hướng đến danh sách luyện tập (cần tạo Fragment này sau)
                navController.navigate(R.id.action_grammarFragment_to_grammarPracticeListFragment);
            } else {
                Toast.makeText(getContext(), "Lỗi điều hướng", Toast.LENGTH_SHORT).show();
            }
            // Tạm thời hiển thị Toast
            // Toast.makeText(getContext(), "Mở danh sách Luyện tập Ngữ pháp (Chưa làm)", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
