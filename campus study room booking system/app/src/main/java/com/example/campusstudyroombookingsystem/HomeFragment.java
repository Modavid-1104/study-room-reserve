package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.PageResponse;
import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RoomAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoomAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        // 转圈颜色用深灰，保持黑白灰
        swipeRefresh.setColorSchemeColors(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(this::loadRooms);

        // 点房间卡片 → 详情页
        adapter.setOnItemClickListener(room -> {
            Intent intent = new Intent(requireContext(), RoomDetailActivity.class);
            intent.putExtra("roomId", room.getId());
            startActivity(intent);
        });

        // 一键找座
        FloatingActionButton fab = view.findViewById(R.id.fab_quick);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuickReserveActivity.class)));
    }

    // 每次切回首页都重新加载，实时刷新空闲数
    @Override
    public void onResume() {
        super.onResume();
        loadRooms();
    }

    private void loadRooms() {
        RetrofitClient.getApiService(requireContext())
                .roomPage(1, 10)
                .enqueue(new Callback<ApiResponse<PageResponse<StudyRoomResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call,
                                           Response<ApiResponse<PageResponse<StudyRoomResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            PageResponse<StudyRoomResponse> page = response.body().getData();
                            if (page != null) {
                                adapter.setData(page.getRecords());
                            }
                        } else {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call,
                                          Throwable t) {
                        Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);

                    }
                });
    }
}
