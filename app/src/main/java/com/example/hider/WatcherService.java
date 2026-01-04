package com.example.hider;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

public class WatcherService extends Service {
    private static final String CH_ID = "GuardChan";
    private BroadcastReceiver receiver;
    private long startTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTime = System.currentTimeMillis();

        // 1. Создание канала уведомлений
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && nm.getNotificationChannel(CH_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                CH_ID, "Security System", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }

        // 2. Создание уведомления
        Notification notif = new Notification.Builder(this, CH_ID)
                .setContentTitle("Profile Protected")
                .setContentText("Data will be wiped on screen off.")       
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build();

        // 3. Запуск как systemExempted (Android 14+)
        if (Build.VERSION.SDK_INT >= 34) { // Android 14 (Upside Down Cake)
            startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);
        } else {
            // До Android 14 типы не требовались
            startForeground(1, notif);
        }

        // 4. Регистрация ресивера выключения экрана
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Защита от ложного срабатывания при запуске
                    if (System.currentTimeMillis() - startTime < 3000) return;

                    if (intent != null && Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                        if (dpm != null) {
                            try {
                                // Попытка стереть данные (требует прав Profile/Device Owner)
                                dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                            } catch (Exception e) {
                                dpm.wipeData(0);
                            }
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 34) {
                registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(receiver, filter);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
