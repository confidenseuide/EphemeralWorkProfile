package ephemeralwp.safespace;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.hardware.usb.UsbManager;

public class WatcherService extends Service {
    private static final String CH_ID = "GuardChan";
    private BroadcastReceiver receiver;
    private BroadcastReceiver usbReceiver;
    private long startTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTime = System.currentTimeMillis();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && nm.getNotificationChannel(CH_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                CH_ID, "Security System", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }

        
        Notification notif = new Notification.Builder(this, CH_ID)
                .setContentTitle("Profile Protected")
                .setContentText("Data will be wiped on screen off.")       
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);
        } else {
            startForeground(1, notif);
        }

        if (usbReceiver == null) {
        usbReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (isInitialStickyBroadcast()) return;
				wipe.wipe(WatcherService.this);
			}
		};
        if (Build.VERSION.SDK_INT >= 34) {
		registerReceiver(usbReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"),Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(usbReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE");
        }
        }

       if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isInitialStickyBroadcast()) return;
                    if (intent != null) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) || UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                        wipe.wipe(WatcherService.this);
                    }
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
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
            receiver = null;
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
        }
        if (usbReceiver != null) {
            usbReceiver = null;
            try { unregisterReceiver(usbReceiver); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
