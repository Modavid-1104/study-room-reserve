package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.SeatResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatActivity extends AppCompatActivity {

    private SeatAdapter adapter;
    private long roomId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // 网格布局，每行 4 个
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        adapter = new SeatAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnSeatClickListener(seat -> {
            Intent intent = new Intent(SeatActivity.this, CreateReservationActivity.class);
            intent.putExtra("seatId", seat.getId());
            intent.putExtra("seatNo", seat.getSeatNo());
            startActivity(intent);
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        roomId = getIntent().getLongExtra("roomId", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到本页（含首次进入）都重新加载座位，保证状态实时
        loadSeats(roomId);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();   // 点返回箭头，关闭当前页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadSeats(long roomId) {
        RetrofitClient.getApiService(this)
                .seatList(roomId)
                .enqueue(new Callback<ApiResponse<List<SeatResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<SeatResponse>>> call,
                                           Response<ApiResponse<List<SeatResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(SeatActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<SeatResponse>>> call, Throwable t) {
                        Toast.makeText(SeatActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
