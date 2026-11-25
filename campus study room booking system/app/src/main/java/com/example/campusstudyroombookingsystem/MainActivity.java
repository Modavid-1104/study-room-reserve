package com.example.campusstudyroombookingsystem;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.campusstudyroombookingsystem.util.WebSocketManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 建立 WebSocket 长连接（原来就有，保留）
        WebSocketManager.get().connect(this);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);

        // 首次进入默认显示首页
        switchFragment(new HomeFragment());

        // 底部标签点击：切换对应 Fragment
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
            } else if (id == R.id.nav_reservation) {
                switchFragment(new ReservationFragment());
            } else if (id == R.id.nav_message) {
                switchFragment(new MessageFragment());
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
            }
            return true;   // 表示已处理该点击
        });
    }

    // 用新 Fragment 替换容器内容
    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
