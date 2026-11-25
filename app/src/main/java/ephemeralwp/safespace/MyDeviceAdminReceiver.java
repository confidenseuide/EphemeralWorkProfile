package ephemeralwp.safespace;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

	@Override
	public void onPasswordFailed(Context context, Intent intent, UserHandle user) {    
	UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
    KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    if (!um.isUserUnlocked(android.os.Process.myUserHandle()) || km.isKeyguardLocked()) {wipe.wipe(context);}
	}
	
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, MyDeviceAdminReceiver.class);  
		dpm.addUserRestriction(admin, UserManager.DISALLOW_AUTOFILL);		
		dpm.addUserRestriction(admin, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);  			
		dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING); 
		dpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);	
		dpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS);		
		dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS);					
		dpm.clearUserRestriction(admin, UserManager.DISALLOW_MODIFY_ACCOUNTS);							
		dpm.setPasswordQuality(admin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
		dpm.setPasswordMinimumLength(admin, 15);
		dpm.setKeyguardDisabledFeatures(admin, DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS | DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS);																		
		dpm.setCameraDisabled(admin, true);    
		dpm.setBackupServiceEnabled(admin, false);		
		dpm.setScreenCaptureDisabled(admin, true);		
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, NucleusReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP); 
		background.work.around.Start.RunService(context);
		context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, ClonerWorkActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);		
		dpm.addCrossProfileIntentFilter(admin, new IntentFilter("ephemeralwp.safespace.INSTALL_APK"), DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);
		dpm.setProfileEnabled(admin);        		
		Integer current_intB=null;
		Integer current_circleB=null;
		String current_browser=null;
		PackageManager pm = context.getPackageManager();								
		int flags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS | PackageManager.MATCH_UNINSTALLED_PACKAGES; 
		List<PackageInfo> packages = pm.getInstalledPackages(flags);
								
		for (PackageInfo pkg : packages) {
				String pkgName = pkg.packageName;
				if (pkgName.equals(context.getPackageName())) continue;   
									
				if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
			         Intent bIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://"));
					 bIntent.addCategory(Intent.CATEGORY_BROWSABLE);
				     bIntent.setPackage(pkgName);
					if (pm.queryIntentActivities(bIntent, PackageManager.MATCH_UNINSTALLED_PACKAGES).isEmpty()) continue;										
					current_circleB = 0;
					if (pkg.requestedPermissions != null) {
							for (String perm : pkg.requestedPermissions) {
									if (perm.equals("android.permission.PACKAGE_USAGE_STATS")){ current_circleB += 500;}
									if (perm.equals("android.permission.MANAGE_EXTERNAL_STORAGE")) {current_circleB += 500;}
									if (perm.equals("android.permission.SYSTEM_ALERT_WINDOW")){ current_circleB += 500;}
									if (perm.equals("android.permission.WRITE_SETTINGS")) {current_circleB += 500;}
									if (perm.equals("android.permission.SEND_SMS") || perm.equals("android.permission.RECEIVE_SMS") || perm.equals("android.permission.READ_SMS")) {current_circleB += 500;}
								    if (perm.equals("android.permission.CALL_PHONE") || perm.equals("android.permission.READ_PHONE_STATE")) {current_circleB += 500;}
								    if (perm.equals("android.permission.WAKE_LOCK")){ current_circleB += 150;}
									if (perm.contains("TURN_SCREEN_ON") || perm.contains("WAKEUP_DEVICE")) {current_circleB += 300; }
									if (perm.equals("android.permission.CHANGE_WIFI_STATE")){ current_circleB += 200;}
									if (perm.equals("com.android.alarm.permission.SET_ALARM")) {current_circleB += 200;}
									if (perm.equals("android.permission.SCHEDULE_EXACT_ALARM")) {current_circleB += 200;}
							  }
					}
					if (current_intB == null) {
							current_intB = current_circleB;
							current_browser = pkgName;
					}
					if (current_circleB < current_intB) {
							current_intB = current_circleB;
							current_browser = pkgName;
					}
					
				}
								
		}
			
		try {				
			if (current_browser != null) {
					context.getSharedPreferences("added_system_apps", Context.MODE_PRIVATE).edit().putBoolean(current_browser, true).apply();
					dpm.enableSystemApp(admin, current_browser);				
			}			
		} catch (Throwable t) {}    

		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		SharedPreferences p = context.getSharedPreferences("HiderPrefs", Context.MODE_PRIVATE);
								
		Set<String> allPackages = new HashSet<>();
		List<InputMethodInfo> enabledImes = imm.getInputMethodList();
		for (InputMethodInfo imi : enabledImes) {
				allPackages.add(imi.getPackageName());
		}
								
		Set<String> previouslyHidden = p.getStringSet("hidden_pkgs", new HashSet<>());
		allPackages.addAll(previouslyHidden);
								
		String current_keyboard = null;
		Integer current_int = null;
								
		for (String pkgName : allPackages) {
					try {
						PackageInfo pkg = pm.getPackageInfo(pkgName, PackageManager.GET_SERVICES | PackageManager.GET_PERMISSIONS);
						int current_circle = 0;
						boolean hasMainIme = false;
						for (InputMethodInfo imi : enabledImes) {
					       if (imi.getPackageName().equals(pkgName)) {
									if (imi.getSubtypeCount() == 0) { 
										hasMainIme = true;
										break; 
									}
									for (int i = 0; i < imi.getSubtypeCount(); i++) {
											if (!imi.getSubtypeAt(i).isAuxiliary()) { 
												hasMainIme = true;
												break; 
											}
									}
						   }
									if (hasMainIme) break;
						}
										if (pkg.requestedPermissions != null) {
											for (String perm : pkg.requestedPermissions) {
												if (perm.equals("android.permission.AUTHENTICATE_ACCOUNTS")) current_circle += 500;
												if (perm.equals("android.permission.MANAGE_ACCOUNTS")) current_circle += 500;
												if (perm.equals("android.permission.USE_CREDENTIALS")) current_circle += 500;
												if (perm.equals("android.permission.READ_PROFILE")) current_circle += 500;
												if (perm.equals("android.permission.POST_NOTIFICATIONS")) current_circle += 500;
												if (perm.equals("android.permission.ACCESS_WIFI_STATE")) current_circle += 500;
												if (perm.equals("android.permission.BLUETOOTH_CONNECT")) current_circle += 500;
												if (perm.equals("com.google.android.c2dm.permission.RECEIVE")) current_circle += 500;
												if (perm.equals("com.google.android.gms.permission.AD_ID")) current_circle += 500;
												if (perm.equals("com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE")) current_circle += 500;
											}
										}
						
						                if (!hasMainIme) current_circle = Integer.MAX_VALUE;
										
										if (current_int == null || current_circle < current_int) {
											current_int = current_circle;
											current_keyboard = pkgName;
										}
					} catch (Throwable t) {}
		}
		if (current_keyboard != null) {
					Set<String> nowHidden = new HashSet<>();
					dpm.setApplicationHidden(admin, current_keyboard, false);
								
					for (String pkg : allPackages) {
							if (!pkg.equals(current_keyboard)) {
								 dpm.setApplicationHidden(admin, pkg, true);
								 nowHidden.add(pkg);
							}
					}
					p.edit().putStringSet("hidden_pkgs", nowHidden).apply();
		}
		
	}	
}
