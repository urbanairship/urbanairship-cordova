/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.util.UAStringUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manages the plugin config.
 */
public class PluginConfig {

    private static final String PREFERENCE_NAME = "com.urbanairship.urbanairship-cordova";

    private static final String SENDER_PREFIX = "sender:";

    private static final String UA_PREFIX = "com.urbanairship";
    private static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    private static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    private static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    private static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    private static final String PRODUCTION_LOG_LEVEL = "com.urbanairship.production_log_level";
    private static final String DEVELOPMENT_LOG_LEVEL = "com.urbanairship.development_log_level";
    private static final String IN_PRODUCTION = "com.urbanairship.in_production";
    private static final String GCM_SENDER = "com.urbanairship.gcm_sender";
    private static final String ENABLE_PUSH_ONLAUNCH = "com.urbanairship.enable_push_onlaunch";
    private static final String NOTIFICATION_ICON = "com.urbanairship.notification_icon";
    private static final String NOTIFICATION_LARGE_ICON = "com.urbanairship.notification_large_icon";
    private static final String NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color";
    private static final String NOTIFICATION_SOUND = "com.urbanairship.notification_sound";
    private static final String AUTO_LAUNCH_MESSAGE_CENTER = "com.urbanairship.auto_launch_message_center";
    private static final String ENABLE_ANALYTICS = "com.urbanairship.enable_analytics";

    private static PluginConfig instance;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private AirshipConfigOptions configOptions;
    private Map<String, String> defaultConfigValues = new HashMap<String, String>();

