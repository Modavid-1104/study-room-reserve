package com.example.campusstudyroombookingsystem;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.PrivateMessageResponse;
import com.example.campusstudyroombookingsystem.model.SendPrivateMsgRequest;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.example.campusstudyroombookingsystem.util.WebSocketManager;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivateChatActivity extends AppCompatActivity
        implements WebSocketManager.MessageListener {

    private long conversationId;
    private long currentUserId;
    private int role;
    private PrivateChatAdapter adapter;
    private RecyclerView recycler;
    private EditText etInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        conversationId = getIntent().getLongExtra("conversationId", -1);
        String peerName = getIntent().getStringExtra("peerName");

        role = getSharedPreferences("user", MODE_PRIVATE).getInt("role", 0);
        currentUserId = parseCurrentUserId();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(peerName == null ? "在线咨询" : peerName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = findViewById(R.id.recyclerChat);
        etInput = findViewById(R.id.etInput);

        adapter = new PrivateChatAdapter(currentUserId);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnSend).setOnClickListener(v -> send());
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebSocketManager.get().addListener(this);
        loadHistory();   // 每次进页面拉最新
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebSocketManager.get().removeListener(this);
    }

    private void loadHistory() {
        Call<ApiResponse<List<PrivateMessageResponse>>> call = role >= 1
                ? RetrofitClient.getApiService(this).adminPrivateHistory(conversationId)
                : RetrofitClient.getApiService(this).privateHistory(conversationId);

        call.enqueue(new Callback<ApiResponse<List<PrivateMessageResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PrivateMessageResponse>>> call,
                                   Response<ApiResponse<List<PrivateMessageResponse>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.setData(response.body().getData());
                    scrollToBottom();
                    markRead();   // 看完历史，把对方发来的标记已读
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PrivateMessageResponse>>> call,
                                  Throwable t) {
                Toast.makeText(PrivateChatActivity.this,
                        "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void send() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) return;
        etInput.setText("");

        SendPrivateMsgRequest req = new SendPrivateMsgRequest(conversationId, content);
        Call<ApiResponse<Void>> call = role >= 1
                ? RetrofitClient.getApiService(this).adminSendPrivate(req)
                : RetrofitClient.getApiService(this).sendPrivate(req);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    loadHistory();   // 发送成功重新拉取，自己的消息也显示
                } else {
                    Toast.makeText(PrivateChatActivity.this,
                            "发送失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(PrivateChatActivity.this,
                        "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void markRead() {
        Call<ApiResponse<Void>> call = role >= 1
                ? RetrofitClient.getApiService(this).adminMarkPrivateRead(conversationId)
                : RetrofitClient.getApiService(this).markPrivateRead(conversationId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) { }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) { }
        });
    }

    // 收到 WebSocket 推送：只处理本会话的私信
    @Override
    public void onMessage(String text) {
        try {
            JSONObject obj = new JSONObject(text);
            if ("private".equals(obj.optString("type"))
                    && obj.optLong("conversationId") == conversationId) {
                runOnUiThread(this::loadHistory);
            }
        } catch (Exception ignored) {
        }
    }

    // 从 JWT 的 payload 解析当前用户 id（sub 字段）
    private long parseCurrentUserId() {
        try {
            String token = getSharedPreferences("user", MODE_PRIVATE)
                    .getString("token", "");
            String[] parts = token.split("\\.");
            String payload = new String(
                    android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE),
                    "UTF-8");
            return new JSONObject(payload).getLong("sub");
        } catch (Exception e) {
            return -1;
        }
    }

    private void scrollToBottom() {
        recycler.post(() -> {
            int pos = adapter.getItemCount() - 1;
            if (pos >= 0) recycler.scrollToPosition(pos);
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
