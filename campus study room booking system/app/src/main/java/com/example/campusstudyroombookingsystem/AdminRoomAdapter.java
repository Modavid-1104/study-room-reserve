package com.example.campusstudyroombookingsystem;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.VH> {

    public interface OnRoomActivityListener{
        void onEdit(StudyRoomResponse room);
        void onDelete(StudyRoomResponse room);
        void onSeat(StudyRoomResponse room);
    }
    private List<StudyRoomResponse> data = new ArrayList<>();
    private OnRoomActivityListener listener;


    public void setData(List<StudyRoomResponse> list) {
        this.data = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public void setOnRoomActivityListener(OnRoomActivityListener l){
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_room, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StudyRoomResponse room = data.get(position);
        holder.tvName.setText(room.getName());
        holder.tvInfo.setText("座位数：" + room.getTotalSeat());

        holder.btnEdit.setOnClickListener(v -> {
            if(listener != null) listener.onEdit(room);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if(listener != null) listener.onDelete(room);
        });
        holder.btnSeat.setOnClickListener(v ->{
            if(listener != null) listener.onSeat(room);
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvInfo;
        View btnEdit;
        View btnDelete;
        View btnSeat;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSeat = itemView.findViewById(R.id.btnSeat);
        }
    }
}
