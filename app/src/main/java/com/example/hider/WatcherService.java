package com.example.hider;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class WatcherService extends Service {
    private static final String CHANNEL_ID = "SecureGuardChannel";
    private static final int NOTIF_ID = 1;
    private boolean isReceiverRegistered = false;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                Log.d("HIDER", "Screen OFF detected! Wiping...");
                wipeEverything(context);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Создаем канал один раз при создании сервиса
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Secure Guard", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. Сначала строим уведомление
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Profile protection is active")
                .setContentText("Data will be wiped on screen off.")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true) // Делаем его несмахиваемым заранее
                .build();

        // 2. ШАГ 1: Показываем как обычное (подушка безопасности)
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, notification);

        // 3. ШАГ 2: Прыгаем в Foreground (залог приоритета)
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                // 1073741824 это specialUse (0x40000000)
                startForeground(NOTIF_ID, notification, 1073741824);
            } else if (Build.VERSION.SDK_INT >= 26) {
                startForeground(NOTIF_ID, notification);
            }
        } catch (Exception e) {
            Log.e("HIDER", "Foreground failed, staying as background with notification");
        }

        // 4. Регистрируем ресивер, если еще не сделали этого
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 34) {
                // В Android 14 это ОБЯЗАТЕЛЬНО, иначе упадет
                registerReceiver(screenReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(screenReceiver, filter);
            }
            isReceiverRegistered = true;
        }

        return START_STICKY;
    }

    private void wipeEverything(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            try {
                // Пытаемся жахнуть всё
                dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE | DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
            } catch (Exception e) {
                // Если не хватает прав на внешку, сносим хотя бы профиль
                dpm.wipeData(0);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (isReceiverRegistered) {
            unregisterReceiver(screenReceiver);
            isReceiverRegistered = false;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
