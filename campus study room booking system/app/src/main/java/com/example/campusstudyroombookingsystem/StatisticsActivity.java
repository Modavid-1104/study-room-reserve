package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.StatisticsResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvToday;
    private TextView tvTotal;
    private TextView tvCount;
    private TextView tvStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvToday = findViewById(R.id.tvToday);
        tvTotal = findViewById(R.id.tvTotal);
        tvCount = findViewById(R.id.tvCount);
        tvStreak = findViewById(R.id.tvStreak);

        loadStatistics();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStatistics() {
        RetrofitClient.getApiService(this)
                .statistics()
                .enqueue(new Callback<ApiResponse<StatisticsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<StatisticsResponse>> call,
                                           Response<ApiResponse<StatisticsResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            StatisticsResponse data = response.body().getData();
                            if (data != null) {
                                tvToday.setText(formatMinutes(data.getTodayMinutes()));
                                tvTotal.setText(formatMinutes(data.getTotalMinutes()));
                                tvCount.setText(data.getCheckInCount() + " 次");
                                tvStreak.setText(data.getContinuousDays() + " 天");
                            }
                        } else {
                            Toast.makeText(StatisticsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<StatisticsResponse>> call, Throwable t) {
                        Toast.makeText(StatisticsActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 分钟转成"X小时Y分"
    private String formatMinutes(long minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        if (h == 0) {
            return m + " 分钟";
        }
        return h + " 小时 " + m + " 分";
    }
}
