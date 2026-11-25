package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.MessageResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.example.campusstudyroombookingsystem.util.WebSocketManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment
        implements WebSocketManager.MessageListener {

    private MessageAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(this::loadMessages);

        view.findViewById(R.id.entry_consult).setOnClickListener(v ->
                startActivity(new android.content.Intent(
                        requireContext(), ConversationListActivity.class)));


        // 点击未读消息：标记已读再刷新
        adapter.setListener(msg -> {
            if (msg.getIsRead() != null && msg.getIsRead() == 1) {
                return;
            }
            RetrofitClient.getApiService(requireContext())
                    .markRead(msg.getId())
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                               Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()) {
                                loadMessages();
                            } else {
                                Toast.makeText(requireContext(), "操作失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void loadMessages() {
        RetrofitClient.getApiService(requireContext())
                .messageList()
                .enqueue(new Callback<ApiResponse<List<MessageResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MessageResponse>>> call,
                                           Response<ApiResponse<List<MessageResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MessageResponse>>> call,
                                          Throwable t) {
                        Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onMessage(String text) {
        loadMessages();
        Toast.makeText(requireContext(), "收到新消息", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        WebSocketManager.get().addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        WebSocketManager.get().removeListener(this);
    }
}
