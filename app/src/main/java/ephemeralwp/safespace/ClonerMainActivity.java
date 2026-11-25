package ephemeralwp.safespace;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClonerMainActivity extends Activity {

    private ListView listView;
    private AppAdapter adapter;
    private final List<AppItem> userApps = new ArrayList<>();
    private final Set<String> workProfilePackages = new HashSet<>();

    static class AppItem {
        String name;
        String packageName;
        Drawable icon;
        ApplicationInfo info;
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
        
        loadApps();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFFF5F5F5);

        TextView title = new TextView(this);
        title.setText("Clone user apps");
        title.setPadding(40, 50, 40, 30);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);

        listView = new ListView(this);
        layout.addView(listView);

        setContentView(layout);
    }

    private void updateWorkProfilePackages() {
        workProfilePackages.clear();
        LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (launcherApps == null) return;

        UserHandle myHandle = Process.myUserHandle();
        for (UserHandle profile : launcherApps.getProfiles()) {
            if (!profile.equals(myHandle)) {
                List<LauncherActivityInfo> activityList = launcherApps.getActivityList(null, profile);
                for (LauncherActivityInfo info : activityList) {
                    workProfilePackages.add(info.getComponentName().getPackageName());
                }
            }
        }
    }

    private void loadApps() {
        updateWorkProfilePackages();
        userApps.clear();
        PackageManager pm = getPackageManager();

        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !app.packageName.equals(getPackageName())) {
                AppItem item = new AppItem();
                item.name = app.loadLabel(pm).toString();
                item.packageName = app.packageName;
                item.icon = app.loadIcon(pm);
                item.info = app;
                userApps.add(item);
            }
        }

        if (adapter == null) {
            adapter = new AppAdapter();
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return userApps.size(); }
        @Override public Object getItem(int i) { return userApps.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LinearLayout row = new LinearLayout(ClonerMainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(30, 25, 30, 25);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(0xFFFFFFFF);

            final AppItem app = userApps.get(i);

            ImageView iconView = new ImageView(ClonerMainActivity.this);
            iconView.setImageDrawable(app.icon);
            row.addView(iconView, new LinearLayout.LayoutParams(100, 100));

            TextView tv = new TextView(ClonerMainActivity.this);
            tv.setText(app.name);
            tv.setPadding(30, 0, 10, 0);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            row.addView(tv);

            Button btn = new Button(ClonerMainActivity.this);
            boolean isAdded = workProfilePackages.contains(app.packageName);

            if (isAdded) {
                btn.setText("ADDED");
                btn.setBackgroundColor(0xFFD3D3D3);
                btn.setTextColor(0xFFFFFFFF);
                btn.setEnabled(false);
            } else {
                btn.setText("CLONE");
                btn.setBackgroundColor(0xFF4CAF50);
                btn.setTextColor(0xFFFFFFFF);
                btn.setEnabled(true);
                btn.setOnClickListener(v -> confirmClone(app));
            }

            row.addView(btn);

            return row;
        }
    }

    private void confirmClone(final AppItem app) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Clone " + app.name + "?")
                .setPositiveButton("Yes", (dialog, which) -> cloneApp(app.info))
                .setNegativeButton("No", null)
                .show();
    }

    private void cloneApp(ApplicationInfo appInfo) {
        try {
            ArrayList<String> paths = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();

            paths.add(appInfo.sourceDir);
            names.add("base.apk");

            if (appInfo.splitSourceDirs != null) {
                Collections.addAll(paths, appInfo.splitSourceDirs);
                for (int i = 0; i < appInfo.splitSourceDirs.length; i++) {
                    names.add("split_" + i + ".apk");
                }
            }

            Intent intent = new Intent("ephemeralwp.safespace.INSTALL_APK");
            intent.putStringArrayListExtra("paths", paths);
            intent.putStringArrayListExtra("names", names);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PackageManager pm = getPackageManager();
            List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
            ResolveInfo workProxy = null;
            for (ResolveInfo r : info) {
                if (!r.activityInfo.packageName.equals(getPackageName())) {
                    workProxy = r;
                    break;
                }
            }

            if (workProxy == null) {
                showError(new IllegalStateException("Work profile proxy not found."));
                return;
            }

            intent.setComponent(new ComponentName(workProxy.activityInfo.packageName, workProxy.activityInfo.name));
            startActivity(intent);

            Toast.makeText(this, "Sent: " + appInfo.packageName, Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            showError(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (errorDialog != null) {
            if (errorDialog.isShowing()) {            
                errorDialog.dismiss();
            }       
            errorDialog = null;    
        }
    }

    private AlertDialog errorDialog;

    private void showError(Throwable e) {
        if (errorDialog != null && errorDialog.isShowing()) return;
    
        errorDialog = new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(Log.getStackTraceString(e))
            .setCancelable(false)
            .setPositiveButton("OK", null)
            .create();
        errorDialog.show();
    
        TextView tv = errorDialog.findViewById(android.R.id.message);
        if (tv != null) tv.setTextIsSelectable(true);    
    }
}
