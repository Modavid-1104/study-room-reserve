package com.example.campusstudyroombookingsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    android.widget.EditText etContent;
    private TextView tvMediaCount;

    private final List<File> mediaFiles = new ArrayList<>();
    private File currentFile;

    private static final int REQ_PHOTO = 101;
    private static final int REQ_VIDEO = 102;
    private static final int REQ_CAMERA_PERM = 103;
    private boolean wantVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        long reservationId = getIntent().getLongExtra("reservationId", -1);

        ratingBar = findViewById(R.id.ratingBar);
        etContent = findViewById(R.id.etContent);
        tvMediaCount = findViewById(R.id.tvMediaCount);

        findViewById(R.id.btnPhoto).setOnClickListener(v -> pickMedia(false));
        findViewById(R.id.btnVideo).setOnClickListener(v -> pickMedia(true));
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submit(reservationId));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // 先检查相机权限，再决定拍照还是录像
    private void pickMedia(boolean isVideo) {
        wantVideo = isVideo;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERM);
            return;
        }
        launchCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchCamera() {
        Intent intent;
        if (wantVideo) {
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            currentFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "video_" + System.currentTimeMillis() + ".mp4");
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 最长30秒
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            currentFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "photo_" + System.currentTimeMillis() + ".jpg");
        }
        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", currentFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, wantVideo ? REQ_VIDEO : REQ_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQ_PHOTO || requestCode == REQ_VIDEO)
                && resultCode == RESULT_OK
                && currentFile != null && currentFile.exists()) {
            mediaFiles.add(currentFile);
            tvMediaCount.setText("已选 " + mediaFiles.size() + " 个媒体");
        }
    }

    private void submit(long reservationId) {
        int score = (int) ratingBar.getRating();
        String content = String.valueOf(etContent.getText()).trim();

        if (score < 1) {
            Toast.makeText(this, "请至少选择 1 颗星", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty()) {
            Toast.makeText(this, "评价内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaFiles.isEmpty()) {
            Toast.makeText(this, "请至少拍一张照片或一段视频", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody idBody = RequestBody.create(null, String.valueOf(reservationId));
        RequestBody scoreBody = RequestBody.create(null, String.valueOf(score));
        RequestBody contentBody = RequestBody.create(null, content);

        List<MultipartBody.Part> parts = new ArrayList<>();
        for (File f : mediaFiles) {
            String mime = f.getName().endsWith(".mp4") ? "video/mp4" : "image/jpeg";
            RequestBody fb = RequestBody.create(f, MediaType.parse(mime));
            parts.add(MultipartBody.Part.createFormData("files", f.getName(), fb));
        }

        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("正在提交评价，请稍候…");
        pd.setCancelable(false);
        pd.show();

        RetrofitClient.getApiService(this)
                .createReviewMedia(idBody, scoreBody, contentBody, parts)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        pd.dismiss();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(CreateReviewActivity.this, "评价成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = response.body() != null && response.body().getMsg() != null
                                    ? response.body().getMsg() : "提交失败";
                            Toast.makeText(CreateReviewActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        pd.dismiss();
                        Toast.makeText(CreateReviewActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
