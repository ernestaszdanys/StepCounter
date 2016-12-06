package com.cognizant.pedometer.cognizantpedometer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by ERZD01 on 2016-10-17.
 */

public class Utils {
    public static boolean isKitKatWithStepCounter(PackageManager pm) {
        // Require at least Android KitKat
        int currentApiVersion = Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    public static JSONObject encryptJson(JSONObject jsonObject) {

        JSONObject encryptedJsonObject = new JSONObject();
        try {
            String encrypted = new AesEncryptor().encrypt(jsonObject.toString());
            encryptedJsonObject.put("secret", encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | JSONException e) {
            e.printStackTrace();
        }

        return encryptedJsonObject;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }


}
