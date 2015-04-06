package com.urbanairship.phonegap;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PushAutopilot extends Autopilot {
    static final String UA_PREFIX = "com.urbanairship";
    static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    static final String IN_PRODUCTION = "com.urbanairship.in_production";
    static final String GCM_SENDER = "com.urbanairship.gcm_sender";
    static final String ENABLE_PUSH_ONLAUNCH = "com.urbanairship.enable_push_onlaunch";

    private PluginConfig pluginConfig;

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(Context context) {
        AirshipConfigOptions options = new AirshipConfigOptions();
        PluginConfig pluginConfig = getPluginConfig(context);

        // Apply any overrides from the manifest
        options.productionAppKey = pluginConfig.getString(PRODUCTION_KEY, options.productionAppKey);
        options.productionAppSecret = pluginConfig.getString(PRODUCTION_SECRET, options.productionAppSecret);
        options.developmentAppKey = pluginConfig.getString(DEVELOPMENT_KEY, options.developmentAppKey);
        options.developmentAppSecret = pluginConfig.getString(DEVELOPMENT_SECRET, options.developmentAppSecret);
        options.gcmSender = pluginConfig.getString(GCM_SENDER, options.gcmSender);
        options.inProduction = pluginConfig.getBoolean(IN_PRODUCTION, options.inProduction);

        // Set the minSDK to 14.  It just controls logging error messages for different platform features.
        options.minSdkVersion = 14;

        return options;
    }

    @Override
    public void onAirshipReady(UAirship airship) {
        final boolean enablePushOnLaunch = getPluginConfig(UAirship.getApplicationContext())
                .getBoolean(ENABLE_PUSH_ONLAUNCH, false);

        if (enablePushOnLaunch) {
            airship.getPushManager().setUserNotificationsEnabled(enablePushOnLaunch);
        }
    }

    public PluginConfig getPluginConfig(Context context) {
        if (pluginConfig == null) {
            pluginConfig = new PluginConfig(context);
        }

        return pluginConfig;
    }

    class PluginConfig {
        private Map<String, String> configValues = new HashMap<String, String>();

        PluginConfig(Context context) {
            parseConfig(context);
        }

        String getString(String key, String defaultValue) {
            return configValues.containsKey(key) ? configValues.get(key) : defaultValue;
        }

        boolean getBoolean(String key, boolean defaultValue) {
            return configValues.containsKey(key) ?
                    Boolean.parseBoolean(configValues.get(key)) : defaultValue;
        }

        private void parseConfig(Context context) {
            int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
            if (id == 0) {
                return;
            }

            XmlResourceParser xml = context.getResources().getXml(id);

            int eventType = -1;
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                if (eventType == XmlResourceParser.START_TAG) {
                    if (xml.getName().equals("preference")) {
                        String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.US);
                        String value = xml.getAttributeValue(null, "value");

                        if (name.startsWith(UA_PREFIX) && value != null) {
                            configValues.put(name, value);
                            Logger.verbose("Found " + name + " in config.xml with value: " + value);
                        }
                    }
                }

                try {
                    eventType = xml.next();
                } catch (Exception e) {
                    Logger.error("Error parsing config file", e);
                }
            }
        }

    }
}
