package ephemeralwp.safespace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class HighEfficiencyModeActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setFillViewport(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        layout.setGravity(Gravity.CENTER);

        TextView tv = new TextView(this);
        tv.setTextSize(18);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.LEFT);

        tv.setText("START High-Efficiency Mode? In this mode, the app will use alarms and receivers to continuously restart itself and maintain a high process priority, to make it harder for aggressive firmware to kill it and ensure its restart after death. Furthermore, when the main service restarts, not just via screen-off receiver the app will check every 5 seconds if the screen is off or locked and wipe data. in simple words, it will check if the app has missed the screen turning off moment which occurred when the receiver was inactive or the broadcast was not delivered and app didn't have trigger to wipe data, for example because it was killed at that moment and only then restarted.\n\n"+
                "This mode is suitable if you have a low-spec phone or a phone with aggressive battery optimization, or if you’re running a resource-intensive game.\n\n"+                        
                "If you see a snowflake ❄ after text in the app notification title, it is normal mode. If you see a flame 🔥, it is high-efficiency mode.\n\n"+             
                "High-efficiency mode may consume more battery power.");
        
        Button runBtn = new Button(this);        
        runBtn.setText("START");
        runBtn.setBackgroundColor(Color.WHITE);
        runBtn.setTextColor(Color.BLACK);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        btnParams.setMargins(0, 50, 0, 0);
        runBtn.setLayoutParams(btnParams);

        runBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getPackageName() + ".START");
            intent.setPackage(getPackageName());            
            sendBroadcast(intent);
        });

        layout.addView(tv);
        layout.addView(runBtn);
        scroll.addView(layout);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
