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

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    public interface OnSeatClickListener {
        void onSeatClick(SeatResponse seat);
    }

    private List<SeatResponse> list = new ArrayList<>();
    private OnSeatClickListener listener;

    public void setData(List<SeatResponse> data) {
        this.list = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    public void setOnSeatClickListener(OnSeatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        SeatResponse seat = list.get(position);
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

        // 只有空闲(0)才响应点击
        holder.itemView.setOnClickListener(v -> {
            if (seat.getStatus() == 0 && listener != null) {
                listener.onSeatClick(seat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeatNo;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatNo = itemView.findViewById(R.id.tvSeatNo);
        }
    }
}
