package com.example.campusstudyroombookingsystem;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campusstudyroombookingsystem.model.ConversationResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter
        extends RecyclerView.Adapter<ConversationAdapter.VH> {

    public interface OnItemClick {
        void onClick(ConversationResponse c);
    }

    private List<ConversationResponse> list = new ArrayList<>();
    private OnItemClick listener;

    public void setData(List<ConversationResponse> data) {
        this.list = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClick l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ConversationResponse c = list.get(position);

        h.tvNickname.setText(c.getPeerNickname());
        h.tvLast.setText(TextUtils.isEmpty(c.getLastMessage())
                ? "暂无消息" : c.getLastMessage());
        h.tvTime.setText(formatTime(c.getLastTime()));

        // 未读角标
        int unread = c.getUnread() == null ? 0 : c.getUnread();
        if (unread > 0) {
            h.tvUnread.setVisibility(View.VISIBLE);
            h.tvUnread.setText(String.valueOf(unread));
        } else {
            h.tvUnread.setVisibility(View.GONE);
        }

        // 头像：有路径才加载，否则用默认图标
        if (!TextUtils.isEmpty(c.getPeerAvatar())) {
            String url = RetrofitClient.BASE_URL + c.getPeerAvatar();
            Glide.with(h.itemView.getContext())
                    .load(url)
                    .circleCrop()
                    .into(h.ivAvatar);
        }
    }

    private String formatTime(String t) {
        if (TextUtils.isEmpty(t)) return "";
        String s = t.replace("T", " ");
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class VH extends RecyclerView.ViewHolder {
        com.google.android.material.imageview.ShapeableImageView ivAvatar;
        TextView tvNickname, tvLast, tvTime, tvUnread;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvLast = itemView.findViewById(R.id.tv_last);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnread = itemView.findViewById(R.id.tv_unread);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onClick(list.get(pos));
                }
            });
        }
    }
}
