/*
 Copyright 2009-2016 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.annotation.NonNull;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.push.PushMessage;
import com.urbanairship.cordova.UAirshipPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

/**
 * Intent receiver for Urban Airship channel and push events.
 */
public class CordovaAirshipReceiver extends AirshipReceiver {

    private static final String TAG = "CordovaAirshipReceiver";

    @Override
    protected void onChannelCreated(Context context, String channelId) {
        Log.i(TAG, "Channel created. Channel ID: " + channelId + ".");

        Intent intent = new Intent(UAirshipPlugin.ACTION_CHANNEL_REGISTRATION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onChannelUpdated(Context context, String channelId) {
        Log.i(TAG, "Channel updated. Channel ID: " + channelId + ".");

        Intent intent = new Intent(UAirshipPlugin.ACTION_CHANNEL_REGISTRATION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(TAG, "Channel registration failed.");

        Intent intent = new Intent(UAirshipPlugin.ACTION_CHANNEL_REGISTRATION)
                .putExtra(UAirshipPlugin.EXTRA_ERROR, true);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onPushReceived(@NonNull Context context, @NonNull PushMessage message, boolean notificationPosted) {
        Log.i(TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);

        if (!notificationPosted) {
            Intent intent = new Intent(UAirshipPlugin.ACTION_PUSH_RECEIVED)
                .putExtra(UAirshipPlugin.EXTRA_PUSH, message);

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification posted. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        Intent intent = new Intent(UAirshipPlugin.ACTION_PUSH_RECEIVED)
                .putExtra(UAirshipPlugin.EXTRA_PUSH, notificationInfo.getMessage())
                .putExtra(UAirshipPlugin.EXTRA_NOTIFICATION_ID, notificationInfo.getNotificationId());

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification opened. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        UAirshipPlugin.launchPushMessage = notificationInfo.getMessage();
        UAirshipPlugin.launchNotificationId = notificationInfo.getNotificationId();

        // Return false here to allow Urban Airship to auto launch the launcher activity
        return false;
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo, @NonNull ActionButtonInfo actionButtonInfo) {
        Log.i(TAG, "Notification action button opened. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());

        UAirshipPlugin.launchPushMessage = notificationInfo.getMessage();
        UAirshipPlugin.launchNotificationId = notificationInfo.getNotificationId();

        // Return false here to allow Urban Airship to auto launch the launcher
        // activity for foreground notification action buttons
        return false;
    }
}
