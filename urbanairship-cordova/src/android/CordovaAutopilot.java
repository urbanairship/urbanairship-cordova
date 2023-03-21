/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.core.app.NotificationManagerCompat;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.actions.DeepLinkListener;
import com.urbanairship.analytics.Analytics;
import com.urbanairship.channel.AirshipChannelListener;
import com.urbanairship.cordova.events.ShowInboxEvent;
import com.urbanairship.cordova.events.PreferenceCenterEvent;
import com.urbanairship.messagecenter.InboxListener;
import com.urbanairship.messagecenter.MessageCenter;
import com.urbanairship.preferencecenter.PreferenceCenter;
import com.urbanairship.push.NotificationActionButtonInfo;
import com.urbanairship.push.NotificationInfo;
import com.urbanairship.push.NotificationListener;
import com.urbanairship.push.PushListener;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.PushTokenListener;

/**
 * The Urban Airship autopilot to automatically handle takeOff.
 */
public class CordovaAutopilot extends Autopilot {

    private static final String CORDOVA_VERSION_KEY = "com.urbanairship.cordova.version";

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        AirshipConfigOptions configOptions = PluginManager.shared(context).getAirshipConfig();
        if (configOptions != null) {
            PluginLogger.setLogLevel(configOptions.logLevel);
        }
        return configOptions;
    }

    @Override
    public boolean isReady(@NonNull Context context) {
        return PluginManager.shared(context).getAirshipConfig() != null;
    }

    @Override
    public boolean allowEarlyTakeOff(@NonNull Context context) {
        return false;
    }

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        final Context context = UAirship.getApplicationContext();
        final PluginManager pluginManager = PluginManager.shared(context);

        PluginManager.NotificationsOptedOutFlag optOutFlag = pluginManager.getDisableNotificationsOnOptOut();
        if (optOutFlag != null) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                switch(optOutFlag) {
                    case ONCE:
                        if (!pluginManager.getProcessedNotificationOptOutFlag()) {
                            airship.getPushManager().setUserNotificationsEnabled(false);
                        }
                        break;
                    case ALWAYS:
                        airship.getPushManager().setUserNotificationsEnabled(false);
                        break;
                }
            }
            pluginManager.editConfig().setProcessedNotificationsOptedOutFlag(true).apply();
        }

        if (pluginManager.getEnablePushOnLaunch()) {
            airship.getPushManager().setUserNotificationsEnabled(true);
        }

        registerCordovaPluginVersion(context, airship);

        // Cordova notification provider
        airship.getPushManager().setNotificationProvider(new CordovaNotificationProvider(context, PluginManager.shared(context)));

        // Deep link
        airship.setDeepLinkListener(new DeepLinkListener() {
            @Override
            public boolean onDeepLink(@NonNull String deepLink) {
                pluginManager.deepLinkReceived(deepLink);
                return true;
            }
        });

        airship.getChannel().addChannelListener(new AirshipChannelListener() {
            @Override
            public void onChannelCreated(@NonNull String channelId) {
                PluginLogger.info("Channel created. Channel ID: %s.", channelId);
                PluginManager.shared(context).channelUpdated(channelId, true);
                PluginManager.shared(context).checkOptInStatus();
            }

            @Override
            public void onChannelUpdated(@NonNull String channelId) {
                PluginLogger.info("Channel updated. Channel ID: %s.", channelId);
                PluginManager.shared(context).channelUpdated(channelId, true);
                PluginManager.shared(context).checkOptInStatus();
            }
        });


        airship.getPushManager().addPushTokenListener(new PushTokenListener() {
            @Override
            public void onPushTokenUpdated(@NonNull String pushToken) {
                PluginLogger.info("Push token updated. Token: %s.", pushToken);
            }
        });

        airship.getPushManager().addPushListener(new PushListener() {
            @Override
            public void onPushReceived(@NonNull PushMessage message, boolean notificationPosted) {
                if (!notificationPosted) {
                    PluginLogger.info("Silent push received.");
                    PluginManager.shared(context).pushReceived(null, message);
                }
            }
        });

        airship.getPushManager().setNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationPosted(@NonNull NotificationInfo notificationInfo) {
                PluginLogger.info("Notification posted. Alert: %s. NotificationId: %s", notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
                PluginManager.shared(context).pushReceived(notificationInfo.getNotificationId(), notificationInfo.getMessage());
            }

            @Override
            public boolean onNotificationOpened(@NonNull NotificationInfo notificationInfo) {
                PluginLogger.info("Notification opened. Alert: %s. NotificationId: %s", notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
                PluginManager.shared(context).notificationOpened(notificationInfo);

                // Return false here to allow Urban Airship to auto launch the launcher activity
                return false;
            }

            @Override
            public boolean onNotificationForegroundAction(@NonNull NotificationInfo notificationInfo, @NonNull NotificationActionButtonInfo notificationActionButtonInfo) {
                PluginLogger.info("Notification action button opened. Button ID: %s. Alert: %s. NotificationId: %s", notificationActionButtonInfo.getButtonId(), notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
                PluginManager.shared(context).notificationOpened(notificationInfo, notificationActionButtonInfo);

                // Return false here to allow Urban Airship to auto launch the launcher
                // activity for foreground notification action buttons
                return false;
            }

            @Override
            public void onNotificationBackgroundAction(@NonNull NotificationInfo notificationInfo, @NonNull NotificationActionButtonInfo notificationActionButtonInfo) {
                PluginLogger.info("Notification action button opened. Button ID: %s. Alert: %s. NotificationId: %s", notificationActionButtonInfo.getButtonId(), notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
                PluginManager.shared(context).notificationOpened(notificationInfo, notificationActionButtonInfo);
            }

            @Override
            public void onNotificationDismissed(@NonNull NotificationInfo notificationInfo) {
                PluginLogger.info("Notification dismissed. Notification ID: %s.", notificationInfo.getNotificationId());
            }
        });

        // Inbox updates
        MessageCenter.shared().getInbox().addListener(new InboxListener() {
            @Override
            public void onInboxUpdated() {
                pluginManager.inboxUpdated();
            }
        });


        MessageCenter.shared().setOnShowMessageCenterListener(new MessageCenter.OnShowMessageCenterListener() {
            @Override
            public boolean onShowMessageCenter(@Nullable String messageId) {
                if (PluginManager.shared(UAirship.getApplicationContext()).getAutoLaunchMessageCenter()) {
                    return false;
                } else {
                    sendShowInboxEvent(messageId);
                    return true;
                }
            }
        });

        PreferenceCenter.shared().setOpenListener(new PreferenceCenter.OnOpenListener() {
            @Override
            public boolean onOpenPreferenceCenter(String preferenceCenterId) {
                boolean isCustomPreferenceCenterUiEnabled = pluginManager.getUseCustomPreferenceCenterUi(preferenceCenterId);
                if (isCustomPreferenceCenterUiEnabled) {
                    sendPreferenceCenterEvent(preferenceCenterId);
                    return true;
                } else {
                    return false;
                }
            }
        });

        loadCustomNotificationButtonGroups(context, airship);
        loadCustomNotificationChannels(context, airship);
    }

    private void registerCordovaPluginVersion(@NonNull Context context, @NonNull UAirship airship) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (info.metaData != null) {
                String version = info.metaData.getString(CORDOVA_VERSION_KEY, "0.0.0");
                airship.getAnalytics().registerSDKExtension(Analytics.EXTENSION_CORDOVA, version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            PluginLogger.error(e, "Failed to get package info.");
        }
    }

    private void loadCustomNotificationButtonGroups(Context context, UAirship airship) {
        @XmlRes int resId = context.getResources().getIdentifier("ua_custom_notification_buttons", "xml", context.getPackageName());

        if (resId != 0) {
            PluginLogger.debug("Loading custom notification button groups");
            airship.getPushManager().addNotificationActionButtonGroups(context, resId);
        }
    }

    private void loadCustomNotificationChannels(Context context, UAirship airship) {
        @XmlRes int resId = context.getResources().getIdentifier("ua_custom_notification_channels", "xml", context.getPackageName());

        if (resId != 0) {
            PluginLogger.debug("Loading custom notification channels");
            airship.getPushManager().getNotificationChannelRegistry().createNotificationChannels(resId);
        }
    }

    private static void sendShowInboxEvent(@NonNull String messageId) {
        Context context = UAirship.getApplicationContext();
        PluginManager.shared(context).sendShowInboxEvent(new ShowInboxEvent(messageId));
    }

    private static void sendPreferenceCenterEvent(@NonNull String preferenceCenterId) {
        Context context = UAirship.getApplicationContext();
        PluginManager.shared(context).sendPreferenceCenterEvent(new PreferenceCenterEvent(preferenceCenterId));
    }
}
