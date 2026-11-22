package com.example.campusstudyroombookingsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.CreateReservationRequest;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateReservationActivity extends AppCompatActivity {

    private Calendar startCalendar;
    private Calendar endCalendar;

    // 显示用、提交用两种格式
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private TextView tvStart;
    private TextView tvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reservation);

        long seatId = getIntent().getLongExtra("seatId", -1);
        String seatNo = getIntent().getStringExtra("seatNo");

        TextView tvSeat = findViewById(R.id.tvSeat);
        tvSeat.setText("预约座位：" + seatNo);

        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnEnd = findViewById(R.id.btnEnd);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnStart.setOnClickListener(v -> pickDateTime(true));
        btnEnd.setOnClickListener(v -> pickDateTime(false));

        btnSubmit.setOnClickListener(v -> submit(seatId));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    // 先选日期，再选时间，结果存进对应 Calendar
    private void pickDateTime(boolean isStart) {
        Calendar base = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            TimePickerDialog timeDialog = new TimePickerDialog(this, (timeView, hour, minute) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(year, month, day, hour, minute, 0);
                picked.set(Calendar.MILLISECOND, 0);

                if (isStart) {
                    startCalendar = picked;
                    tvStart.setText(displayFormat.format(picked.getTime()));
                } else {
                    endCalendar = picked;
                    tvEnd.setText(displayFormat.format(picked.getTime()));
                }
            }, base.get(Calendar.HOUR_OF_DAY), base.get(Calendar.MINUTE), true);
            timeDialog.show();
        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void submit(long seatId) {
        if (startCalendar == null || endCalendar == null) {
            Toast.makeText(this, "请选择开始和结束时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!endCalendar.after(startCalendar)) {
            Toast.makeText(this, "结束时间必须晚于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateReservationRequest request = new CreateReservationRequest(
                seatId,
                isoFormat.format(startCalendar.getTime()),
                isoFormat.format(endCalendar.getTime()));

        RetrofitClient.getApiService(this)
                .createReservation(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(CreateReservationActivity.this, "预约成功", Toast.LENGTH_SHORT).show();
                            finish();   // 关闭当前页，回到座位页
                        } else {
                            String msg = response.body() != null ? response.body().getMsg() : "预约失败";
                            Toast.makeText(CreateReservationActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(CreateReservationActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
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

}
