package com.cognizant.pedometer.cognizantpedometer;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;

/**
 * Created by ERZD01 on 2016-10-18.
 */

public class Settings {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private String stepsTodayKey = "stepsTodayKey";
    private String lastStepsKey = "lastStepsKey";
    private String currentDayKey = "currentDayKey";

    private String guidKey = "guidKey";
    private String usernameKey = "usernameKey";
    private String telephoneKey = "telephoneKey";
    private String emailKey = "emailKey";

    public Settings(Context context) {
        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    // stepsToday
    public int getStepsToday() {
        return sharedPreferences.getInt(stepsTodayKey, 0);
    }
    public void setStepsToday(int stepsToday) {
        sharedPreferencesEditor.putInt(stepsTodayKey, stepsToday);
        sharedPreferencesEditor.commit();
    }

    // lastSteps
    public int getLastSteps() {
        return sharedPreferences.getInt(lastStepsKey, 0);
    }
    public void setLastSteps(int lastSteps) {
        sharedPreferencesEditor.putInt(lastStepsKey, lastSteps);
        sharedPreferencesEditor.commit();
    }

    // currentDay
    public DateTime getCurrentDay() {
        String dateString = sharedPreferences.getString(currentDayKey, "");
        if (dateString.equals("")) {
            DateTime pastDate = new DateTime("2000-01-01");
            setCurrentDay(pastDate);
            return pastDate;
        }
        return new DateTime(dateString);
    }
    public void setCurrentDay(DateTime currentDay) {
        String currentDayString = currentDay.toString();
        sharedPreferencesEditor.putString(currentDayKey, currentDayString);
        sharedPreferencesEditor.commit();
    }

    // guid
    public String getGuid() {
        return sharedPreferences.getString(guidKey, "");
    }
    public void setGuid(String guid) {
        sharedPreferencesEditor.putString(guidKey, guid);
        sharedPreferencesEditor.commit();
    }

    // username
    public String getUsername() {
        return sharedPreferences.getString(usernameKey, "");
    }
    public void setUsername(String username) {
        sharedPreferencesEditor.putString(usernameKey, username);
        sharedPreferencesEditor.commit();
    }

    // telephone
    public String getTelephone() {
        return sharedPreferences.getString(telephoneKey, "");
    }
    public void setTelephone(String telephone) {
        sharedPreferencesEditor.putString(telephoneKey, telephone);
        sharedPreferencesEditor.commit();
    }

    // email
    public String getEmail() {
        return sharedPreferences.getString(emailKey, "");
    }
    public void setEmail(String email) {
        sharedPreferencesEditor.putString(emailKey, email);
        sharedPreferencesEditor.commit();
    }
}
