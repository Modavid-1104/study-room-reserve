package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvAddress;
    private TextView tvTime;
    private TextView tvTotalSeat;
    private TextView tvDescription;
    private long roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvTime = findViewById(R.id.tvTime);
        tvTotalSeat = findViewById(R.id.tvTotalSeat);
        tvDescription = findViewById(R.id.tvDescription);

        // 接收上一页传过来的 id
        this.roomId = getIntent().getLongExtra("roomId", -1);
        loadDetail(roomId);

        findViewById(R.id.btnViewSeat).setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailActivity.this, SeatActivity.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent);
        });

        findViewById(R.id.btnViewReview).setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailActivity.this, ReviewListActivity.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent);
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadDetail(long roomId) {
        RetrofitClient.getApiService(this)
                .roomDetail(roomId)
                .enqueue(new Callback<ApiResponse<StudyRoomResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<StudyRoomResponse>> call,
                                           Response<ApiResponse<StudyRoomResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            StudyRoomResponse room = response.body().getData();
                            if (room != null) {
                                tvName.setText(room.getName());
                                tvAddress.setText("位置：" + room.getAddress());
                                tvTime.setText("开放时间：" + room.getOpenTime() + " - " + room.getCloseTime());
                                tvTotalSeat.setText("总座位数：" + room.getTotalSeat());
                                tvDescription.setText(room.getDescription());
                            }
                        } else {
                            Toast.makeText(RoomDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<StudyRoomResponse>> call, Throwable t) {
                        Toast.makeText(RoomDetailActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();   // 点返回箭头，关闭当前页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
