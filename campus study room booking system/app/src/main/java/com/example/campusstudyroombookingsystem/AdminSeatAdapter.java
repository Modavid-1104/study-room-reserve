package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.SeatResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminSeatAdapter extends RecyclerView.Adapter<AdminSeatAdapter.VH> {

    public interface OnAdminSeatClickListener {
        void onClick(SeatResponse seat);
    }

    private List<SeatResponse> data = new ArrayList<>();
    private OnAdminSeatClickListener listener;

    public void setData(List<SeatResponse> list) {
        this.data = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public void setListener(OnAdminSeatClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SeatResponse seat = data.get(position);
        holder.tvSeatNo.setText(seat.getSeatNo());

        int colorRes;
        if (seat.getStatus() == 1) {
            colorRes = R.color.seat_booked;
        } else if (seat.getStatus() == 2) {
            colorRes = R.color.seat_disabled;
        } else {
            colorRes = R.color.seat_free;
        }
        holder.tvSeatNo.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes));

        // 管理端：所有状态都能点，下一轮在这里弹操作菜单
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(seat);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSeatNo;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSeatNo = itemView.findViewById(R.id.tvSeatNo);
        }
    }
}
