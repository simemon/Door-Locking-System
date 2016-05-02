package com.lenovo.siraj.androidgcm;

/**
 * Created by Siraj on 20-Apr-16.
 */

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    //If the token is changed registering the device again
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}
