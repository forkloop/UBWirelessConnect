package us.forkloop.ubwirelessconnect;

import us.forkloop.ubwirelessconnect.R;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

public class UBWirelessConnectActivity extends Activity implements OnClickListener {

    private static final String PREFS_FILE = "ub_wireless_connect";
    private static final String PREFIX = "ubwc";
    private EditText username;
    private EditText password;

    private int appWidgetId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ub_wireless_connect_config);

        Log.d(getClass().getSimpleName(), "Start configuration activity...");
        setResult(RESULT_CANCELED);
        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        username = (EditText) findViewById(R.id.ub_name);
        password = (EditText) findViewById(R.id.password);
        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_button) {
            // Save user info.
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();
            editor.putString(PREFIX + "username", username.getText().toString());
            editor.putString(PREFIX + "password", password.getText().toString());
            editor.commit();

            // Update widget.
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.ub_wireless_connect_widget);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}