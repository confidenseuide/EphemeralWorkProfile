package ephemeralwp.safespace;

import android.content.Intent;
import android.widget.Toast;
import android.widget.Button;
import android.content.pm.LauncherApps;
import android.os.UserHandle;
import java.util.List;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class CopeActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN);  
    }

    private TextView customInputDisplay;
    private StringBuilder currentInput = new StringBuilder();

    private TextView text;
    private LinearLayout buttonBox;

    private boolean isEn() { return !Locale.getDefault().getLanguage().equals("ru"); }
            
    private void setButtonState(Button b, boolean enabled) {    
		b.setEnabled(enabled);
		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.RECTANGLE);
		shape.setColor(enabled ? Color.parseColor("#34495e") : Color.parseColor("#4a6278"));
		shape.setCornerRadius(6f);
		b.setBackground(shape);
    }
        
    @Override
    protected void onCreate(Bundle b) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(b);        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);
        text = new TextView(this);
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        text.setTextSize(16f);
        text.setTextColor(Color.WHITE);
        buttonBox = new LinearLayout(this);
        buttonBox.setOrientation(LinearLayout.VERTICAL);
        buttonBox.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        boxParams.setMargins(0, 64, 0, 0);
        buttonBox.setLayoutParams(boxParams);
        root.addView(text);
        root.addView(buttonBox);
        scrollView.addView(root);
        setContentView(scrollView);
        renderMainSettingsMenu();
    }

    private void render(String textValue) { text.setText(textValue); }

    private void renderMainSettingsMenu() {
    buttonBox.removeAllViews();

    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    ComponentName adminName = new ComponentName(this, MyDeviceAdminReceiver.class);

	CheckBox cbCrossProfileCopyPaste = new CheckBox(this);

	  cbCrossProfileCopyPaste.setText(isEn()
        ? "Disallow cross profile copy-paste"
        : "Запретить копирование и вставку между профилями");

	  cbCrossProfileCopyPaste.setTextColor(Color.WHITE);
	  cbCrossProfileCopyPaste.setTextSize(15f);

		Bundle restrictions = dpm.getUserRestrictions(new ComponentName(this, MyDeviceAdminReceiver.class));
		boolean copyPasteDisabled = restrictions != null
        && restrictions.getBoolean(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, false);
		cbCrossProfileCopyPaste.setChecked(copyPasteDisabled);
		cbCrossProfileCopyPaste.setOnClickListener(v -> {   
			boolean disable = cbCrossProfileCopyPaste.isChecked();  
			if (disable) {      
				dpm.addUserRestriction(adminName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);  
			} else {   
				dpm.clearUserRestriction(adminName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
			}
		});

	buttonBox.addView(cbCrossProfileCopyPaste);

	CheckBox cbCam = new CheckBox(this);
		cbCam.setText(isEn()
        ? "Disallow camera in work profile"
        : "Запретить камеру в рабочем профиле");
		cbCam.setTextColor(Color.WHITE);
		cbCam.setTextSize(15f);

		boolean hasNoCam = dpm.getCameraDisabled(adminName);		
		cbCam.setChecked(hasNoCam);

		cbCam.setOnClickListener(v -> {
   
		boolean disable = cbCam.isChecked();   
		dpm.setCameraDisabled(adminName, disable);
  
		});

        buttonBox.addView(cbCam);

	CheckBox cbScr = new CheckBox(this);
		cbScr.setText(isEn()
        ? "Disallow screenshots in work profile"
        : "Запретить скриншоты в рабочем профиле");
		cbScr.setTextColor(Color.WHITE);
		cbScr.setTextSize(15f);

		boolean hasNoScr = dpm.getScreenCaptureDisabled(adminName);                
		cbScr.setChecked(hasNoScr);

		cbScr.setOnClickListener(v -> {
   
		boolean disable = cbScr.isChecked();   		
		dpm.setScreenCaptureDisabled(adminName, disable);
			
        });

        buttonBox.addView(cbScr);	
    
    Button btnSetWorkPassword = new Button(this);
    btnSetWorkPassword.setText(
        isEn() ? "Set password for work profile"
               : "Установить пароль для рабочего профиля"
    );

    GradientDrawable shape = new GradientDrawable();
    shape.setShape(GradientDrawable.RECTANGLE);
    shape.setColor(Color.parseColor("#34495e"));
    shape.setCornerRadius(7f);

    btnSetWorkPassword.setBackground(shape);
    btnSetWorkPassword.setTextColor(Color.WHITE);
    btnSetWorkPassword.setPadding(33, 33, 33, 33);

    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    btnParams.setMargins(0, 15, 0, 15);
    btnSetWorkPassword.setLayoutParams(btnParams);

    btnSetWorkPassword.setOnClickListener(v -> {        
            Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            startActivity(intent);  
            finish();
    });

    buttonBox.addView(btnSetWorkPassword);

     boolean isSeparate = false;
     try {
        DevicePolicyManager ldpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName ladminName = new ComponentName(this, MyDeviceAdminReceiver.class);     
        isSeparate = !ldpm.isUsingUnifiedPassword(ladminName);
    } catch (Throwable t) {
        isSeparate = false;
    }
                    
    if (isSeparate) {   
		Button btnSetWorkAttempts = new Button(this);
        btnSetWorkAttempts.setText( isEn() ? "Set unlock attempts limit for work profile" : "Установить лимит попыток разблокировки рабочего профиля");

        GradientDrawable attemptsShape = new GradientDrawable();
        attemptsShape.setShape(GradientDrawable.RECTANGLE);
        attemptsShape.setColor(Color.parseColor("#34495e"));
        attemptsShape.setCornerRadius(6f);

        btnSetWorkAttempts.setBackground(attemptsShape);
        btnSetWorkAttempts.setTextColor(Color.WHITE);
        btnSetWorkAttempts.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams attemptsParams =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        attemptsParams.setMargins(0, 15, 0, 15);
        btnSetWorkAttempts.setLayoutParams(attemptsParams);
        
        btnSetWorkAttempts.setOnClickListener(v -> {    
          render(isEn() ? "Set the maximum number of failed unlock attempts before reset for the work profile. This reset can wipe main profile too if you used the ADB command specified in the toggles." 
				   : "Задайте максимальное количество неверных попыток разблокировки до сброса для рабочего профиля. Этот сброс может стереть и основной профиль если вы использовали ADB команду указанную в переключателях.");
          renderWorkProfileAttemptsInput();
        });

        buttonBox.addView(btnSetWorkAttempts);
            
	    }

        Button btnBack = new Button(this);
        btnBack.setText(isEn() ? "Main menu" : "Главное меню");
        GradientDrawable backShape = new GradientDrawable();
        backShape.setShape(GradientDrawable.RECTANGLE);
        backShape.setColor(Color.parseColor("#34495e"));
        backShape.setCornerRadius(6f);
        btnBack.setBackground(backShape);
        btnBack.setTextColor(Color.WHITE);
        btnBack.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(   
            LinearLayout.LayoutParams.MATCH_PARENT,  
            LinearLayout.LayoutParams.WRAP_CONTENT
        );

        backParams.setMargins(0, 15, 0, 15);
        btnBack.setLayoutParams(backParams);
        btnBack.setOnClickListener(v -> startActivity(new Intent(this, ActionsActivity.class)));
        buttonBox.addView(btnBack);
    }

    private void renderWorkProfileAttemptsInput() {
    buttonBox.removeAllViews();
    currentInput.setLength(0);

    TextView customInputDisplay = new TextView(this);
    customInputDisplay.setGravity(Gravity.CENTER);
    customInputDisplay.setTextSize(22f);
    customInputDisplay.setTextColor(Color.WHITE);
    customInputDisplay.setText("");
    customInputDisplay.setPadding(32, 32, 32, 32);

    GradientDrawable bgShape = new GradientDrawable();
    bgShape.setShape(GradientDrawable.RECTANGLE);
    bgShape.setColor(Color.parseColor("#2c3e50"));
    bgShape.setCornerRadius(8f);
    bgShape.setStroke(2, Color.parseColor("#7f8c8d"));

    customInputDisplay.setBackground(bgShape);

    LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    displayParams.setMargins(0, 0, 0, 16);
    customInputDisplay.setLayoutParams(displayParams);

    buttonBox.addView(customInputDisplay);

    LinearLayout keypadBox = new LinearLayout(this);
    keypadBox.setOrientation(LinearLayout.VERTICAL);
    keypadBox.setGravity(Gravity.CENTER);

    LinearLayout.LayoutParams keypadParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    keypadParams.setMargins(0, 16, 0, 16);
    keypadBox.setLayoutParams(keypadParams);

    final Button[] okBtnRef = new Button[1];

    String[][] keys = {
            {"1", "2", "3"},
            {"4", "5", "6"},
            {"7", "8", "9"},
            {"⌫", "0", "OK"}
    };

    for (String[] rowKeys : keys) {

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        rowParams.setMargins(0, 4, 0, 4);
        rowLayout.setLayoutParams(rowParams);

        for (String key : rowKeys) {

            Button keyBtn = new Button(this);
            keyBtn.setText(key);

            GradientDrawable keyShape = new GradientDrawable();
            keyShape.setShape(GradientDrawable.RECTANGLE);

            boolean isOk = key.equals("OK");

            if (isOk) {
                keyBtn.setEnabled(false);
                keyShape.setColor(Color.parseColor("#4a6278"));
                okBtnRef[0] = keyBtn;
            } else {
                keyShape.setColor(Color.parseColor("#34495e"));
            }

            keyShape.setCornerRadius(6f);

            keyBtn.setBackground(keyShape);
            keyBtn.setTextColor(Color.WHITE);
            keyBtn.setTextSize(20f);
            keyBtn.setPadding(16, 24, 16, 24);

            LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

            keyParams.setMargins(4, 0, 4, 0);
            keyBtn.setLayoutParams(keyParams);

            keyBtn.setOnClickListener(v -> {

                if (key.equals("⌫")) {

                    if (currentInput.length() > 0) {
                        currentInput.deleteCharAt(currentInput.length() - 1);
                    }

                } else if (isOk) {

                    if (!keyBtn.isEnabled()) {
                        return;
                    }

                    try {
                        int attempts = Integer.parseInt(currentInput.toString());

                        if (attempts <= 0) {
                            return;
                        }

                        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

                        ComponentName adminName = new ComponentName(this, MyDeviceAdminReceiver.class);
                                            
                            dpm.setMaximumFailedPasswordsForWipe(adminName, attempts);

                            Toast.makeText(this, isEn() ? "Unlock attempts limit applied" : "Лимит попыток разблокировки применён", Toast.LENGTH_SHORT).show();
                            renderMainSettingsMenu();
                        
                    } catch (NumberFormatException ignored) {}

                } else {
                    if (currentInput.length() < 9) {
                        currentInput.append(key);
                    }
                }

                customInputDisplay.setText(currentInput.toString());

                boolean valid = false;

                if (currentInput.length() > 0) {
                    try {
                        long value = Long.parseLong(currentInput.toString());
                        valid = value > 0 && value <= Integer.MAX_VALUE;
                    } catch (NumberFormatException ignored) {
                        valid = false;
                    }
                }

                setButtonState(okBtnRef[0], valid);
            });

            rowLayout.addView(keyBtn);
        }

        keypadBox.addView(rowLayout);
    }

    buttonBox.addView(keypadBox);

    }

       
}
