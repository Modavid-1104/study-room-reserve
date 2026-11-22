package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.PageResponse;
import com.example.campusstudyroombookingsystem.model.RoomRequest;
import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRoomActivity extends AppCompatActivity {

    private AdminRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_room);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRoomAdapter();
        recyclerView.setAdapter(adapter);

        // 新增按钮下一轮接表单，先占位
        findViewById(R.id.btnAdd).setOnClickListener(v -> showFormDialog(null));

        adapter.setOnRoomActivityListener(new AdminRoomAdapter.OnRoomActivityListener(){
            @Override
            public void onEdit(StudyRoomResponse room){
                showFormDialog(room);
            }
            @Override
            public void onDelete(StudyRoomResponse room){
                confirmDelete(room);
            }
            @Override
            public void onSeat(StudyRoomResponse room) {
                Intent intent;
                intent = new Intent(AdminRoomActivity.this, AdminSeatActivity.class);
                intent.putExtra("roomId", room.getId());
                startActivity(intent);
            }
        });

        findViewById(R.id.btnAdminMessage).setOnClickListener(v ->
                startActivity(new Intent(AdminRoomActivity.this, AdminMessageActivity.class)));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示左侧返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadRooms();
    }



    private void showFormDialog(StudyRoomResponse existing){
        boolean isEdit = existing != null;
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad,pad,pad,0);

        TextInputLayout tilName = makeInput("自习室名称", InputType.TYPE_CLASS_TEXT);
        TextInputLayout tilAddress = makeInput("地址", InputType.TYPE_CLASS_TEXT);
        TextInputLayout tilSeat = makeInput("总座位数", InputType.TYPE_CLASS_NUMBER);
        TextInputLayout tilOpen = makeInput("开放时间，如08:00", InputType.TYPE_CLASS_TEXT);
        TextInputLayout tilClose = makeInput("关闭时间，如22:00", InputType.TYPE_CLASS_TEXT);

        container.addView(tilName);
        container.addView(tilAddress);
        container.addView(tilSeat);
        container.addView(tilOpen);
        container.addView(tilClose);

        if(isEdit){
            tilName.getEditText().setText(existing.getName());
            tilAddress.getEditText().setText(existing.getAddress());
            tilSeat.getEditText().setText(String.valueOf(existing.getTotalSeat()));
            tilOpen.getEditText().setText(existing.getOpenTime());
            tilClose.getEditText().setText(existing.getCloseTime());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "编辑自习室" : "新增自习室")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = tilName.getEditText().getText().toString().trim();
                    String seatText = tilSeat.getEditText().getText().toString().trim();
                    if (name.isEmpty() || seatText.isEmpty()) {
                        Toast.makeText(this, "名称和座位数必填", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RoomRequest req = new RoomRequest();
                    req.setName(name);
                    req.setAddress(tilAddress.getEditText().getText().toString().trim());
                    req.setTotalSeat(Integer.parseInt(seatText));
                    req.setOpenTime(tilOpen.getEditText().getText().toString().trim());
                    req.setCloseTime(tilClose.getEditText().getText().toString().trim());

                    if (isEdit) {
                        req.setId(existing.getId());
                        submitUpdate(req);
                    } else {
                        submitAdd(req);
                    }
                })
                .show();
    }


    private void confirmDelete(StudyRoomResponse room) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定删除「" + room.getName() + "」吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> submitDelete(room.getId()))
                .show();
    }

    // 生成带 OutlinedBox 风格的 TextInputLayout
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


    private void submitAdd(RoomRequest req) {
        RetrofitClient.getApiService(this).addRoom(req)
                .enqueue(simpleCallback("新增成功"));
    }

    private void submitUpdate(RoomRequest req) {
        RetrofitClient.getApiService(this).updateRoom(req)
                .enqueue(simpleCallback("修改成功"));
    }

    private void submitDelete(long id) {
        RetrofitClient.getApiService(this).deleteRoom(id)
                .enqueue(simpleCallback("删除成功"));
    }

    // 三个写操作共用的回调
    private Callback<ApiResponse<Void>> simpleCallback(String okMsg) {
        return new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminRoomActivity.this, okMsg, Toast.LENGTH_SHORT).show();
                    loadRooms();   // 操作完刷新列表
                } else {
                    String msg = response.body() != null ? response.body().getMsg() : "操作失败";
                    Toast.makeText(AdminRoomActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminRoomActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void loadRooms() {
        RetrofitClient.getApiService(this)
                .roomPage(1, 50)
                .enqueue(new Callback<ApiResponse<PageResponse<StudyRoomResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call,
                                           Response<ApiResponse<PageResponse<StudyRoomResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PageResponse<StudyRoomResponse> page = response.body().getData();
                            if (page != null) {
                                adapter.setData(page.getRecords());
                            }
                        } else {
                            Toast.makeText(AdminRoomActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<StudyRoomResponse>>> call, Throwable t) {
                        Toast.makeText(AdminRoomActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
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
