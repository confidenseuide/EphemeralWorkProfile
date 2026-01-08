package com.example.hider;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class SetPasswordActivity extends Activity {
    
    @Override
    protected void onResume() {
        super.onResume();
		dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);		
		if (!isWorkProfileContext() && hasWorkProfile()) {
            launchWorkProfileDelayed();
		}
		if (!isWorkProfileContext() && !hasWorkProfile()) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
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
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btnChange = new Button(this);
        btnChange.setText("Set Password");

        btnChange.setOnClickListener(v -> {
            Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        layout.addView(btnChange);
        setContentView(layout);
    }
}
