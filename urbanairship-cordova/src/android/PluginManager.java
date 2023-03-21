/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.PrivacyManager;
import com.urbanairship.UAirship;
import com.urbanairship.cordova.events.DeepLinkEvent;
import com.urbanairship.cordova.events.Event;
import com.urbanairship.cordova.events.InboxEvent;
import com.urbanairship.cordova.events.NotificationOpenedEvent;
import com.urbanairship.cordova.events.NotificationOptInEvent;
import com.urbanairship.cordova.events.PreferenceCenterEvent;
import com.urbanairship.cordova.events.PushEvent;
import com.urbanairship.cordova.events.RegistrationEvent;
import com.urbanairship.cordova.events.ShowInboxEvent;
import com.urbanairship.push.NotificationActionButtonInfo;
import com.urbanairship.push.NotificationInfo;
import com.urbanairship.push.PushMessage;
import com.urbanairship.util.UAStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Plugin manager.
 */
public class PluginManager {

    enum NotificationsOptedOutFlag {
        ALWAYS,
        ONCE
    }

    /**
     * Interface when a new event is received.
     */
    public interface Listener {
        void onEvent(@NonNull Event event);
    }

    private static final String PREFERENCE_FILE = "com.urbanairship.ua_plugin_shared_preferences";

    private static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    private static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    private static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    private static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    private static final String PRODUCTION_LOG_LEVEL = "com.urbanairship.production_log_level";
    private static final String DEVELOPMENT_LOG_LEVEL = "com.urbanairship.development_log_level";
    private static final String IN_PRODUCTION = "com.urbanairship.in_production";
    private static final String ENABLE_PUSH_ONLAUNCH = "com.urbanairship.enable_push_onlaunch";

    private static final String DISABLE_ANDROID_NOTIFICATIONS_ON_OPT_OUT = "com.urbanairship.android.disable_user_notifications_on_system_opt_out";
    private static final String PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG = "com.urbanairship.PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG";

    private static final String NOTIFICATION_ICON = "com.urbanairship.notification_icon";
    private static final String NOTIFICATION_LARGE_ICON = "com.urbanairship.notification_large_icon";
    private static final String NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color";
    private static final String NOTIFICATION_SOUND = "com.urbanairship.notification_sound";
    static final String AUTO_LAUNCH_MESSAGE_CENTER = "com.urbanairship.auto_launch_message_center";
    private static final String ENABLE_ANALYTICS = "com.urbanairship.enable_analytics";
    private static final String CLOUD_SITE = "com.urbanairship.site";
    private static final String FCM_FIREBASE_APP_NAME = "com.urbanairship.fcm_firebase_app_name";
    private static final String INITIAL_CONFIG_URL = "com.urbanairship.initial_config_url";

    private static final String NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY = "com.urbanairship.notification_opt_in_status_preferences";

    private static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "com.urbanairship.default_notification_channel_id";
    private static final String FOREGROUND_NOTIFICATIONS = "com.urbanairship.foreground_notifications";

    private static PluginManager instance;
    private final Object lock = new Object();

    private NotificationOpenedEvent notificationOpenedEvent;
    private DeepLinkEvent deepLinkEvent = null;
    private Listener listener = null;
    private final List<Event> pendingEvents = new ArrayList<Event>();

    private final SharedPreferences sharedPreferences;
    private final Map<String, String> defaultConfigValues;
    private final Context context;
    private AirshipConfigOptions configOptions;
    private boolean isAirshipAvailable = false;

