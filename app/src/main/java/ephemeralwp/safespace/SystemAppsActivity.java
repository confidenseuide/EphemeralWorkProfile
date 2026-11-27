package ephemeralwp.safespace;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.widget.*;
import android.view.*;
import java.util.*;

public class SystemAppsActivity extends Activity {

    private DevicePolicyManager dpm;
    private ComponentName admin;
    private ListView listView;
    private AppAdapter adapter;
    private List<AppInfo> systemApps = new ArrayList<>();
    private SharedPreferences prefs;

    static class AppInfo {
        String name;
        String packageName;
        Drawable icon;
        boolean isAdded; 
    }

    @Override
    protected void onResume() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onResume();
		getWindow().getDecorView().setKeepScreenOn(true);
        getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        admin = new ComponentName(this, MyDeviceAdminReceiver.class);
        
        prefs = getSharedPreferences("added_system_apps", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFFF5F5F5);

		TextView title = new TextView(this);
        title.setText("Add System Apps");
        title.setPadding(40, 50, 40, 30);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);

		TextView warning = new TextView(this);
        warning.setText("Here you can add only system (pre-installed) apps to work profile");
        warning.setTextColor(0xFFFFFFFF);
        warning.setTextSize(14);
        warning.setPadding(40, 30, 40, 30);
        GradientDrawable warnBg = new GradientDrawable();
        warnBg.setColor(0xCCD32F2F);
        warnBg.setCornerRadius(24);
        warning.setBackground(warnBg);
        LinearLayout.LayoutParams warnLp = new LinearLayout.LayoutParams(-1, -2);
        warnLp.setMargins(40, 0, 40, 30);
        layout.addView(warning, warnLp);
                
        listView = new ListView(this);
		layout.addView(listView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

		Button backBtn = new Button(this);
        backBtn.setText("Back");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setAllCaps(false);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(0xFFF44336);
        btnBg.setCornerRadius(24);
        backBtn.setBackground(btnBg);
        backBtn.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, -2);
        btnLp.setMargins(40, 30, 40, 60);
        layout.addView(backBtn, btnLp);
        
        setContentView(layout);
        refreshList();
    }

    private void refreshList() {
        systemApps.clear();
        PackageManager pm = getPackageManager();
        
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);
        Set<String> installedNames = new HashSet<>();
        for (ApplicationInfo ai : installedApps) {
            installedNames.add(ai.packageName);
        }

        List<ApplicationInfo> allApps = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);

        for (ApplicationInfo app : allApps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || app.packageName.equals(getPackageName())) continue;

            boolean isActuallyInstalled = installedNames.contains(app.packageName);
            boolean isSavedInPrefs = prefs.contains(app.packageName);

            if (isActuallyInstalled && !isSavedInPrefs) {
                continue; 
            }

            AppInfo info = new AppInfo();
            info.name = app.loadLabel(pm).toString();
            info.packageName = app.packageName;
            info.icon = app.loadIcon(pm);
            info.isAdded = isSavedInPrefs;
            
            systemApps.add(info);
        }
        
        adapter = new AppAdapter();
        listView.setAdapter(adapter);
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return systemApps.size(); }
        @Override public Object getItem(int i) { return systemApps.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LinearLayout row = new LinearLayout(SystemAppsActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(30, 25, 30, 25);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(0xFFFFFFFF);

            final AppInfo app = systemApps.get(i);

            ImageView iconView = new ImageView(SystemAppsActivity.this);
            iconView.setImageDrawable(app.icon);
            row.addView(iconView, new LinearLayout.LayoutParams(100, 100));

            TextView tv = new TextView(SystemAppsActivity.this);
            tv.setText(app.name);
            tv.setPadding(30, 0, 10, 0);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            row.addView(tv);

            Button btn = new Button(SystemAppsActivity.this);
            btn.setText(app.isAdded ? "REMOVE" : "ADD");
            btn.setBackgroundColor(app.isAdded ? 0xFFF44336 : 0xFF4CAF50);
            btn.setTextColor(0xFFFFFFFF);

            btn.setOnClickListener(v -> toggleStatus(app));
            row.addView(btn);

            return row;
        }
    }

	private AlertDialog statusDialog;

    private void toggleStatus(final AppInfo app) {
	if (statusDialog != null && statusDialog.isShowing()) return;
            
    statusDialog = new AlertDialog.Builder(this)
        .setTitle("Confirm")
        .setMessage((app.isAdded ? "Remove " : "Add ") + app.name + "?")
        .setPositiveButton("Yes", (dialog, which) -> {
            try {
                if (!app.isAdded) {
                    
                    dpm.enableSystemApp(admin, app.packageName);
                    dpm.setApplicationHidden(admin, app.packageName, false);
                    prefs.edit().putBoolean(app.packageName, true).apply();
                    
                    app.isAdded = true;
                } else {
                    
                    dpm.setApplicationHidden(admin, app.packageName, true);
                    prefs.edit().remove(app.packageName).apply();
                    
                    app.isAdded = false;
                }
                
                adapter.notifyDataSetChanged();
                
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
        .setNegativeButton("No", null)
        .show();
		
		Window window = statusDialog.getWindow();
		if (window != null) {        
			WindowManager.LayoutParams params = window.getAttributes();        
			params.gravity = Gravity.CENTER;        
			params.x = 0;       
			params.y = 0;        
			window.setAttributes(params);
		}
	}

	   @Override
    protected void onDestroy() {        
        if (statusDialog != null) {
            if (statusDialog.isShowing()) {            
                statusDialog.dismiss();
            }       
            statusDialog = null;    
        }        
		super.onDestroy();
    }

}
