package com.example.learning_english; // Package base của bạn

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class MainApplication extends Application {

    // Tên file và key cho SharedPreferences
    public static final String PREFS_NAME = "AppSettingsPrefs";
    public static final String PREF_NIGHT_MODE = "NightMode";

    @Override
    public void onCreate() {
        super.onCreate();

        // Đọc chế độ đã lưu từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Lấy chế độ đã lưu, mặc định là theo hệ thống nếu chưa lưu
        int currentNightMode = sharedPreferences.getInt(PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Áp dụng chế độ ngay khi ứng dụng khởi động
        AppCompatDelegate.setDefaultNightMode(currentNightMode);
    }
}
