package com.cognizant.pedometer.cognizantpedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ERZD01 on 2016-10-18.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent localServiceIntent = new Intent(context, StepService.class);
        context.startService(localServiceIntent);
    }
}
