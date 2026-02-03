package ephemeralwp.safespace;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NucleusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || 
            action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
            action.equals(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)) {
            
            if (isInitialStickyBroadcast()) {
            
            return;}

            wipe.wipe(context);
            
        }
    }
}
