package com.mio.jrdv.autobt;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by joseramondelgado on 02/11/16.
 *
 * http://stackoverflow.com/questions/13558550/can-i-get-data-from-shared-preferences-inside-a-service
 *
 * For example, if you need access to your preferences somewhere else, you may call this to read preferences:

 String str = MyApplication.preferences.getString( KEY, DEFAULT );


 Or you may call this to save something to the preferences:

 MyApplication.preferences.edit().putString( KEY, VALUE ).commit();


 (don't forget to call commit() after adding or changing preferences!)
 */

public class Myapplication extends Application {
    public static SharedPreferences preferences;
    public static  final String PREF_HORA_WIFI_OFF="08";
    public static final String PREF_BOOL_WIFIDETECT="NO";
    public static final String PREF_BOOL_ADMINYAOK="NO";
    public static final String PREF_MIN_WIFI_OFF="55";
    public static final String PREF_BOOL_AUTOWIFIOFF="NO";


    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
    }
}

