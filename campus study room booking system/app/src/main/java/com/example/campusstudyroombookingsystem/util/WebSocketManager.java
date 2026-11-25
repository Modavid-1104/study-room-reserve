package com.example.campusstudyroombookingsystem.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    // 注意：和 BASE_URL 同源，cpolar 域名变了这里也要改
    private static final String WS_URL = "ws://208a666b.r43.cpolar.top/ws";

    private static volatile WebSocketManager instance;
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean manualClose = false;
    private int retryCount = 0;
    private Context appContext;

    public interface MessageListener {
        void onMessage(String text);
    }

    private final CopyOnWriteArrayList<MessageListener> listeners = new CopyOnWriteArrayList<>();

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)   // 长连接，读不超时
                .build();
    }

    public static WebSocketManager get() {
        if (instance == null) {
            synchronized (WebSocketManager.class) {
                if (instance == null) instance = new WebSocketManager();
            }
        }
        return instance;
    }

    public void connect(Context context) {
        manualClose = false;          // 新增：每次主动连接都重置
        appContext = context.getApplicationContext();
        String token = appContext.getSharedPreferences("user", Context.MODE_PRIVATE)
                .getString("token", "");

        Request request = new Request.Builder()
                .url(WS_URL + "?token=" + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, okhttp3.Response response) {
                retryCount = 0;   // 连接成功，重置重连计数
            }
            @Override
            public void onMessage(WebSocket ws, String text) {
                mainHandler.post(() -> {
                    for (MessageListener l : listeners) l.onMessage(text);
                });
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, okhttp3.Response response) {
                scheduleReconnect();
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                if (!manualClose) scheduleReconnect();
            }
        });
    }

    // 断线自动重连，间隔 1s→2s→4s→8s→15s 封顶
    private void scheduleReconnect() {
        if (manualClose || appContext == null) return;
        retryCount++;
        long delay = Math.min(1000L * (1L << Math.min(retryCount, 4)), 15000L);
        mainHandler.postDelayed(() -> connect(appContext), delay);
    }

    public void disconnect() {
        manualClose = true;
        if (webSocket != null) webSocket.close(1000, "logout");
        listeners.clear();
    }

    public void addListener(MessageListener l) { listeners.addIfAbsent(l); }
    public void removeListener(MessageListener l) { listeners.remove(l); }
}
