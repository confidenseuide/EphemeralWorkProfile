package ephemeralwp.safespace;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsActivity extends Activity {

    // Храним мапу "Label -> ClassName", чтобы при клике знать, что запускать
    private Map<String, String> labelToClass = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Кнопка просто сворачивает приложение
        Button btnHome = new Button(this);
        btnHome.setText("HOME");
        btnHome.setOnClickListener(v -> {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
        });
        layout.addView(btnHome);

        ListView listView = new ListView(this);
        layout.addView(listView);
        setContentView(layout);

        loadActivities();

        List<String> labels = new ArrayList<>(labelToClass.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                String label = labels.get(position);
                String className = labelToClass.get(label);
                Intent i = new Intent();
                i.setComponent(new ComponentName(getPackageName(), className));
                startActivity(i);
            } catch (Exception ignored) {}
        });
    }

    private void loadActivities() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            
            for (ActivityInfo info : pi.activities) {
                // Игнорируем лаунчер и текущую активити
                if (info.name.equals(this.getClass().getName()) || info.name.contains("MainActivity")) {
                    continue;
                }

                // Получаем label. Если его нет — берем короткое имя класса
                String label = info.loadLabel(pm).toString();
                if (label.equals(info.name) || label.isEmpty()) {
                    String[] parts = info.name.split("\\.");
                    label = parts[parts.length - 1];
                }

                // Добавляем в мапу (если лейблы одинаковые, добавится только один)
                labelToClass.put(label, info.name);
            }
        } catch (Exception ignored) {}
    }
}
