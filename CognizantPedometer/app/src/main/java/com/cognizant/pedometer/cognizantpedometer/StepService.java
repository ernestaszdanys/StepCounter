package com.cognizant.pedometer.cognizantpedometer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;

/**
 * Created by erzd01 on 2016-10-13.
 */

public class StepService extends Service implements SensorEventListener {
    // Binder given to clients
    private final IBinder mBinder = new StepServiceBinder();

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;

    static final public String SERVICE_RESULT = "com.cognizant.pedometer.cognizantpedometer.REQUEST_PROCESSED";
    static final public String SERVICE_STEPS = "com.cognizant.pedometer.cognizantpedometer.C_STEPS";
    private LocalBroadcastManager broadcaster;

    private Settings settings;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();

        //check if kit kat can sensor compatible
        if (!Utils.isKitKatWithStepCounter(getPackageManager())) {
            System.out.println("Not compatible with sensors, stopping service.");
            stopSelf();
            return;
        }

        broadcaster = LocalBroadcastManager.getInstance(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        settings = new Settings(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendResult();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class StepServiceBinder extends Binder {
        StepService getService() {
            // Return this instance of StepService so clients can call public methods
            return StepService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendResult() {
        Intent intent = new Intent(SERVICE_RESULT);
        intent.putExtra(SERVICE_STEPS, String.valueOf(settings.getStepsToday()));
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = values.length > 0 ? (int) values[0] : 0;

        DateTime now = new DateTime();
        DateTime currentDay = settings.getCurrentDay();
        if (!(now.getDayOfYear() == currentDay.getDayOfYear() && now.getYear() == currentDay.getYear())) {
            settings.setLastSteps(0);
            settings.setStepsToday(0);
            settings.setCurrentDay(new DateTime());
        }

        if (!(9 <= now.getHourOfDay() && now.getHourOfDay() < 19)) {
            return;
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int lastSteps = settings.getLastSteps();
            if (lastSteps < 1 || value < lastSteps) {
                lastSteps = value;
            }

            int newSteps = value - lastSteps;
            settings.setLastSteps(value);
            settings.setStepsToday(settings.getStepsToday() + newSteps);


            if (settings.getStepsToday() % 10 == 0) {
                JSONObject encryptedJsonObject = getEncryptedJson();
                if (Utils.isConnected(getApplicationContext())) {
                    HttpService httpService = new HttpService("PUT", encryptedJsonObject) {
                        @Override
                        public void onResponseReceived(Response response) {
                        }
                    };
                    httpService.execute();
                }
            }
            sendResult();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private JSONObject getEncryptedJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", settings.getGuid());
            jsonObject.put("steps", settings.getStepsToday());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Utils.encryptJson(jsonObject);
    }
}
