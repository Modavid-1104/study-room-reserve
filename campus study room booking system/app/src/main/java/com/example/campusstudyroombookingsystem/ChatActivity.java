package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ChatMsg;
import com.example.campusstudyroombookingsystem.model.ChatRequestBody;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.UUID;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.campusstudyroombookingsystem.util.LocationHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ChatAdapter adapter;
    private EditText etInput;
    private RecyclerView recycler;
    private String conversationId;
    private static final int REQ_LOCATION = 1002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 进入聊天页生成一次会话ID，整个页面固定使用
        conversationId = UUID.randomUUID().toString();


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = findViewById(R.id.recyclerChat);
        etInput = findViewById(R.id.etInput);

        adapter = new ChatAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // 初始欢迎语
        adapter.add(new ChatMsg(ChatMsg.ROLE_AI, "你好，我是自习室助手，有什么可以帮你？"));


        findViewById(R.id.btnSend).setOnClickListener(v -> send());
        ensureLocationPermission();   // 进聊天页申请定位，用于签到

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void send() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        adapter.add(new ChatMsg(ChatMsg.ROLE_USER, text));
        etInput.setText("");
        scrollToBottom();

        // 主动取最新定位，拿到后（哪怕为 null）再发请求
        LocationHelper.getCurrentLocation(this, location -> {
            Double lat = location != null ? location.getLatitude() : null;
            Double lon = location != null ? location.getLongitude() : null;
            doRequest(text, lat, lon);
        });
    }

    // 真正发请求的方法
    private void doRequest(String text, Double lat, Double lon) {
        RetrofitClient.getApiService(this)
                .aiChat(new ChatRequestBody(conversationId, text, lat, lon))
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call,
                                           Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            adapter.add(new ChatMsg(ChatMsg.ROLE_AI, response.body().getData()));
                        } else {
                            String errMsg = (response.body() != null && response.body().getMsg() != null)
                                    ? response.body().getMsg()
                                    : "处理失败，请稍后重试";
                            adapter.add(new ChatMsg(ChatMsg.ROLE_AI, errMsg));
                        }
                        scrollToBottom();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        adapter.add(new ChatMsg(ChatMsg.ROLE_AI,
                                "网络异常：" + t.getMessage()));
                        scrollToBottom();
                    }
                });
    }


    private void scrollToBottom() {
        recycler.post(() -> {
            int pos = adapter.getItemCount() - 1;
            if (pos >= 0) {
                recycler.scrollToPosition(pos);
            }
        });
    }
    // 没有权限就申请，有权限就直接取一次坐标
    private void ensureLocationPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
        }
    }


    // 授权结果回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 无需处理：真正发消息时才通过 LocationHelper 主动定位
    }


}
