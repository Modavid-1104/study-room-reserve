package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.PrivateMessageResponse;

import java.util.ArrayList;
import java.util.List;

public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatAdapter.VH> {

    private final List<PrivateMessageResponse> list = new ArrayList<>();
    private final long currentUserId;

    public PrivateChatAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setData(List<PrivateMessageResponse> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    // 自己发的返回 1，对方发的返回 0
    @Override
    public int getItemViewType(int position) {
        return list.get(position).getSenderId() == currentUserId ? 1 : 0;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1
                ? R.layout.item_chat_user
                : R.layout.item_chat_peer;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tvContent.setText(list.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvContent;

        VH(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
