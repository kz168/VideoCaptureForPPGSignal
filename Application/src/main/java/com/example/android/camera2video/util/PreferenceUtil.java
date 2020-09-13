package com.example.android.camera2video.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.camera2video.R;


public class PreferenceUtil {
    private static PreferenceUtil instance;
    private SharedPreferences sharedPref;
    private Context context;
    private static String IS_FLASH_ON = "is_flash_on";

    private PreferenceUtil(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.key_pref), Context.MODE_PRIVATE);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceUtil(context);
        }

        return instance;
    }

    public void setFlashSettings(boolean isFlashOn) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(IS_FLASH_ON, isFlashOn);
        editor.apply();
    }

    public boolean isFlashOn() {
        return sharedPref.getBoolean(IS_FLASH_ON, false);
    }
}