    private PluginConfig(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets the shared instance.
     *
     * @param context The context.
     * @return The shared instance.
     */
    public synchronized static PluginConfig shared(Context context) {
        if (instance == null) {
            instance = new PluginConfig(context);
            instance.parseConfig(context);
        }

        return instance;
    }

    /**
     * Gets the airship config for this instance.
     *
     * @return The airship config if available, or null if the plugin is not configured.
     */
    public AirshipConfigOptions getAirshipConfig() {
        if (configOptions != null) {
            return configOptions;
        }

        if (!containsKey(DEVELOPMENT_KEY) && !containsKey(DEVELOPMENT_SECRET) && !containsKey(PRODUCTION_KEY) && !containsKey(PRODUCTION_SECRET)) {
            return null;
        }

        AirshipConfigOptions.Builder builder = new AirshipConfigOptions.Builder()
                .setDevelopmentAppKey(getString(DEVELOPMENT_KEY, ""))
                .setDevelopmentAppSecret(getString(DEVELOPMENT_SECRET, ""))
                .setProductionAppKey(getString(PRODUCTION_KEY, ""))
                .setProductionAppSecret(getString(PRODUCTION_SECRET, ""))
                .setFcmSenderId(parseSender(getString(GCM_SENDER, null)))
                .setAnalyticsEnabled(getBoolean(ENABLE_ANALYTICS, true))
                .setDevelopmentLogLevel(parseLogLevel(getString(DEVELOPMENT_LOG_LEVEL, ""), Log.DEBUG))
                .setProductionLogLevel(parseLogLevel(getString(PRODUCTION_LOG_LEVEL, ""), Log.ERROR))
                .setNotificationIcon(getResource(NOTIFICATION_ICON, "drawable"))
                .setNotificationAccentColor(getColor(NOTIFICATION_ACCENT_COLOR, 0));

        if (containsKey(IN_PRODUCTION)) {
            builder.setInProduction(getBoolean(IN_PRODUCTION, false));
        } else {
            builder.detectProvisioningMode(context);
        }

        try {
            configOptions = builder.build();
            return configOptions;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets the enable push on launch option.
     *
     * @return {@code true} to enable push on launch, otherwise {@code false}.
     */
    public boolean getEnablePushOnLaunch() {
        return getBoolean(ENABLE_PUSH_ONLAUNCH, false);
    }

    /**
     * Gets the auto launch message center option.
     *
     * @return {@code true} to auto launch message center from a push, otherwise {@code false}.
     */
    public boolean getAutoLaunchMessageCenter() {
        return getBoolean(AUTO_LAUNCH_MESSAGE_CENTER, true);
    }

    /**
     * Gets the notification sound.
     *
     * @return The notification sound.
     */
    @Nullable
    public Uri getNotificationSound() {
        int resource = getResource(NOTIFICATION_SOUND, "raw");
        if (resource != 0) {
            return Uri.parse("android.resource://" + context.getPackageName() + "/" + resource);
        }

        return null;
    }

    /**
     * Gets the notification large icon.
     *
     * @return The notification large icon.
     */
    public int getNotificationLargeIcon() {
        return getResource(NOTIFICATION_LARGE_ICON, "drawable");
    }


    /**
     * Edits the config.
     *
     * @return A config editor.
     */
    public ConfigEditor editConfig() {
        return new ConfigEditor(sharedPreferences.edit());
    }

    /**
     * Convert the log level string to an int.
     *
     * @param logLevel        The log level as a string.
     * @param defaultLogLevel Default log level.
     * @return The log level.
     */
    public static int parseLogLevel(String logLevel, int defaultLogLevel) {
        if (logLevel == null || logLevel.length() == 0) {
            return defaultLogLevel;
        }
        String logString = logLevel.trim().toLowerCase();
        if (logString.equals("verbose")) {
            return Log.VERBOSE;
        } else if (logString.equals("debug")) {
            return Log.DEBUG;
        } else if (logString.equals("info")) {
            return Log.INFO;
        } else if (logString.equals("warn")) {
            return Log.WARN;
        } else if (logString.equals("error")) {
            return Log.ERROR;
        } else if (logString.equals("none")) {
            return Log.ASSERT;
        } else {
            return defaultLogLevel;
        }
    }

    /**
     * Parses the sender ID.
     *
     * @param value The value from config.
     * @return The sender ID.
     */
    private String parseSender(String value) {
        if (value == null) {
            return null;
        }

        if (value.startsWith("sender:")) {
            return value.substring(SENDER_PREFIX.length());
        }

        return value;
    }

    /**
     * Gets a String value from the config.
     *
     * @param key          The config key.
     * @param defaultValue Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    private String getString(String key, String defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Gets a Boolean value from the config.
     *
     * @param key          The config key.
     * @param defaultValue Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    private boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Gets a color value from the config.
     *
     * @param key The config key.
     * @return The parsed color, or defaultColor.
     */
    private int getColor(String key, int defaultColor) {
        String color = getValue(key);
        if (!UAStringUtil.isEmpty(color)) {
            try {
                return Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Logger.error("Unable to parse color: " + color, e);
            }
        }
        return defaultColor;
    }

    private int getResource(String key, String resourceFolder) {
        String resourceName = getString(key, null);
        if (!UAStringUtil.isEmpty(resourceName)) {
            int id = context.getResources().getIdentifier(resourceName, resourceFolder, context.getPackageName());
            if (id != 0) {
                return id;
            } else {
                Logger.error("Unable to find resource with name: " + resourceName);
            }
        }
        return 0;
    }

    private boolean containsKey(String key) {
        return getValue(key) != null;
    }

    private String getValue(String key) {
        return sharedPreferences.getString(key, defaultConfigValues.get(key));
    }

    /**
     * Parses the config.xml file.
     *
     * @param context The application context.
     */
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
                        defaultConfigValues.put(name, value);
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

    /**
     * Config editor.
     */
    public class ConfigEditor {
        private final SharedPreferences.Editor editor;

        private ConfigEditor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        /**
         * Sets the production app key and secret.
         *
         * @param appKey    The app key.
         * @param appSecret The app secret.
         * @return The config editor.
         */
        public ConfigEditor setProductionConfig(@NonNull String appKey, @NonNull String appSecret) {
            editor.putString(PRODUCTION_KEY, appKey)
                    .putString(PRODUCTION_SECRET, appSecret);
            return this;
        }

        /**
         * Sets the production log level.
         *
         * @param logLevel The log level.
         * @return The config editor.
         */
        public ConfigEditor setProductionLogLevel(@Nullable String logLevel) {
            editor.putString(PRODUCTION_LOG_LEVEL, logLevel);
            return this;
        }

        /**
         * Sets the development app key and secret.
         *
         * @param appKey    The app key.
         * @param appSecret The app secret.
         * @return The config editor.
         */
        public ConfigEditor setDevelopmentConfig(@NonNull String appKey, @NonNull String appSecret) {
            editor.putString(DEVELOPMENT_KEY, appKey)
                    .putString(DEVELOPMENT_SECRET, appSecret);
            return this;
        }

        /**
         * Sets the development log level.
         *
         * @param logLevel The log level.
         * @return The config editor.
         */
        public ConfigEditor setDevelopmentLogLevel(@Nullable String logLevel) {
            editor.putString(DEVELOPMENT_LOG_LEVEL, logLevel);
            return this;
        }

        /**
         * Sets the notification icon.
         *
         * @param icon The icon name.
         * @return The config editor.
         */
        public ConfigEditor setNotificationIcon(String icon) {
            editor.putString(NOTIFICATION_ICON, icon);
            return this;
        }

        /**
         * Sets the notification large icon.
         *
         * @param largeIcon The icon name.
         * @return The config editor.
         */
        public ConfigEditor setNotificationLargeIcon(String largeIcon) {
            editor.putString(NOTIFICATION_LARGE_ICON, largeIcon);
            return this;
        }

        /**
         * Sets the notification accent color.
         *
         * @param accentColor The accent color.
         * @return The config editor.
         */
        public ConfigEditor setNotificationAccentColor(String accentColor) {
            editor.putString(NOTIFICATION_ACCENT_COLOR, accentColor);
            return this;
        }

        /**
         * Sets auto launch message center option.
         *
         * @param autoLaunchMessageCenter {@code true} to enable auto launching the message enter, otherwise {@code false}.
         * @return The config editor.
         */
        public ConfigEditor setAutoLaunchMessageCenter(boolean autoLaunchMessageCenter) {
            editor.putString(AUTO_LAUNCH_MESSAGE_CENTER, Boolean.toString(autoLaunchMessageCenter));
            return this;
        }

        /**
         * Applies the changes.
         */
        void apply() {
            editor.apply();
        }
    }
}
