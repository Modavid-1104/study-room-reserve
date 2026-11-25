package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.campusstudyroombookingsystem.util.WebSocketManager;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int role = requireContext()
                .getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
                .getInt("role", 0);

        // 角色文字
        view.findViewById(R.id.tv_role);
        android.widget.TextView tvRole = view.findViewById(R.id.tv_role);
        tvRole.setText(role >= 1 ? "管理员" : "学生用户");

        // 管理后台仅管理员可见
        view.findViewById(R.id.item_admin)
                .setVisibility(role >= 1 ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.item_statistics).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), StatisticsActivity.class)));

        view.findViewById(R.id.item_chat).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChatActivity.class)));

        view.findViewById(R.id.item_admin).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminRoomActivity.class)));

        view.findViewById(R.id.item_logout).setOnClickListener(v -> logout());
    }

    private void logout() {
        // 关闭 WebSocket，再清登录凭证
        WebSocketManager.get().disconnect();

        requireContext()
                .getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
                .edit()
                .remove("token")
                .apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
