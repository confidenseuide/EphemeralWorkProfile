package ephemeralwp.safespace;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;
import android.widget.*;
import android.view.*;
import android.view.inputmethod.*;

public class MainActivity extends Activity {

	private void showOnboarding() {
    final android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
    android.view.Window window = dialog.getWindow();
    if (window != null) {
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);    
		window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
    float scaleFactor = (float) Math.sqrt(dm.widthPixels * dm.heightPixels);
    float textPx = scaleFactor * 0.025f;
    int pX = (int) (dm.widthPixels * 0.08f);

    android.widget.LinearLayout root = new android.widget.LinearLayout(this);
    root.setOrientation(android.widget.LinearLayout.VERTICAL);
    root.setBackgroundColor(0xFFFFFFFF);

    android.widget.LinearLayout headerContainer = new android.widget.LinearLayout(this);
    headerContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
    headerContainer.setBackgroundColor(0xFF7484B0);
    android.widget.LinearLayout.LayoutParams hParams = new android.widget.LinearLayout.LayoutParams(-1, 0, 1.0f);
    
    android.view.View spacer = new android.view.View(this);
    headerContainer.addView(spacer, new android.widget.LinearLayout.LayoutParams(-1, 0, 1.0f));

    android.widget.TextView titleTv = new android.widget.TextView(this);
    titleTv.setText("EphemeralWorkProfile");
    titleTv.setTextColor(0xFFFFFFFF);
    titleTv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, scaleFactor * 0.035f);
    titleTv.setPadding(pX, 0, pX, (int)(pX * 0.5f));
    headerContainer.addView(titleTv);
    
    root.addView(headerContainer, hParams);

    android.widget.ScrollView scroll = new android.widget.ScrollView(this);
    android.widget.LinearLayout.LayoutParams sParams = new android.widget.LinearLayout.LayoutParams(-1, 0, 2.0f);
    
    android.widget.TextView tv = new android.widget.TextView(this);
    tv.setPadding(pX, (int)(pX * 0.8f), pX, pX);
    tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx);
    tv.setTextColor(0xFF333333);
    tv.setLineSpacing(0, 1.2f);
	tv.setTypeface(null, android.graphics.Typeface.BOLD); 
    tv.setText("Hello! This is EphemeralWorkProfile app!\n\n" +
            "This app creates work profile that will be destroyed on screen off, on phone reboot or profile restart, on any USB data connection except for simple charging from power brick\n\n" +
            "Just click start -> next -> next ->... to create profile\n\n" +
            "Before work profile enabling, this app: \n" +
            "Disables backup, camera and screenshots in work profile (to protect your privacy)\n"+
			"Enables some system components (browser, keyboard) and allows app installation\n" +
            "Sets unlocking security requirements to help you choose the optimal password length and type\n"+
			"When profile created it opens control panel screen where you can manage profile and set a password\n\n"+
			"WARNING: This app may not work on systems with autostart restrictions, for example, on Xiaomi devices\n");
    scroll.addView(tv);
    root.addView(scroll, sParams);

    android.view.View divider = new android.view.View(this);
    divider.setBackgroundColor(0xFFDCDCDC);
    root.addView(divider, new android.widget.LinearLayout.LayoutParams(-1, 3));
		
    android.widget.RelativeLayout bottomBar = new android.widget.RelativeLayout(this);
    bottomBar.setBackgroundColor(0xFFF5F5F5);
    bottomBar.setPadding(pX, (int)(pX * 0.2f), pX, (int)(pX * 0.2f));

    android.widget.Button btn = new android.widget.Button(this);
    btn.setText("START >");
    btn.setTextColor(0xFF333333);
    btn.setBackgroundColor(0);
    btn.setTypeface(null, android.graphics.Typeface.BOLD);
    btn.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx * 0.9f);

    android.widget.RelativeLayout.LayoutParams btnParams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
    btnParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_RIGHT);
    bottomBar.addView(btn, btnParams);

    root.addView(bottomBar, new android.widget.LinearLayout.LayoutParams(-1, -2));

    btn.setOnClickListener(v -> {
        
		Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
		intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, new ComponentName(this, MyDeviceAdminReceiver.class));
		intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE, true);
		startActivityForResult(intent, 100);
		
        dialog.dismiss();
    });

    dialog.setContentView(root);
    dialog.setCancelable(false);
    dialog.show();

	}

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		final DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        final TextView tv = new TextView(this);
        tv.setBackgroundColor(0xFF000000);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(120);
        tv.setGravity(17);
        setContentView(tv);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        
        if (dpm.isProfileOwnerApp(getPackageName())) {
			Context appContext = getApplicationContext();
			Intent actions = new Intent(appContext, ActionsActivity.class);
			actions.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			appContext.startActivity(actions);
		} else {
            if (hasWorkProfile()) {
                launchWorkProfileDelayed();
            } else {
				showOnboarding();				
			}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isWorkProfileContext() && hasWorkProfile()) {
            launchWorkProfileDelayed();
		} else {
			KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);        
			if (keyguardManager != null && keyguardManager.isKeyguardLocked()) finish();    
		}
		getWindow().getDecorView().setKeepScreenOn(true);
        getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
		if (isWorkProfileContext()) {
			Context appContext = getApplicationContext();
			Intent actions = new Intent(appContext, ActionsActivity.class);
			actions.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			appContext.startActivity(actions);			
		}
    }


    private boolean isWorkProfileContext() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isProfileOwnerApp(getPackageName());
    }

    private boolean hasWorkProfile() {
    LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
    UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
    
    if (launcherApps != null && userManager != null) {
        List<UserHandle> profiles = userManager.getUserProfiles();
        
        for (UserHandle profile : profiles) {
            if (userManager.getSerialNumberForUser(profile) == 0) {
                continue;
            }
            
            try {
                if (launcherApps.isPackageEnabled(getPackageName(), profile)) {
                    return true;
                }
            } catch (Throwable e) {}
        }
    }
    return false;
	}
	
    private void launchWorkProfileDelayed() {
	   LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
            UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
            
            if (launcherApps != null && userManager != null) {
                List<UserHandle> profiles = userManager.getUserProfiles();
                for (UserHandle profile : profiles) {
                   if (userManager.getSerialNumberForUser(profile) != 0) {
                       try {
					   launcherApps.startMainActivity(
                            new ComponentName(getPackageName(), MainActivity.class.getName()), 
                            profile, null, null
                        );
					   } catch (Throwable t) {}                                                
                    }
                }
				finish();
            }        
    }
}
