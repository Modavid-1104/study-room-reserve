package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.MessageResponse;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    public interface OnMessageClickListener {
        void onClick(MessageResponse msg);
    }

    private List<MessageResponse> data = new ArrayList<>();
    private OnMessageClickListener listener;

    public void setData(List<MessageResponse> list) {
        this.data = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public void setListener(OnMessageClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageResponse msg = data.get(position);
        holder.tvTitle.setText(msg.getTitle());
        holder.tvContent.setText(msg.getContent());
        // 未读加粗提示，已读显示已读
        holder.tvStatus.setText(msg.getIsRead() != null && msg.getIsRead() == 1
                ? "已读" : "未读");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(msg);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
