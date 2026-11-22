package com.example.campusstudyroombookingsystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.RegisterRequest;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etNickname;
    private TextInputEditText etPhone;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etNickname = findViewById(R.id.etNickname);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String username = String.valueOf(etUsername.getText());
        String password = String.valueOf(etPassword.getText());
        String nickname = String.valueOf(etNickname.getText());
        String phone = String.valueOf(etPhone.getText());

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "账号、密码、昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        RetrofitClient.getApiService(this)
                .register(new RegisterRequest(username, password, nickname, phone))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        btnRegister.setEnabled(true);
                        ApiResponse<Void> body = response.body();
                        if (response.isSuccessful() && body != null) {
                            if (body.isSuccess()) {
                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                finish(); // 返回登录页
                            } else {
                                Toast.makeText(RegisterActivity.this, body.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "请求失败，HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
