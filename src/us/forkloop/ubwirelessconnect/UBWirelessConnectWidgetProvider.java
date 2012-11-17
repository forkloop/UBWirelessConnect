package us.forkloop.ubwirelessconnect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class UBWirelessConnectWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "UBWirelessConnectWidgetProvider";
    private static final String PREFS_FILE = "ub_wireless_connect";
    private static final String PREFIX = "ubwc";
    private static final int NID = 1001;
    // Hope this will never done.
    private static final String TEST_URL = "http://www.google.com";
    private static final String LOGIN_URL = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("us.forkloop.ubwirelessconnect.WIDGET_TOGGLE")) {
        }
        super.onReceive(context, intent);
    }

    /**
     * Click the widget to start login from ub wireless router.
     * Start with testing the Internet is accessible, if not, try to connect with username/password.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int NUM = appWidgetIds.length;

        for (int i=0; i<NUM; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent();
            intent.setAction("us.forkloop.ubwirelessconnect.WIDGET_TOGGLE");
            // Equivalent to send broadcast.
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ub_wireless_connect_widget);
            remoteViews.setOnClickPendingIntent(R.id.toggle, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private class LoginAsyncTask extends AsyncTask<Context, Integer, String> {

        private Context context = null;
        private URL url = null;
        private URL loginURL = null;
        private HttpURLConnection conn = null;
        private HttpURLConnection loginConn = null;

        @Override
        protected void onPostExecute(String result) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.holy_dark_navigation_refresh)
                .setContentTitle("UBWirelessConnect")
                .setContentText(result);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NID, builder.build());
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];
            // Check wifi connection.
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo == null || !(networkInfo.getTypeName().equals("WIFI") && networkInfo.isConnected())) {
                return "Wi-Fi disconnected.";
            }
            // Check whether network is actually `connected`.
            try {
                url = new URL(TEST_URL);
            } catch (MalformedURLException mue) {
                //XXX
                mue.printStackTrace();
                Log.d(TAG, mue.getMessage());
                return mue.getMessage();
            }
            try {
                conn = (HttpURLConnection) url.openConnection();
                int responseCode = conn.getResponseCode();
                // Connected, Cool.
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Connected.";
                } else {
                    // Get login info.
                    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
                    String username = sharedPreferences.getString(PREFIX + "username", "");
                    Log.d(TAG, "username: " + username);
                    String password = sharedPreferences.getString(PREFIX + "password", "");
                    if (username.isEmpty() || password.isEmpty()) {
                        return "Invalid username or password.";
                    }
                    // Start to connect...
                    try {
                        loginURL = new URL(LOGIN_URL);
                    } catch (MalformedURLException mue) {
                        mue.printStackTrace();
                        Log.d(TAG, mue.getMessage());
                        return mue.getMessage();
                    }
                    try {
                        loginConn = (HttpURLConnection) loginURL.openConnection();
                        loginConn.setDoOutput(true);
                        //TODO
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        return ioe.getMessage();
                    } finally {
                        loginConn.disconnect();
                    }
                    return "Connected.";
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.d(TAG, ioe.getMessage());
                return ioe.getMessage();
            } finally {
                conn.disconnect();
            }
        }
    }
}