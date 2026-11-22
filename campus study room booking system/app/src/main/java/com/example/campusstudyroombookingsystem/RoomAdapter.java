package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<StudyRoomResponse> list = new ArrayList<>();

    // 点击回调，下一步接详情页用
    public interface OnItemClickListener {
        void onItemClick(StudyRoomResponse room);
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    // 拿到新数据后刷新列表
    public void setData(List<StudyRoomResponse> data) {
        this.list = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        StudyRoomResponse room = list.get(position);
        holder.tvName.setText(room.getName());
        holder.tvAddress.setText(room.getAddress());
        holder.tvTime.setText("开放时间：" + room.getOpenTime() + " - " + room.getCloseTime());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(room);
        });

        int total = room.getTotalSeat() == null ? 0 : room.getTotalSeat();
        int free = room.getFreeSeat() == null ? 0 : room.getFreeSeat();
        holder.tvFree.setText("剩余 " + free + " / 共 " + total + " 座");

        // 占用率 = 已用座位 / 总座位
        int used = total - free;
        int percent = total > 0 ? (used * 100 / total) : 0;
        holder.progressBar.setProgress(percent);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        TextView tvTime;

        TextView tvFree;
        ProgressBar progressBar;


        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvFree = itemView.findViewById(R.id.tvFree);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }
}
