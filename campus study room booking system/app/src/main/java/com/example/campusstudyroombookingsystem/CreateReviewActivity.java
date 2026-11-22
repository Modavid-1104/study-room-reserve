package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ReviewRequest;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        long reservationId = getIntent().getLongExtra("reservationId", -1);

        ratingBar = findViewById(R.id.ratingBar);
        etContent = findViewById(R.id.etContent);

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submit(reservationId));
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void submit(long reservationId) {
        // getRating() 返回 float，stepSize=1 时就是整数，转成 int
        int score = (int) ratingBar.getRating();
        String content = String.valueOf(etContent.getText()).trim();

        if (score < 1) {
            Toast.makeText(this, "请至少选择 1 颗星", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty()) {
            Toast.makeText(this, "评价内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        ReviewRequest request = new ReviewRequest(reservationId, score, content);
        RetrofitClient.getApiService(this)
                .createReview(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(CreateReviewActivity.this, "评价成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = response.body() != null ? response.body().getMsg() : "提交失败";
                            Toast.makeText(CreateReviewActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(CreateReviewActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
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
