package ephemeralwp.safespace;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NucleusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) || Intent.ACTION_MANAGED_PROFILE_UNLOCKED.equals(action)) {

            wipe.wipe(context);
            
        }
    }
}
