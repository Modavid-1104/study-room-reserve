package com.example.campusstudyroombookingsystem.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmHelper {

    public static void setReminder(Context context, long startMillis,
                                   String roomName, int requestCode) {
        long triggerAt = startMillis - 15 * 60 * 1000L;   // 开始前15分钟

        long now = startMillis;//实时还剩多少分钟开始

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, "自习室预约提醒");
        intent.putExtra(AlarmReceiver.EXTRA_CONTENT,
                "你预约的 " + roomName + " 还有15分钟开始，请准时签到");

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Android 12+ 若没拿到精确闹钟权限，退化为非精确，保证至少能响
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}
