package ephemeralwp.safespace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ClonerWorkActivity extends Activity {

    private static final String ACTION_INSTALL_DONE = "ephemeralwp.safespace.INSTALL_DONE";
    private TextView logView;

    private final BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            appendLog("Callback status: " + status + " | msg: " + msg);

            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(confirm);
                }
            } else if (status == PackageInstaller.STATUS_SUCCESS) {
                appendLog("SUCCESSFULLY INSTALLED!");
                finish();
            } else {
                showDialog("PackageInstaller Error (" + status + ")", String.valueOf(msg));
            }
        }
    };

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
        
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        logView = new TextView(this);
        logView.setTextColor(Color.GREEN);
        logView.setPadding(33, 33, 33, 33);
        logView.setTextIsSelectable(true);
        scroll.addView(logView);
        setContentView(scroll);

        IntentFilter filter = new IntentFilter(ACTION_INSTALL_DONE);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(installReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(installReceiver, filter);
        }

        ArrayList<String> paths = getIntent().getStringArrayListExtra("paths");
        ArrayList<String> names = getIntent().getStringArrayListExtra("names");

        if (paths != null && names != null) {
            appendLog("Files received: " + paths.size());
            new Thread(() -> installSession(paths, names)).start();
        } else {
            appendLog("ERROR: paths or names == null");
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(installReceiver);
        if (errorDialog != null) {
            if (errorDialog.isShowing()) {            
                errorDialog.dismiss();
            }        
            errorDialog = null;
        }
        super.onDestroy();
    }

    private void appendLog(String text) {
        runOnUiThread(() -> logView.append(text + "\n"));
    }

    private void installSession(ArrayList<String> paths, ArrayList<String> names) {
        PackageInstaller installer = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
        );

        PackageInstaller.Session session = null;
        try {
            int sessionId = installer.createSession(params);
            session = installer.openSession(sessionId);
            appendLog("Session created: " + sessionId);

            int bufSize = 65536;
            byte[] buf = new byte[bufSize];

            for (int i = 0; i < paths.size(); i++) {
                File apkFile = new File(paths.get(i));
                appendLog("Reading: " + apkFile.getAbsolutePath() + " (" + apkFile.length() + " bytes)");
                try (InputStream in = new FileInputStream(apkFile);
                     OutputStream out = session.openWrite(names.get(i), 0, apkFile.length())) {
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    session.fsync(out);
                }
                appendLog("Written to session: " + names.get(i));
            }

            Intent callbackIntent = new Intent(ACTION_INSTALL_DONE).setPackage(getPackageName());
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, sessionId, callbackIntent, flags);

            appendLog("Committing session...");
            session.commit(pendingIntent.getIntentSender());
        } catch (Throwable e) {
            if (session != null) session.abandon();
            String err = Log.getStackTraceString(e);
            appendLog("EXCEPTION:\n" + err);
            showDialog("Error", err);
        } finally {
            if (session != null) session.close();
        }
    }
    
    private AlertDialog errorDialog;

    private void showDialog(String title, String message) {
        runOnUiThread(() -> {
            if (errorDialog != null && errorDialog.isShowing()) return;
            errorDialog = new AlertDialog.Builder(this)            
                .setTitle(title)            
                .setMessage(message)            
                .setCancelable(false)            
                .setPositiveButton("OK", null)            
                .setOnDismissListener(d -> finish())            
                .create();
            errorDialog.show();
            TextView tv = errorDialog.findViewById(android.R.id.message);
            if (tv != null) tv.setTextIsSelectable(true);            
        });
    }
}
