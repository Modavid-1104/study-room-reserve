package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ReservationResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.example.campusstudyroombookingsystem.util.LocationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.campusstudyroombookingsystem.model.SignRequest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReservationActivity extends AppCompatActivity {

    private ReservationAdapter adapter;
    private long pendingSignId = -1;          // 待签到的预约id
    private static final int REQ_LOCATION = 1001;
    private static final int REQ_CAMERA = 100;
    private File photoFile;
    private double pendingLat, pendingLon;          // 拍照前先存定位坐标
    private static final int REQ_CAMERA_PERM = 1002;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservation);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter();
        recyclerView.setAdapter(adapter);
        loadData();
        adapter.setOnCancelListener(reservation -> doCancel(reservation.getId()));
        adapter.setOnSignListener(reservation -> {
            Intent intent = new Intent(this, SignMapActivity.class);
            intent.putExtra("reservationId", reservation.getId());
            intent.putExtra("roomName", reservation.getRoomName());
            intent.putExtra("lat", reservation.getLatitude());
            intent.putExtra("lon", reservation.getLongitude());
            intent.putExtra("radius", reservation.getRadius());
            startActivity(intent);
        });

        adapter.setOnReviewListener(reservation -> {
            Intent intent = new Intent(MyReservationActivity.this, CreateReviewActivity.class);
            intent.putExtra("reservationId", reservation.getId());
            startActivity(intent);
        });
        adapter.setOnSignOutListener(reservation -> doSignOut(reservation.getId()));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
    // 点签到：先确认权限，再定位，最后请求
    private void doSign(long id) {
        pendingSignId = id;

        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            // 运行时申请定位权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
            return;
        }
        getLocationAndSign();
    }

    // 用户授权后的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSign();
            } else {
                Toast.makeText(this, "需要定位权限才能签到", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQ_CAMERA_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "需要相机权限才能拍照签到", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // 用 LocationManager 取坐标
    // 取坐标：先读缓存，缓存为空就主动请求一次定位
    private void getLocationAndSign() {
        LocationHelper.getCurrentLocation(this, location -> {
            if (location == null) {
                Toast.makeText(this, "暂时获取不到定位，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }
            // 先把坐标存起来，拍照回来上传时用
            pendingLat = location.getLatitude();
            pendingLon = location.getLongitude();

            // 再检查相机权限（CAMERA 是危险权限，必须运行时申请）
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERM);
            } else {
                takePhoto();
            }
        });
    }




//    // 带坐标调签到接口
//    private void requestSign(long id, double lat, double lon) {
//        RetrofitClient.getApiService(this)
//                .signReservation(id, new SignRequest(lat, lon))
//                .enqueue(new Callback<ApiResponse<Void>>() {
//                    @Override
//                    public void onResponse(Call<ApiResponse<Void>> call,
//                                           Response<ApiResponse<Void>> response) {
//                        if (response.isSuccessful() && response.body() != null
//                                && response.body().isSuccess()) {
//                            Toast.makeText(MyReservationActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
//                            loadData();
//                        } else {
//                            // 透传后端真实原因（距离、定位等）
//                            String msg = response.body() != null && response.body().getMsg() != null
//                                    ? response.body().getMsg() : "签到失败";
//                            Toast.makeText(MyReservationActivity.this, msg, Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
//                        Toast.makeText(MyReservationActivity.this,
//                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "sign_" + System.currentTimeMillis() + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            uploadSignPhoto(photoFile);
        }
    }



    private void doSignOut(long id) {
        RetrofitClient.getApiService(this)
                .signOutReservation(id)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(MyReservationActivity.this, "签退成功", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            String msg = response.body() != null ? response.body().getMsg() : "签退失败";
                            Toast.makeText(MyReservationActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(MyReservationActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }



    private void doCancel(long id) {
        RetrofitClient.getApiService(this)
                .cancelReservation(id)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(MyReservationActivity.this, "已取消", Toast.LENGTH_SHORT).show();
                            loadData();   // 重新拉取，列表状态刷新
                        } else {
                            Toast.makeText(MyReservationActivity.this, "取消失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(MyReservationActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadData() {
        RetrofitClient.getApiService(this)
                .myReservations()
                .enqueue(new Callback<ApiResponse<List<ReservationResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReservationResponse>>> call,
                                           Response<ApiResponse<List<ReservationResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(MyReservationActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReservationResponse>>> call, Throwable t) {
                        Toast.makeText(MyReservationActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();   // 点返回箭头，关闭当前页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void uploadSignPhoto(File photo) {
        RequestBody fileBody = RequestBody.create(photo, MediaType.parse("image/jpeg"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file", photo.getName(), fileBody);

        RequestBody latBody = RequestBody.create(null, String.valueOf(pendingLat));
        RequestBody lonBody = RequestBody.create(null, String.valueOf(pendingLon));

        RetrofitClient.getApiService(this)
                .signWithPhoto(pendingSignId, latBody, lonBody, filePart)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(MyReservationActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            String msg = response.body() != null && response.body().getMsg() != null
                                    ? response.body().getMsg() : "签到失败";
                            Toast.makeText(MyReservationActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(MyReservationActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}
