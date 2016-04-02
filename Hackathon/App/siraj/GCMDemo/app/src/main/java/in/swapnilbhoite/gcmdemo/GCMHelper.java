package in.swapnilbhoite.gcmdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by sbhoite on 5/1/2015.
 */
public class GCMHelper {
    private static final String TAG = GCMHelper.class.getSimpleName();
    private Activity activity;
    GoogleCloudMessaging gcm;
    String regid;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static GCMHelper gcmHelper = null;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String senderId = "";

    interface GCMCallback {
        public void callback(String regId);
    }

    private GCMHelper(Activity activity, String senderId) {
        this.activity = activity;
        this.senderId = senderId;
    }

    public static synchronized GCMHelper getInstance(Activity activity, String senderId) {
        if (gcmHelper == null) {
            gcmHelper = new GCMHelper(activity, senderId);
        }
        return gcmHelper;
    }

    public void getGCMId(final GCMCallback gcmCallback) {
        if (activity == null || senderId == null || senderId.isEmpty()) {
            sendCallback(gcmCallback, "");
        }

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(activity);
            regid = getRegistrationId(activity);

            final Context context = activity.getApplicationContext();
            if (regid.isEmpty()) {
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(context);
                        }
                        try {
                            regid = gcm.register(senderId);
                            storeRegistrationId(context, regid);
                            return regid;

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String regId) {
                        sendCallback(gcmCallback, regId);
                    }
                }.execute();
            } else {
                sendCallback(gcmCallback, regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            sendCallback(gcmCallback, "");
        }
    }

    private void sendCallback(GCMCallback gcmCallback, String regid) {
        if (gcmCallback != null) {
            gcmCallback.callback(regid);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return context.getSharedPreferences(this.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}