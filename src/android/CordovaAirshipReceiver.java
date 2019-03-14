/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.support.annotation.NonNull;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;

/**
 * Intent receiver for Urban Airship channel and push events.
 */
public class CordovaAirshipReceiver extends AirshipReceiver {


    @Override
    protected void onChannelCreated(@NonNull Context context, @NonNull String channelId) {
        PluginLogger.info("Channel created. Channel ID: %s.", channelId);
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelUpdated(@NonNull Context context, @NonNull String channelId) {
        PluginLogger.info("Channel updated. Channel ID: %s.", channelId);
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelRegistrationFailed(@NonNull Context context) {
        PluginLogger.info("Channel registration failed.");
        PluginManager.shared(context).channelUpdated(UAirship.shared().getPushManager().getChannelId(), false);
    }

    @Override
    protected void onPushReceived(@NonNull Context context, @NonNull PushMessage message, boolean notificationPosted) {
        PluginLogger.info("Received push message. Alert: %s. Posted notification: %s.", message.getAlert(), notificationPosted);

        if (!notificationPosted) {
            PluginManager.shared(context).pushReceived(null, message);
        }
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        PluginLogger.info("Notification posted. Alert: %s. NotificationId: %s", notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
        PluginManager.shared(context).pushReceived(notificationInfo.getNotificationId(), notificationInfo.getMessage());
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        PluginLogger.info("Notification opened. Alert: %s. NotificationId: %s", notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
        PluginManager.shared(context).notificationOpened(notificationInfo);

        // Return false here to allow Urban Airship to auto launch the launcher activity
        return false;
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo, @NonNull ActionButtonInfo actionButtonInfo) {
        PluginLogger.info("Notification action button opened. Button ID: %s. Alert: %s. NotificationId: %s", actionButtonInfo.getButtonId(), notificationInfo.getMessage().getAlert(), notificationInfo.getNotificationId());
        PluginManager.shared(context).notificationOpened(notificationInfo, actionButtonInfo);

        // Return false here to allow Urban Airship to auto launch the launcher
        // activity for foreground notification action buttons
        return false;
    }
}