    private PluginManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        this.defaultConfigValues = ConfigUtils.parseConfigXml(context);
    }

    /**
     * Gets the shared instance.
     *
     * @param context The context.
     * @return The shared instance.
     */
    public synchronized static PluginManager shared(@NonNull Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }

        return instance;
    }

    /**
     * Called when the inbox is updated.
     */
    public void inboxUpdated() {
        notifyListener(new InboxEvent());
    }

    /**
     * Called when a push is received.
     *
     * @param notificationId The notification ID.
     * @param pushMessage    The push message.
     */
    public void pushReceived(@Nullable Integer notificationId, @NonNull PushMessage pushMessage) {
        notifyListener(new PushEvent(notificationId, pushMessage));
    }

    /**
     * Called when a new deep link is received.
     *
     * @param deepLink The deep link.
     */
    public void deepLinkReceived(@NonNull String deepLink) {
        synchronized (lock) {
            DeepLinkEvent event = new DeepLinkEvent(deepLink);
            this.deepLinkEvent = event;

            if (!notifyListener(event)) {
                pendingEvents.add(event);
            }
        }
    }

    /**
     * Called to open the inbox when auto launch is disabled.
     *
     * @param showInboxEvent The show inbox event.
     */
    public void sendShowInboxEvent(@NonNull ShowInboxEvent showInboxEvent) {
        synchronized (lock) {
            if (!notifyListener(showInboxEvent)) {
                pendingEvents.add(showInboxEvent);
            }
        }
    }

    /**
     * Called to open the preference center when use custom UI is disabled.
     *
     * @param preferenceCenterEvent .
     */
    public void sendPreferenceCenterEvent(@NonNull PreferenceCenterEvent preferenceCenterEvent) {
        synchronized (lock) {
            if (!notifyListener(preferenceCenterEvent)) {
                pendingEvents.add(preferenceCenterEvent);
            }
        }
    }

    /**
     * Called on app resume and when registration changes.
     */
    public void checkOptInStatus() {
        boolean optIn = UAirship.shared().getPushManager().isOptIn();

        // Check preferences for opt-in
        if (sharedPreferences.getBoolean(NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY, false) != optIn) {
            sharedPreferences.edit()
                    .putBoolean(NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY, optIn)
                    .apply();
            notifyListener(new NotificationOptInEvent(optIn));
        }
    }

    /**
     * Gets the default notification channel ID.
     *
     * @return The default notification channel ID.
     */
    @Nullable
    public String getDefaultNotificationChannelId() {
        return sharedPreferences.getString(DEFAULT_NOTIFICATION_CHANNEL_ID, null);
    }

    public boolean getUseCustomPreferenceCenterUi(@NonNull String preferenceCenterId) {
        return sharedPreferences.getBoolean(useCustomPreferenceCenterUiKey(preferenceCenterId), false);
    }

    public boolean isForegroundNotificationsEnabled() {
        return sharedPreferences.getBoolean(FOREGROUND_NOTIFICATIONS, true);
    }

    private static String useCustomPreferenceCenterUiKey(@NonNull String preferenceCenterId) {
        return "preference_" + preferenceCenterId + "_use_custom_ui";
    }

    /**
     * Called when the notification is opened.
     *
     * @param notificationInfo The notification info.
     */
    public void notificationOpened(@NonNull NotificationInfo notificationInfo) {
        notificationOpened(new NotificationOpenedEvent(notificationInfo));
    }

    /**
     * Called when the notification is opened.
     *
     * @param notificationInfo The notification info.
     */
    public void notificationOpened(@NonNull NotificationInfo notificationInfo, @Nullable NotificationActionButtonInfo actionButtonInfo) {
        notificationOpened(new NotificationOpenedEvent(notificationInfo, actionButtonInfo));
    }

    private void notificationOpened(@NonNull NotificationOpenedEvent event) {
        synchronized (lock) {
            this.notificationOpenedEvent = event;

            if (!notifyListener(event)) {
                pendingEvents.add(event);
            }
        }
    }

    /**
     * Called when the channel is updated.
     *
     * @param channel The channel ID.
     * @param success {@code true} if the channel updated successfully, otherwise {@code false}.
     */
    public void channelUpdated(@Nullable String channel, boolean success) {
        notifyListener(new RegistrationEvent(channel, UAirship.shared().getPushManager().getPushToken(), success));
    }

    /**
     * Returns {@code true} if Airship is available, i.e., you can call UAirship.shared() and
     * it should return an instance.
     *
     * @return {@code true} if airship is available, otherwise {@code false}.
     */
    public boolean isAirshipAvailable() {
        if (isAirshipAvailable) {
            return true;
        }

        if (getAirshipConfig() == null) {
            return false;
        }

        if (UAirship.isFlying() || UAirship.isTakingOff()) {
            isAirshipAvailable = true;
            return true;
        }

        try {
            UAirship.shared();
            isAirshipAvailable = true;
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return isAirshipAvailable;
    }

    /**
     * Returns the last deep link event.
     *
     * @param clear {@code true} to clear the event, otherwise {@code false}.
     * @return The deep link event, or null if the event is not available.
     */
    @Nullable
    public DeepLinkEvent getLastDeepLinkEvent(boolean clear) {
        synchronized (lock) {
            DeepLinkEvent event = this.deepLinkEvent;
            if (clear) {
                this.deepLinkEvent = null;
            }

            return event;
        }
    }

    /**
     * Returns the last notification opened event.
     *
     * @param clear {@code true} to clear the event, otherwise {@code false}.
     * @return The notification opened event, or null if the event is not available.
     */
    @Nullable
    public NotificationOpenedEvent getLastLaunchNotificationEvent(boolean clear) {
        synchronized (lock) {
            NotificationOpenedEvent event = this.notificationOpenedEvent;
            if (clear) {
                this.notificationOpenedEvent = null;
            }

            return event;
        }
    }

    /**
     * Sets the event listener.
     *
     * @param listener The event listener.
     */
    public void setListener(@Nullable Listener listener) {
        synchronized (lock) {
            this.listener = listener;

            if (listener != null && !pendingEvents.isEmpty()) {
                for (Event event : pendingEvents) {
                    notifyListener(event);
                }
                pendingEvents.clear();
            }
        }
    }

    /**
     * Gets the airship config for this instance.
     *
     * @return The airship config if available, or null if the plugin is not configured.
     */
    @SuppressLint("RestrictedApi")
    @Nullable
    public AirshipConfigOptions getAirshipConfig() {
        if (configOptions != null) {
            return configOptions;
        }

        if (!hasConfig(DEVELOPMENT_KEY) && !hasConfig(DEVELOPMENT_SECRET) && !hasConfig(PRODUCTION_KEY) && !hasConfig(PRODUCTION_SECRET)) {
            return null;
        }

        AirshipConfigOptions.Builder builder = new AirshipConfigOptions.Builder()
                .setDevelopmentAppKey(getConfigString(DEVELOPMENT_KEY, ""))
                .setDevelopmentAppSecret(getConfigString(DEVELOPMENT_SECRET, ""))
                .setProductionAppKey(getConfigString(PRODUCTION_KEY, ""))
                .setProductionAppSecret(getConfigString(PRODUCTION_SECRET, ""))
                .setAnalyticsEnabled(getConfigBoolean(ENABLE_ANALYTICS, true))
                .setDevelopmentLogLevel(ConfigUtils.parseLogLevel(getConfigString(DEVELOPMENT_LOG_LEVEL, ""), Log.DEBUG))
                .setProductionLogLevel(ConfigUtils.parseLogLevel(getConfigString(PRODUCTION_LOG_LEVEL, ""), Log.ERROR))
                .setSite(ConfigUtils.parseCloudSite(getConfigValue(CLOUD_SITE)))
                .setEnabledFeatures(PrivacyManager.FEATURE_ALL)
                .setUrlAllowListScopeOpenUrl(new String[]{"*"});
        
        if (hasConfig(FCM_FIREBASE_APP_NAME)) {
            builder.setFcmFirebaseAppName(getConfigString(FCM_FIREBASE_APP_NAME, ""));
        }

        if (hasConfig(INITIAL_CONFIG_URL)) {
            builder.setInitialConfigUrl(getConfigString(INITIAL_CONFIG_URL, null));
        }
        
        if (hasConfig(IN_PRODUCTION)) {
            builder.setInProduction(getConfigBoolean(IN_PRODUCTION, false));
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
        return getConfigBoolean(ENABLE_PUSH_ONLAUNCH, false);
    }

    @Nullable
    public NotificationsOptedOutFlag getDisableNotificationsOnOptOut() {
        String disableNotifications = getConfigString(DISABLE_ANDROID_NOTIFICATIONS_ON_OPT_OUT, null);
        if (UAStringUtil.isEmpty(disableNotifications)) {
            return null;
        }

        switch (disableNotifications.toLowerCase(Locale.ROOT).trim()) {
            case "once":
                return NotificationsOptedOutFlag.ONCE;
            case "always":
                return NotificationsOptedOutFlag.ALWAYS;
            default:
                PluginLogger.error("Invalid value for %s: %s", DISABLE_ANDROID_NOTIFICATIONS_ON_OPT_OUT, disableNotifications);
        }

        return null;
    }

    boolean getProcessedNotificationOptOutFlag() {
        return sharedPreferences.getBoolean(PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG, false);
    }

    /**
     * Gets the auto launch message center option.
     *
     * @return {@code true} to enable auto launching the  message center, otherwise {@code false}.
     */
    public boolean getAutoLaunchMessageCenter() {
        return getConfigBoolean(AUTO_LAUNCH_MESSAGE_CENTER, true);
    }

    /**
     * Gets the notification sound.
     *
     * @return The notification sound.
     */
    @Nullable
    public Uri getNotificationSound() {
        int resource = getConfigResource(NOTIFICATION_SOUND, "raw");
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
        return getConfigResource(NOTIFICATION_LARGE_ICON, "drawable");
    }

    /**
     * Gets the notification accent color.
     *
     * @return The notification accent color.
     */
    public int getNotificationAccentColor() {
        return getConfigColor(NOTIFICATION_ACCENT_COLOR, Color.GRAY);
    }

    /**
     * Gets the notification icon.
     *
     * @return The notification icon.
     */
    public int getNotificationIcon() {
        return getConfigResource(NOTIFICATION_ICON, "drawable");
    }

    /**
     * Creates a config editor.
     *
     * @return A config editor.
     */
    @NonNull
    public ConfigEditor editConfig() {
        return new ConfigEditor(sharedPreferences.edit());
    }


    /**
     * Gets a String value from the config.
     *
     * @param key          The config key.
     * @param defaultValue Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    private String getConfigString(@NonNull String key, String defaultValue) {
        String value = getConfigValue(key);
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
    private boolean getConfigBoolean(String key, boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Gets a color value from the config.
     *
     * @param key The config key.
     * @param defaultColor Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    @ColorInt
    private int getConfigColor(@NonNull String key, @ColorInt int defaultColor) {
        String color = getConfigValue(key);
        if (!UAStringUtil.isEmpty(color)) {
            try {
                return Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                PluginLogger.error(e, "Unable to parse color: %s", color);
            }
        }
        return defaultColor;
    }



    /**
     * Gets a resource value from the config.
     *
     * @param key The config key.
     * @param key The resource folder.
     * @return The resource ID or 0 if not found.
     */
    private int getConfigResource(@NonNull String key, @NonNull String resourceFolder) {
        String resourceName = getConfigString(key, null);
        if (!UAStringUtil.isEmpty(resourceName)) {
            int id = context.getResources().getIdentifier(resourceName, resourceFolder, context.getPackageName());
            if (id != 0) {
                return id;
            } else {
                PluginLogger.error("Unable to find resource with name: %s", resourceName);
            }
        }
        return 0;
    }

    /**
     * Checks if a config value is defined.
     *
     * @param key The key.
     * @return {@code true} if the value is not null, otherwise {@code false}.
     */
    private boolean hasConfig(@NonNull String key) {
        return getConfigValue(key) != null;
    }

    /**
     * Gets a config value.
     *
     * @param key The key.
     * @return The config value if it exists, otherwise the default config value.
     */
    @Nullable
    private String getConfigValue(@NonNull String key) {
        return sharedPreferences.getString(key, defaultConfigValues.get(key));
    }



    /**
     * Helper method to notify the listener of the event.
     *
     * @param event The event.
     * @return {@code true} if the listener was notified, otherwise {@code false}.
     */
    private boolean notifyListener(@NonNull Event event) {
        synchronized (lock) {
            if (listener != null) {
                listener.onEvent(event);
                return true;
            }
            return false;
        }
    }


    /**
     * Config editor.
     */
    public class ConfigEditor {
        private final SharedPreferences.Editor editor;

        private ConfigEditor(@NonNull SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        /**
         * Sets the production app key and secret.
         *
         * @param appKey    The app key.
         * @param appSecret The app secret.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setProductionConfig(@NonNull String appKey, @NonNull String appSecret) {
            editor.putString(PRODUCTION_KEY, appKey)
                    .putString(PRODUCTION_SECRET, appSecret);
            return this;
        }

        /**
         * Sets the development app key and secret.
         *
         * @param appKey    The app key.
         * @param appSecret The app secret.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setDevelopmentConfig(@NonNull String appKey, @NonNull String appSecret) {
            editor.putString(DEVELOPMENT_KEY, appKey)
                    .putString(DEVELOPMENT_SECRET, appSecret);
            return this;
        }

        /**
         * Sets the notification icon.
         *
         * @param icon The icon name.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setNotificationIcon(@Nullable String icon) {
            if (icon == null) {
                editor.remove(NOTIFICATION_ICON);
            } else {
                editor.putString(NOTIFICATION_ICON, icon);
            }
            return this;
        }

        /**
         * Sets the notification large icon.
         *
         * @param largeIcon The icon name.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setNotificationLargeIcon(String largeIcon) {
            if (largeIcon == null) {
                editor.remove(NOTIFICATION_LARGE_ICON);
            } else {
                editor.putString(NOTIFICATION_LARGE_ICON, largeIcon);
            }
            return this;
        }

        /**
         * Sets the notification accent color.
         *
         * @param accentColor The accent color.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setNotificationAccentColor(String accentColor) {
            if (accentColor == null) {
                editor.remove(NOTIFICATION_ACCENT_COLOR);
            } else {
                editor.putString(NOTIFICATION_ACCENT_COLOR, accentColor);
            }
            return this;
        }

        /**
         * Sets the default notification channel ID.
         *
         * @param value The string value.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setDefaultNotificationChannelId(@Nullable String value) {
            if (value == null) {
                editor.remove(DEFAULT_NOTIFICATION_CHANNEL_ID);
            } else {
                editor.putString(DEFAULT_NOTIFICATION_CHANNEL_ID, value);
            }
            return this;
        }

        /**
         * Sets auto launch message center option.
         *
         * @param autoLaunchMessageCenter {@code true} to enable auto launching the message center, otherwise {@code false}.
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setAutoLaunchMessageCenter(boolean autoLaunchMessageCenter) {
            editor.putString(AUTO_LAUNCH_MESSAGE_CENTER, Boolean.toString(autoLaunchMessageCenter));
            return this;
        }

        @NonNull
        public ConfigEditor setForegroundNotificationsEnabled(boolean allow) {
            editor.putBoolean(FOREGROUND_NOTIFICATIONS, allow);
            return this;
        }

        @NonNull
        public ConfigEditor setProcessedNotificationsOptedOutFlag(boolean optedNotificationsOut) {
            editor.putBoolean(PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG, optedNotificationsOut);
            return this;
        }

        /**
         * Sets the cloud site.
         *
         * @param site The string value for the site, either "US" or "EU".
         * @return The config editor.
         */
        @NonNull
        public ConfigEditor setCloudSite(String site) {
            editor.putString(CLOUD_SITE, site);
            return this;
        }

        @NonNull
        public ConfigEditor setUseCustomPreferenceCenterUi(@NonNull String preferenceCenterId, boolean useCustomUi) {
            editor.putBoolean(useCustomPreferenceCenterUiKey(preferenceCenterId), useCustomUi);
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