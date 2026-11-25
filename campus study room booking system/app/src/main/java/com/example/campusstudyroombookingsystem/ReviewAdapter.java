package com.example.campusstudyroombookingsystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campusstudyroombookingsystem.model.ReviewMediaResponse;
import com.example.campusstudyroombookingsystem.model.ReviewResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<ReviewResponse> list = new ArrayList<>();

    public void setData(List<ReviewResponse> data) {
        this.list = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewResponse r = list.get(position);
        holder.ratingBar.setIsIndicator(true);
        holder.ratingBar.setRating(r.getScore());
        holder.tvContent.setText(r.getContent());
        holder.tvTime.setText(formatTime(r.getCreateTime()));

        // 动态加载媒体
        holder.mediaContainer.removeAllViews();
        List<ReviewMediaResponse> media = r.getMediaList();
        if (media == null) return;

        Context context = holder.itemView.getContext();
        for (ReviewMediaResponse m : media) {
            String fullUrl = RetrofitClient.BASE_URL + "/upload/" + m.getUrl();

            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 200));
            params.topMargin = dp(context, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 图片直接加载；视频 Glide 尝试加载首帧
            Glide.with(context).load(fullUrl).centerCrop().into(imageView);

            if (m.getType() != null && m.getType() == 2) {
                // 视频：点击用系统播放器打开
                imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(fullUrl), "video/*");
                    context.startActivity(intent);
                });
            }
            holder.mediaContainer.addView(imageView);
        }
    }

    private int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
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

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView tvContent;
        TextView tvTime;
        LinearLayout mediaContainer;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            mediaContainer = itemView.findViewById(R.id.mediaContainer);
        }
    }
}
