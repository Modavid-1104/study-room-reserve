package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ChatMsg;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> {

    private final List<ChatMsg> list = new ArrayList<>();

    // 新增一条消息
    public void add(ChatMsg msg) {
        list.add(msg);
        notifyItemInserted(list.size() - 1);
    }

    // 根据角色返回不同布局类型
    @Override
    public int getItemViewType(int position) {
        return list.get(position).getRole();
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == ChatMsg.ROLE_USER
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH holder, int position) {
        holder.tvContent.setText(list.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ChatVH extends RecyclerView.ViewHolder {
        TextView tvContent;

        ChatVH(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
