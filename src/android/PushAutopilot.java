package com.urbanairship.phonegap;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

public class PushAutopilot extends Autopilot {
    static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    static final String IN_PRODUCTION = "com.urbanairship.in_production";
    static final String GCM_SENDER = "com.urbanairship.gcm_sender";
    static final String LOCATION_ENABLED = "com.urbanairship.location_enabled";


    @Override
    public void execute(Application application) {
        UAirship.takeOff(application, gatherConfigOptions(application));

        PushManager.shared().setIntentReceiver(PushReceiver.class);
        if (UAirship.shared().getAirshipConfigOptions().pushServiceEnabled) {
            PushManager.enablePush();
        }
    }


    private AirshipConfigOptions gatherConfigOptions(Application application) {
        // Create the default options
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(application);

        //Find any overrides in the manifest
        Bundle metaData = getMetaDataBundle(application);
        if (metaData == null) {
            return options;
        }

        // Apply any overrides from the manifest
        options.productionAppKey = metaData.getString(PRODUCTION_KEY, options.productionAppKey);
        options.productionAppSecret = metaData.getString(PRODUCTION_SECRET, options.productionAppSecret);
        options.developmentAppKey = metaData.getString(DEVELOPMENT_KEY, options.developmentAppKey);
        options.developmentAppSecret = metaData.getString(DEVELOPMENT_SECRET, options.productionAppKey);
        options.inProduction = metaData.getBoolean(IN_PRODUCTION, options.inProduction);
        options.gcmSender = metaData.getString(GCM_SENDER, options.gcmSender);
        options.locationOptions.locationServiceEnabled = metaData.getBoolean(LOCATION_ENABLED, options.locationOptions.locationServiceEnabled);
        return options;

    }


    private Bundle getMetaDataBundle(Application application) {
        try {
            ApplicationInfo ai = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData;
        } catch (Exception ex) {
            Logger.error("Unable to get package info", ex);
        }
        return null;
    }
}
