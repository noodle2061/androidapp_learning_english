package com.example.learning_english.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.fragment.app.FragmentManager;

import com.example.learning_english.R;
import com.example.learning_english.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toast.makeText(MainActivity.this, "author: Rua Con 2k3", Toast.LENGTH_SHORT).show();

        // --- Bỏ phần thiết lập Toolbar ---

        // --- Thiết lập Navigation ---
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Chỉ cần liên kết với BottomNavigationView
            NavigationUI.setupWithNavController(binding.navView, navController);
        } else {
            throw new IllegalStateException("NavHostFragment not found in layout!");
        }
        // --------------------------
    }

    // --- Bỏ các phương thức liên quan đến Options Menu ---
    // onCreateOptionsMenu, onOptionsItemSelected, onSupportNavigateUp
}
