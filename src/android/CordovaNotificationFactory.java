/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.DefaultNotificationFactory;


/**
 * Notification factory that pulls its config from the {@link PluginManager}.
 */
public class CordovaNotificationFactory extends DefaultNotificationFactory {

    @NonNull
    public static final String PUSH_MESSAGE_BUNDLE_EXTRA = "com.urbanairship.push_bundle";

    private final PluginManager pluginManager;
    private final int appIcon;

    public CordovaNotificationFactory(@NonNull Context context) {
        super(context);
        appIcon = context.getApplicationInfo().icon;
        pluginManager = PluginManager.shared(context);
    }

    @Override
    @NonNull
    public NotificationCompat.Builder extendBuilder(@NonNull NotificationCompat.Builder builder, @NonNull PushMessage message, int notificationId) {
        builder.getExtras().putBundle(PUSH_MESSAGE_BUNDLE_EXTRA, message.getPushBundle());
        return builder;
    }

    @Override
    public int getLargeIcon() {
        return pluginManager.getNotificationLargeIcon();
    }

    @Override
    public int getSmallIconId() {
        int icon = pluginManager.getNotificationIcon();
        if (icon != 0) {
            return icon;
        }
        return appIcon;
    }

    @Override
    @Nullable
    public Uri getSound() {
        return pluginManager.getNotificationSound();
    }

    @Override
    public int getColor() {
        return pluginManager.getNotificationAccentColor();
    }
}
