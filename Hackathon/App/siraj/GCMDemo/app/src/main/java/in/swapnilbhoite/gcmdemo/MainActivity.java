package in.swapnilbhoite.gcmdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "318121547513";      //project ID from google
    private static Handler handler;
    private EditText editTextGCMID;
    private static TextView textViewMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextGCMID = (EditText) findViewById(R.id.textViewGCMID);
        textViewMessages = (TextView) findViewById(R.id.textViewMessages);
        handler = new Handler(Looper.getMainLooper());
        editTextGCMID.setSelectAllOnFocus(true);

        GCMHelper.getInstance(this, SENDER_ID).getGCMId(new GCMHelper.GCMCallback() {
            @Override
            public void callback(String regId) {
                if (editTextGCMID != null) {
                    if (regId.isEmpty()) {
                        editTextGCMID.setText("Unable to generate, please check log for details");
                    } else {
                        editTextGCMID.setText(regId);
                    }
                }
            }
        });
    }

    public static void setText(final String text) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (textViewMessages != null) {
                        textViewMessages.append("\n" + text);
                    }
                }
            });
        }
    }
}