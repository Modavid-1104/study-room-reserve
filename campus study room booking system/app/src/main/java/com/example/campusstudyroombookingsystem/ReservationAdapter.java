package com.example.campusstudyroombookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ReservationResponse;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnSignListener {
        void onSign(ReservationResponse reservation);
    }
    public interface OnCancelListener {
        void onCancel(ReservationResponse reservation);
    }
    public interface OnReviewListener {
        void onReview(ReservationResponse reservation);
    }
    public interface OnSignOutListener {
        void onSignOut(ReservationResponse reservation);
    }


    private List<ReservationResponse> list = new ArrayList<>();
    private OnSignListener signListener;
    private OnCancelListener cancelListener;
    private OnReviewListener reviewListener;
    private OnSignOutListener signOutListener;


    public void setData(List<ReservationResponse> data) {
        this.list = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }
    public void setOnSignListener(OnSignListener l) { this.signListener = l; }
    public void setOnCancelListener(OnCancelListener l) { this.cancelListener = l; }
    public void setOnReviewListener(OnReviewListener l) { this.reviewListener = l; }
    public void setOnSignOutListener(OnSignOutListener l) { this.signOutListener = l; }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        ReservationResponse r = list.get(position);

        holder.tvRoomSeat.setText(r.getRoomName() + " · " + r.getSeatNo());
        holder.tvTime.setText("时间：" + formatTime(r.getStartTime()) + " - " + formatTime(r.getEndTime()));
        holder.tvStatus.setText("状态：" + statusText(r.getStatus()));

        // 先全部隐藏，再按状态决定显示哪些
        holder.btnSign.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReview.setVisibility(View.GONE);
        holder.btnSignOut.setVisibility(View.GONE);

        int status = r.getStatus() == null ? -1 : r.getStatus();
        if (status == 0) {
            // 待签到：可签到、可取消
            holder.btnSign.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnSign.setOnClickListener(v -> {
                if (signListener != null) signListener.onSign(r);
            });
            holder.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null) cancelListener.onCancel(r);
            });
        } else if (status == 1) {
            // 已签到：可签退
            holder.btnSignOut.setVisibility(View.VISIBLE);
            holder.btnSignOut.setOnClickListener(v -> {
                if (signOutListener != null) signOutListener.onSignOut(r);
            });
        } else if (status == 4) {
            // 已签退：可评价
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                if (reviewListener != null) reviewListener.onReview(r);
            });
        }

    }

    private String statusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待签到";
            case 1: return "已签到";
            case 2: return "已取消";
            case 3: return "超时释放";
            case 4: return "已完成";
            default: return "未知";
        }
    }

    private String formatTime(String t) {
        if (t == null) return "";
        String s = t.replace("T", " ");
        if (s.length() >= 16) return s.substring(0, 16);
        return s;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomSeat;
        TextView tvTime;
        TextView tvStatus;
        Button btnSign;
        Button btnCancel;
        Button btnReview;
        Button btnSignOut;


        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomSeat = itemView.findViewById(R.id.tvRoomSeat);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnSign = itemView.findViewById(R.id.btnSign);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnSignOut = itemView.findViewById(R.id.btnSignOut);
        }
    }
}
