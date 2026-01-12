package android.app.admin;

import android.content.ComponentName;

public class DevicePolicyManager {
    // Тот самый скрытый метод
    public void setExemptFromBackgroundRestrictedOperations(ComponentName admin, boolean exempt) {}

    // Методы, которые потерял компилятор
    public boolean isProfileOwnerApp(String packageName) { return false; }
    public void wipeData(int flags) {}

    // Константы, на которых упал билд
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME = "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME";
    public static final String EXTRA_PROVISIONING_DISCLAIMER_CONTENT = "android.app.extra.PROVISIONING_DISCLAIMER_CONTENT";
    
    // Если посыпятся другие ошибки на константы - их тоже надо будет сюда вписать как строки
}
