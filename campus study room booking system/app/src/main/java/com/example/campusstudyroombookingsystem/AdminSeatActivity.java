package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.SeatBatchRequest;
import com.example.campusstudyroombookingsystem.model.SeatRequest;
import com.example.campusstudyroombookingsystem.model.SeatResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSeatActivity extends AppCompatActivity {

    private AdminSeatAdapter adapter;
    private long roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seat);

        roomId = getIntent().getLongExtra("roomId", -1);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new AdminSeatAdapter();
        recyclerView.setAdapter(adapter);

        // 点击座位下一轮接操作菜单，先占位
        adapter.setListener(seat -> showSeatActions(seat));

        findViewById(R.id.btnBatch).setOnClickListener(v -> showBatchDialog());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadSeats();
    }

    // 座位操作菜单
    private void showSeatActions(SeatResponse seat) {
        // 禁用状态显示"启用"，其他显示"禁用"
        String statusAction = seat.getStatus() == 2 ? "启用座位" : "禁用座位";
        String[] items = {"编辑信息", statusAction};

        new AlertDialog.Builder(this)
                .setTitle("座位 " + seat.getSeatNo())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(seat);
                    } else {
                        toggleStatus(seat);
                    }
                })
                .show();
    }

    // 编辑座位号、备注
    private void showEditDialog(SeatResponse seat) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        TextInputLayout tilNo = makeInput("座位号", InputType.TYPE_CLASS_TEXT);
        TextInputLayout tilRemark = makeInput("备注", InputType.TYPE_CLASS_TEXT);

        tilNo.getEditText().setText(seat.getSeatNo());
        if (seat.getRemark() != null) {
            tilRemark.getEditText().setText(seat.getRemark());
        }

        container.addView(tilNo);
        container.addView(tilRemark);

        new AlertDialog.Builder(this)
                .setTitle("编辑座位")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String no = tilNo.getEditText().getText().toString().trim();
                    if (no.isEmpty()) {
                        Toast.makeText(this, "座位号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SeatRequest req = new SeatRequest();
                    req.setId(seat.getId());
                    req.setSeatNo(no);
                    req.setRemark(tilRemark.getEditText().getText().toString().trim());

                    RetrofitClient.getApiService(this).updateSeat(req)
                            .enqueue(refreshCallback("修改成功"));
                })
                .show();
    }

    // 在 0(启用) 和 2(禁用) 之间切换
    private void toggleStatus(SeatResponse seat) {
        int newStatus = seat.getStatus() == 2 ? 0 : 2;
        RetrofitClient.getApiService(this)
                .updateSeatStatus(seat.getId(), newStatus)
                .enqueue(refreshCallback("状态已更新"));
    }

    private Callback<ApiResponse<Void>> refreshCallback(String okMsg) {
        return new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminSeatActivity.this, okMsg, Toast.LENGTH_SHORT).show();
                    loadSeats();
                } else {
                    String msg = response.body() != null ? response.body().getMsg() : "操作失败";
                    Toast.makeText(AdminSeatActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminSeatActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void showBatchDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        TextInputLayout tilCount = makeInput("生成数量", InputType.TYPE_CLASS_NUMBER);
        TextInputLayout tilPrefix = makeInput("座位号前缀，如 C", InputType.TYPE_CLASS_TEXT);

        container.addView(tilCount);
        container.addView(tilPrefix);

        new AlertDialog.Builder(this)
                .setTitle("批量生成座位")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("生成", (d, w) -> {
                    String countText = tilCount.getEditText().getText().toString().trim();
                    if (countText.isEmpty()) {
                        Toast.makeText(this, "请输入数量", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String prefix = tilPrefix.getEditText().getText().toString().trim();
                    SeatBatchRequest req = new SeatBatchRequest(
                            roomId, Integer.parseInt(countText), prefix);
                    RetrofitClient.getApiService(this).batchSeat(req)
                            .enqueue(refreshCallback("生成成功"));
                })
                .show();
    }


    private void loadSeats() {
        RetrofitClient.getApiService(this)
                .seatList(roomId)
                .enqueue(new Callback<ApiResponse<List<SeatResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<SeatResponse>>> call,
                                           Response<ApiResponse<List<SeatResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.setData(response.body().getData());
                        } else {
                            Toast.makeText(AdminSeatActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<SeatResponse>>> call, Throwable t) {
                        Toast.makeText(AdminSeatActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private TextInputLayout makeInput(String hint, int inputType) {
        TextInputLayout til = new TextInputLayout(this);
        til.setHint(hint);
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(inputType);
        til.addView(et);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        til.setLayoutParams(lp);
        return til;
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
