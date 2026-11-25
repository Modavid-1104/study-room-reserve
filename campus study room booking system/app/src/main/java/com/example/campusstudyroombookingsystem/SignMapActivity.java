package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SignMapActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private TextView tvStatus;

    private long reservationId;
    private double roomLat, roomLon;
    private int radius;
    private double myLat, myLon;

    private static final int REQ_LOC_PERM = 200;
    private static final int REQ_CAMERA = 100;
    private static final int REQ_CAMERA_PERM = 201;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        setContentView(R.layout.activity_sign_map);

        reservationId = getIntent().getLongExtra("reservationId", -1);
        roomLat = getIntent().getDoubleExtra("lat", 0);
        roomLon = getIntent().getDoubleExtra("lon", 0);
        radius = getIntent().getIntExtra("radius", 100);
        String roomName = getIntent().getStringExtra("roomName");

        tvStatus = findViewById(R.id.tvStatus);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        MyLocationStyle style = new MyLocationStyle();
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        aMap.setMyLocationStyle(style);
        aMap.setMyLocationEnabled(true);

        aMap.addMarker(new MarkerOptions()
                .position(new LatLng(roomLat, roomLon)).title(roomName));

        aMap.addCircle(new CircleOptions()
                .center(new LatLng(roomLat, roomLon))
                .radius(radius)
                .fillColor(0x30000000)
                .strokeColor(0x66000000)
                .strokeWidth(2));

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(roomLat, roomLon), 17f));

        checkLocationPermission();

        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> onConfirm());
    }

    // 点确认：先确认定位成功，再申请相机权限 → 拍照
    private void onConfirm() {
        if (myLat == 0 && myLon == 0) {
            Toast.makeText(this, "还没定位成功，稍等", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERM);
        } else {
            takePhoto();
        }
    }

    // 调起相机
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "sign_" + System.currentTimeMillis() + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    // 拍照回来后上传
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d("SignMap", "onActivityResult request=" + requestCode
                + " result=" + resultCode);
        // 不依赖 resultCode，照片文件真实存在就上传
        if (requestCode == REQ_CAMERA && photoFile != null
                && photoFile.exists() && photoFile.length() > 0) {
            uploadSignPhoto();
        }
    }


    // 高德坐标 + 照片 Multipart 上传
    private void uploadSignPhoto() {
        RequestBody fileBody = RequestBody.create(photoFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file", photoFile.getName(), fileBody);
        RequestBody latBody = RequestBody.create(null, String.valueOf(myLat));
        RequestBody lonBody = RequestBody.create(null, String.valueOf(myLon));

        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("正在上传签到照片，请稍候…");
        pd.setCancelable(false);
        pd.show();


        RetrofitClient.getApiService(this)
                .signWithPhoto(reservationId, latBody, lonBody, filePart)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        pd.dismiss();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(SignMapActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = response.body() != null && response.body().getMsg() != null
                                    ? response.body().getMsg() : "签到失败";
                            new androidx.appcompat.app.AlertDialog.Builder(SignMapActivity.this)
                                    .setTitle("签到失败")
                                    .setMessage(msg)
                                    .setPositiveButton("我知道了", null)
                                    .show();
                        }

                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        pd.dismiss();
                        Toast.makeText(SignMapActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void startLocation() {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            //高德定位
            //option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //GPS
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            option.setInterval(2000);
            option.setNeedAddress(true);
            locationClient.setLocationOption(option);
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation loc) {
                    if (loc != null && loc.getErrorCode() == 0) {
                        myLat = loc.getLatitude();
                        myLon = loc.getLongitude();
                        tvStatus.setText("定位成功，确认你在圆圈范围内后点签到");
                    } else {
                        tvStatus.setText("定位失败：" + (loc != null ? loc.getErrorInfo() : ""));
                    }
                }
            });
            locationClient.startLocation();
        } catch (Exception e) {
            tvStatus.setText("定位初始化失败：" + e.getMessage());
        }
    }

    private void checkLocationPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            startLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC_PERM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                tvStatus.setText("需要定位权限才能签到，请到系统设置中开启");
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

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationClient != null) locationClient.onDestroy();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
