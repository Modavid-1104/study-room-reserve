package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.MessageSendRequest;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMessageActivity extends AppCompatActivity {

    private EditText etUserId;
    private EditText etTitle;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_message);

        etUserId = findViewById(R.id.etUserId);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btnSend).setOnClickListener(v -> send());
    }

    private void send() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String userIdText = etUserId.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        MessageSendRequest req = new MessageSendRequest();
        req.setTitle(title);
        req.setContent(content);
        // 填了用户ID就单发，留空则 null（后端识别为群发）
        if (!userIdText.isEmpty()) {
            req.setUserId(Long.parseLong(userIdText));
        }

        RetrofitClient.getApiService(this)
                .sendMessage(req)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminMessageActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                            etTitle.setText("");
                            etContent.setText("");
                            etUserId.setText("");
                        } else {
                            String msg = response.body() != null ? response.body().getMsg() : "发送失败";
                            Toast.makeText(AdminMessageActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(AdminMessageActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
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
