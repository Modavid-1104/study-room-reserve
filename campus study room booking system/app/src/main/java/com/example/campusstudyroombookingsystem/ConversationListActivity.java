package com.example.campusstudyroombookingsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ConversationResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationListActivity extends AppCompatActivity {

    private ConversationAdapter adapter;
    private int role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        role = getSharedPreferences("user", MODE_PRIVATE).getInt("role", 0);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter();
        recyclerView.setAdapter(adapter);

        // 点会话 → 聊天页
        adapter.setOnItemClickListener(c -> {
            Intent intent = new Intent(this, PrivateChatActivity.class);
            intent.putExtra("conversationId", c.getId());
            intent.putExtra("peerName", c.getPeerNickname());
            startActivity(intent);
        });
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fab_create);

        if (role >= 1) {
            // 管理员不需要发起咨询，隐藏按钮
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(v -> createConversation());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();   // 从聊天页返回时刷新未读
    }

    private void createConversation() {
        // 后端 getOrCreate：有会话就复用，没有就自动创建
        RetrofitClient.getApiService(this)
                .myConversation()
                .enqueue(new Callback<ApiResponse<ConversationResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ConversationResponse>> call,
                                           Response<ApiResponse<ConversationResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            ConversationResponse c = response.body().getData();
                            Intent intent = new Intent(
                                    ConversationListActivity.this, PrivateChatActivity.class);
                            intent.putExtra("conversationId", c.getId());
                            intent.putExtra("peerName", c.getPeerNickname());
                            startActivity(intent);
                        } else {
                            Toast.makeText(ConversationListActivity.this,
                                    "创建失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ConversationResponse>> call,
                                          Throwable t) {
                        Toast.makeText(ConversationListActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void loadData() {
        // 管理员调管理员接口，学生调学生接口
        Call<ApiResponse<List<ConversationResponse>>> call = role >= 1
                ? RetrofitClient.getApiService(this).adminConversationList()
                : RetrofitClient.getApiService(this).myConversationList();

        call.enqueue(new Callback<ApiResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ConversationResponse>>> call,
                                   Response<ApiResponse<List<ConversationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.setData(response.body().getData());
                } else {
                    Toast.makeText(ConversationListActivity.this,
                            "加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ConversationResponse>>> call,
                                  Throwable t) {
                Toast.makeText(ConversationListActivity.this,
                        "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
