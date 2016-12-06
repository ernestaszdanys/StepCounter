package com.cognizant.pedometer.cognizantpedometer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    StepService mService;
    boolean mBound = false;

    private TextView stepsTodayTextView, usernameTextView;

    private BroadcastReceiver receiver;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Utils.isKitKatWithStepCounter(getPackageManager())) {
            Toast.makeText(getApplicationContext(), "Unsupported device", Toast.LENGTH_SHORT).show();
        }

        settings = new Settings(getApplicationContext());

        // Start create account activity
        if (settings.getGuid().equals("")) {
            Intent createAccountIntent = new Intent(this, CreateAccountActivity.class);
            startActivity(createAccountIntent);
            finish();
            return;
        }

        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        usernameTextView.setText(settings.getUsername());
        stepsTodayTextView = (TextView) findViewById(R.id.stepsTodayTextView);
        stepsTodayTextView.setText(String.valueOf(settings.getStepsToday()));
        stepsTodayTextView.setText(String.valueOf(settings.getStepsToday()));

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String stepsToday = intent.getStringExtra(StepService.SERVICE_STEPS);
                stepsTodayTextView.setText(stepsToday);
            }
        };
    }

    private void StartStepService()
    {
        try
        {
            Intent intent = new Intent (this, StepService.class);
            startService(intent);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!Utils.isKitKatWithStepCounter(getPackageManager())) {
            Toast.makeText(getApplicationContext(), "Unsupported device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isMyServiceRunning(StepService.class)) {
            StartStepService();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(StepService.SERVICE_RESULT)
        );

        if (mBound) {
            return;
        }

        // Bind to StepService
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to StepService, cast the IBinder and get StepService instance
            StepService.StepServiceBinder binder = (StepService.StepServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
