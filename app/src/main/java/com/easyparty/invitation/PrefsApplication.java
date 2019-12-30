package com.easyparty.invitation;


import android.content.ContextWrapper;
import android.support.multidex.MultiDexApplication;
import com.pixplicity.easyprefs.library.Prefs;

public class PrefsApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }
}