package com.urbanairship.phonegap.sample;

import android.app.Application;
import android.content.Context;

import com.urbanairship.UAirship;
import com.urbanairship.phonegap.plugins.PushNotificationPluginIntentReceiver;
import com.urbanairship.push.PushManager;

public class MainApplication extends Application {

    final static String TAG = MainApplication.class.getSimpleName();
    private static MainApplication instance = new MainApplication();

    public MainApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UAirship.takeOff(this);
        if (UAirship.shared().getAirshipConfigOptions().pushServiceEnabled) {
            PushManager.enablePush();
            PushManager.shared().setIntentReceiver(PushNotificationPluginIntentReceiver.class);
        }
    }

    public void onStop() {
        UAirship.land();
    }
}
