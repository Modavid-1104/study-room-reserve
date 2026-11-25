package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.MessageResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.example.campusstudyroombookingsystem.util.WebSocketManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity
        implements WebSocketManager.MessageListener {

    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        // 点击未读消息：调接口标记已读，再刷新
        adapter.setListener(msg -> {
            if (msg.getIsRead() != null && msg.getIsRead() == 1) {
                return;   // 已经读过，不用重复请求
            }
            RetrofitClient.getApiService(this)
                    .markRead(msg.getId())
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                               Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()) {
                                loadMessages();   // 刷新，状态变"已读"
                            } else {
                                Toast.makeText(MessageActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(MessageActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loadMessages();
    }

    private void loadMessages() {
        RetrofitClient.getApiService(this)
                .messageList()
                .enqueue(new Callback<ApiResponse<List<MessageResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MessageResponse>>> call,
                                           Response<ApiResponse<List<MessageResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(MessageActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MessageResponse>>> call, Throwable t) {
                        Toast.makeText(MessageActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
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

    @Override
    public void onMessage(String text) {
        loadMessages();   // 收到推送，重新拉取列表
        Toast.makeText(this, "收到新消息", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebSocketManager.get().addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebSocketManager.get().removeListener(this);
    }

}
