package com.example.campusstudyroombookingsystem.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.example.campusstudyroombookingsystem.LoginActivity;

public class RetrofitClient {

    private static final String BASE_URL = "http://208a666b.r43.cpolar.top";

    private static volatile ApiService apiService;

    // 防止多个请求同时 401 时重复跳转
    private static volatile boolean isRedirecting = false;

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            synchronized (RetrofitClient.class) {
                if (apiService == null) {
                    Context appContext = context.getApplicationContext();

                    // 1. token 拦截器：请求前自动加 token
                    Interceptor tokenInterceptor = chain -> {
                        SharedPreferences sp = appContext.getSharedPreferences("user", Context.MODE_PRIVATE);
                        String token = sp.getString("token", "");
                        Request request = chain.request().newBuilder()
                                .header("token", token)
                                .build();
                        return chain.proceed(request);
                    };

                    // 2. 鉴权拦截器：响应里 code=401 就清登录态并跳登录页
                    Interceptor authInterceptor = chain -> {
                        Response response = chain.proceed(chain.request());
                        ResponseBody body = response.body();

                        if (body != null && body.contentType() != null
                                && body.contentType().toString().contains("application/json")) {
                            // 先把 body 读成字符串（流只能读一次）
                            String bodyString = body.string();
                            MediaType mediaType = body.contentType();

                            try {
                                JsonObject json = JsonParser.parseString(bodyString).getAsJsonObject();
                                if (json.has("code") && json.get("code").getAsInt() == 401) {
                                    handleTokenExpired(appContext);
                                }
                            } catch (Exception ignored) {
                                // 非标准结构，忽略，按原响应处理
                            }

                            // 用读取到的字符串重建 body，保证下游能正常拿到数据
                            ResponseBody newBody = ResponseBody.create(mediaType, bodyString);
                            return response.newBuilder().body(newBody).build();
                        }
                        return response;
                    };

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(tokenInterceptor)
                            .addInterceptor(authInterceptor)   // 响应统一鉴权
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build();

                    Gson gson = new GsonBuilder().create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    // token 过期的统一处理
    private static void handleTokenExpired(Context appContext) {
        if (isRedirecting) return;
        isRedirecting = true;

        // 清除本地登录态
        appContext.getSharedPreferences("user", Context.MODE_PRIVATE)
                .edit().clear().apply();

        // 跳转必须在主线程
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(appContext, "登录已过期，请重新登录", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(appContext, LoginActivity.class);
            // 非 Activity 上下文启动要加 NEW_TASK；CLEAR_TASK 清空旧页面栈
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);

            isRedirecting = false;
        });
    }
}
