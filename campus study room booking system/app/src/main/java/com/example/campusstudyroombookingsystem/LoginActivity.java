package com.example.campusstudyroombookingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.LoginRequest;
import com.example.campusstudyroombookingsystem.model.TokenResponse;
import com.example.campusstudyroombookingsystem.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String savedToken = sp.getString("token", "");
        if (!TextUtils.isEmpty(savedToken)) {
            // 本地已有登录凭证，跳过登录页，直接进主页
            startActivity(new Intent(this, RoomListActivity.class));
            finish();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvToRegister = findViewById(R.id.tvToRegister);

        tvToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = String.valueOf(etUsername.getText());
        String password = String.valueOf(etPassword.getText());

        // 前端先做一层非空校验
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        // enqueue 自动在子线程发请求，回调切回主线程，可直接更新 UI
        RetrofitClient.getApiService(LoginActivity.this)
                .login(new LoginRequest(username, password))
                .enqueue(new Callback<ApiResponse<TokenResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<TokenResponse>> call,
                                           Response<ApiResponse<TokenResponse>> response) {
                        btnLogin.setEnabled(true);
                        ApiResponse<TokenResponse> body = response.body();

                        // 先看 HTTP 层，再看业务 code
                        if (response.isSuccessful() && body != null) {
                            if (body.isSuccess()) {
                                // 保存 token，后续接口要带着它
                                String token = body.getData() != null ? body.getData().getToken() : "";
                                Integer role = body.getData() != null ? body.getData().getRole() : 0;
                                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                                sp.edit()
                                        .putString("token", token)
                                        .putInt("role", role == null ? 0 : role)
                                        .apply();


                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, RoomListActivity.class));
                                finish();
                            } else {
                                // 账号不存在 / 密码错误等，后端返回的提示
                                Toast.makeText(LoginActivity.this, body.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "请求失败，HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<TokenResponse>> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        // 后端没启动、地址不对、手机连不上电脑都会走这里
                        Toast.makeText(LoginActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
