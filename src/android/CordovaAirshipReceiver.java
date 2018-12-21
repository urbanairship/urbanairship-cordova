/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;

/**
 * Intent receiver for Urban Airship channel and push events.
 */
public class CordovaAirshipReceiver extends AirshipReceiver {

    private static final String TAG = "CordovaAirshipReceiver";

    @Override
    protected void onChannelCreated(Context context, String channelId) {
        Log.i(TAG, "Channel created. Channel ID: " + channelId + ".");
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelUpdated(Context context, String channelId) {
        Log.i(TAG, "Channel updated. Channel ID: " + channelId + ".");
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(TAG, "Channel registration failed.");
        PluginManager.shared(context).channelUpdated(UAirship.shared().getPushManager().getChannelId(), false);
    }

    @Override
    protected void onPushReceived(@NonNull Context context, @NonNull PushMessage message, boolean notificationPosted) {
        Log.i(TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);


        if (!notificationPosted) {
            PluginManager.shared(context).pushReceived(null, message);
        }
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification posted. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        PluginManager.shared(context).pushReceived(notificationInfo.getNotificationId(), notificationInfo.getMessage());
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification opened. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        PluginManager.shared(context).notificationOpened(notificationInfo);

        // Return false here to allow Urban Airship to auto launch the launcher activity
        return false;
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo, @NonNull ActionButtonInfo actionButtonInfo) {
        Log.i(TAG, "Notification action button opened. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());

        PluginManager.shared(context).notificationOpened(notificationInfo, actionButtonInfo);

        // Return false here to allow Urban Airship to auto launch the launcher
        // activity for foreground notification action buttons
        return false;
    }
}
