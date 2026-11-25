package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ReservationResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationFragment extends Fragment {

    private ReservationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservationAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(this::loadData);

        // 签到 → 地图签到页
        adapter.setOnSignListener(reservation -> {
            Intent intent = new Intent(requireContext(), SignMapActivity.class);
            intent.putExtra("reservationId", reservation.getId());
            intent.putExtra("roomName", reservation.getRoomName());
            intent.putExtra("lat", reservation.getLatitude());
            intent.putExtra("lon", reservation.getLongitude());
            intent.putExtra("radius", reservation.getRadius());
            startActivity(intent);
        });

        // 评价 → 写评价页
        adapter.setOnReviewListener(reservation -> {
            Intent intent = new Intent(requireContext(), CreateReviewActivity.class);
            intent.putExtra("reservationId", reservation.getId());
            startActivity(intent);
        });

        adapter.setOnCancelListener(reservation -> doCancel(reservation.getId()));
        adapter.setOnSignOutListener(reservation -> doSignOut(reservation.getId()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();   // 从签到/评价页返回时刷新状态
    }

    private void doCancel(long id) {
        RetrofitClient.getApiService(requireContext())
                .cancelReservation(id)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "已取消", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(requireContext(), "取消失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void doSignOut(long id) {
        RetrofitClient.getApiService(requireContext())
                .signOutReservation(id)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "签退成功", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            String msg = response.body() != null
                                    ? response.body().getMsg() : "签退失败";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadData() {
        RetrofitClient.getApiService(requireContext())
                .myReservations()
                .enqueue(new Callback<ApiResponse<List<ReservationResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReservationResponse>>> call,
                                           Response<ApiResponse<List<ReservationResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReservationResponse>>> call,
                                          Throwable t) {
                        Toast.makeText(requireContext(), "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
