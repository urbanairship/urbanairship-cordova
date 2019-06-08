/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.actions.Action;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionRegistry;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.OpenRichPushInboxAction;
import com.urbanairship.actions.OverlayRichPushMessageAction;
import com.urbanairship.messagecenter.MessageCenter;
import com.urbanairship.push.NotificationActionButtonInfo;
import com.urbanairship.push.NotificationInfo;
import com.urbanairship.push.NotificationListener;
import com.urbanairship.push.RegistrationListener;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.push.PushMessage;
import com.urbanairship.cordova.events.ShowInboxEvent;

import static com.urbanairship.cordova.PluginManager.AUTO_LAUNCH_MESSAGE_CENTER;

/**
 * The Urban Airship autopilot to automatically handle takeOff.
 */
public class CordovaAutopilot extends Autopilot {

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

        if (pluginManager.getEnablePushOnLaunch()) {
            airship.getPushManager().setUserNotificationsEnabled(true);
        }

        // Cordova notification factory
        airship.getPushManager().setNotificationFactory(new CordovaNotificationFactory(context));

        // Deep link
        airship.getActionRegistry().getEntry(DeepLinkAction.DEFAULT_REGISTRY_NAME).setDefaultAction(new DeepLinkAction() {
            @NonNull
            @Override
            public ActionResult perform(@NonNull ActionArguments arguments) {
                String deepLink = arguments.getValue().getString();
                if (deepLink != null) {
                    pluginManager.deepLinkReceived(deepLink);
                }
                return ActionResult.newResult(arguments.getValue());
            }
        });

        // Auto launch message center
        airship.getActionRegistry()
                .getEntry(OverlayRichPushMessageAction.DEFAULT_REGISTRY_NAME)
                .setPredicate(new ActionRegistry.Predicate() {
                    @Override
                    public boolean apply(ActionArguments actionArguments) {
                        if (actionArguments.getSituation() == Action.SITUATION_PUSH_OPENED) {
                            return pluginManager.getAutoLaunchMessageCenter();
                        }

                        return true;
                    }
                });

        airship.getPushManager().addRegistrationListener(new RegistrationListener() {
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

            @Override
            public void onPushTokenUpdated(@NonNull String s) {}
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
            public void onNotificationDismissed(@NonNull NotificationInfo notificationInfo) {}
        });

        // Auto launch message center
        airship.getActionRegistry()
                .getEntry(OpenRichPushInboxAction.DEFAULT_REGISTRY_NAME)
                .setPredicate(new ActionRegistry.Predicate() {
                    @Override
                    public boolean apply(ActionArguments actionArguments) {
                        if (actionArguments.getSituation() == Action.SITUATION_PUSH_OPENED) {
                            return pluginManager.getAutoLaunchMessageCenter();
                        }

                        return true;
                    }
                });

        // Inbox updates
        airship.getInbox().addListener(new RichPushInbox.Listener() {
            @Override
            public void onInboxUpdated() {
                pluginManager.inboxUpdated();
            }
        });

        airship.getMessageCenter().setOnShowMessageCenterListener(new MessageCenter.OnShowMessageCenterListener() {
            @Override
            public boolean onShowMessageCenter(@Nullable String messageId) {
                if (PreferenceManager.getDefaultSharedPreferences(UAirship.getApplicationContext()).getBoolean(AUTO_LAUNCH_MESSAGE_CENTER, true)) {
                    return false;
                } else {
                    sendShowInboxEvent(messageId);
                    return true;
                }
            }
        });

        loadCustomNotificationButtonGroups(context, airship);
    }

    private void loadCustomNotificationButtonGroups(Context context, UAirship airship) {
        String packageName = UAirship.shared().getPackageName();
        @XmlRes int resId = context.getResources().getIdentifier("ua_custom_notification_buttons", "xml", packageName);

         if (resId != 0) {
            airship.getPushManager().addNotificationActionButtonGroups(context, resId);
        }
    }
    private static void sendShowInboxEvent(@NonNull String messageId) {
        Context context = UAirship.getApplicationContext();
        PluginManager.shared(context).sendShowInboxEvent(new ShowInboxEvent(messageId));
    }
}
