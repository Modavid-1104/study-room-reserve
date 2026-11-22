package com.example.campusstudyroombookingsystem.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class LocationHelper {

    private static final long TIMEOUT_MS = 3000;   // 主动定位超时时间

    public interface LocationResult {
        void onResult(Location location);          // 拿不到时为 null
    }

    public static void getCurrentLocation(Context context, LocationResult result) {
        // 没权限：直接返回 null
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            result.onResult(null);
            return;
        }

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // 1. 缓存兜底：所有 provider 里时间最新的已知位置
        Location fallback = null;
        for (String p : lm.getProviders(true)) {
            Location l = lm.getLastKnownLocation(p);
            if (l != null && (fallback == null || l.getTime() > fallback.getTime())) {
                fallback = l;
            }
        }

        // 2. 选一个可用 provider（优先 GPS，其次网络）
        String provider = null;
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        if (provider == null) {
            result.onResult(fallback);   // 定位服务没开，只能交缓存
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] settled = {false};

        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (settled[0]) return;
                settled[0] = true;
                handler.removeCallbacksAndMessages(null);
                lm.removeUpdates(this);
                result.onResult(location);     // 主动拿到最新位置，优先使用
            }
            @Override public void onStatusChanged(String p, int s, Bundle b) {}
            @Override public void onProviderEnabled(String p) {}
            @Override public void onProviderDisabled(String p) {}
        };

        // 3. 超时兜底：3 秒内没主动回调，就用缓存
        final Location finalFallback = fallback;   // 复制成不再改变的变量，供 lambda 捕获
        final Runnable timeoutRunnable = () -> {
            if (settled[0]) return;
            settled[0] = true;
            lm.removeUpdates(listener);
            result.onResult(finalFallback);
        };

        handler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        lm.requestLocationUpdates(provider, 0, 0, listener, Looper.getMainLooper());
    }
}
