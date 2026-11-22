package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.PageResponse;
import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomListActivity extends AppCompatActivity {

    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        // 绑定 Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(room -> {
            Intent intent = new Intent(this, RoomDetailActivity.class);
            intent.putExtra("roomId", room.getId());
            startActivity(intent);
        });
    }

    // 每次回到主页（含首次进入）都重新加载，实时刷新空闲数和进度条
    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }

    // 加载菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // 管理项仅管理员可见
        int role = getSharedPreferences("user", MODE_PRIVATE).getInt("role", 0);
        menu.findItem(R.id.action_admin).setVisible(role >= 1);
        return true;
    }

    // 处理菜单点击
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_message) {
            startActivity(new Intent(this, MessageActivity.class));
            return true;
        } else if (id == R.id.action_my) {
            startActivity(new Intent(this, MyReservationActivity.class));
            return true;
        } else if (id == R.id.action_admin) {
            startActivity(new Intent(this, AdminRoomActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }else if (id == R.id.action_quick) {
            startActivity(new Intent(this, QuickReserveActivity.class));
            return true;
        }else if (id == R.id.action_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;
        } else if (id == R.id.action_chat) {
        startActivity(new Intent(this, ChatActivity.class));
        return true;
    }



        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        getSharedPreferences("user", MODE_PRIVATE)
                .edit().remove("token").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadRooms() {
        RetrofitClient.getApiService(this)
                .roomPage(1, 10)
                .enqueue(new Callback<ApiResponse<PageResponse<StudyRoomResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call,
                                           Response<ApiResponse<PageResponse<StudyRoomResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PageResponse<StudyRoomResponse> page = response.body().getData();
                            if (page != null) {
                                adapter.setData(page.getRecords());
                            }
                        } else {
                            Toast.makeText(RoomListActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call, Throwable t) {
                        Toast.makeText(RoomListActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
