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
import com.example.campusstudyroombookingsystem.model.QuickReservationRequest;
import com.example.campusstudyroombookingsystem.model.QuickResultResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickReserveActivity extends AppCompatActivity {

    private Calendar startCalendar;
    private Calendar endCalendar;

    private final SimpleDateFormat displayFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat isoFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private TextView tvStart;
    private TextView tvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_reserve);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnEnd = findViewById(R.id.btnEnd);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnStart.setOnClickListener(v -> pickDateTime(true));
        btnEnd.setOnClickListener(v -> pickDateTime(false));
        btnSubmit.setOnClickListener(v -> submit());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    private void submit() {
        if (startCalendar == null || endCalendar == null) {
            Toast.makeText(this, "请选择开始和结束时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!endCalendar.after(startCalendar)) {
            Toast.makeText(this, "结束时间必须晚于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        // roomId 传 null，全局找
        QuickReservationRequest request = new QuickReservationRequest(
                null,
                isoFormat.format(startCalendar.getTime()),
                isoFormat.format(endCalendar.getTime()));

        RetrofitClient.getApiService(this)
                .quickReserve(request)
                .enqueue(new Callback<ApiResponse<QuickResultResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<QuickResultResponse>> call,
                                           Response<ApiResponse<QuickResultResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            QuickResultResponse data = response.body().getData();
                            if (data != null) {
                                Toast.makeText(QuickReserveActivity.this,
                                        "已为你预约：" + data.getRoomName() + " " + data.getSeatNo() + "号座",
                                        Toast.LENGTH_LONG).show();
                            }
                            finish();
                        } else {
                            String msg = response.body() != null ? response.body().getMsg() : "找座失败";
                            Toast.makeText(QuickReserveActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<QuickResultResponse>> call, Throwable t) {
                        Toast.makeText(QuickReserveActivity.this,
                                "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
