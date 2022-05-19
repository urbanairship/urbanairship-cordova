package com.urbanairship.cordova;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.urbanairship.app.GlobalActivityMonitor;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.AirshipNotificationProvider;
import com.urbanairship.push.notifications.NotificationArguments;
import com.urbanairship.push.notifications.NotificationResult;

/**
 * Notification provider that pulls its config from the {@link PluginManager}.
 */
public class CordovaNotificationProvider extends AirshipNotificationProvider {

    @NonNull
    public static final String PUSH_MESSAGE_BUNDLE_EXTRA = "com.urbanairship.push_bundle";

    private final PluginManager pluginManager;

    public CordovaNotificationProvider(@NonNull Context context, @NonNull PluginManager pluginManager) {
        super(context, pluginManager.getAirshipConfig());
        this.pluginManager = pluginManager;
    }

    @Override
    public String getDefaultNotificationChannelId() {
        String defaultChannelId = pluginManager.getDefaultNotificationChannelId();
        if (defaultChannelId != null) {
            return defaultChannelId;
        }

        return super.getDefaultNotificationChannelId();
    }

    @NonNull
    @Override
    public NotificationResult onCreateNotification(@NonNull Context context, @NonNull NotificationArguments arguments) {
        if (!shouldDisplay(context, arguments)) {
            return NotificationResult.cancel();
        }
        return super.onCreateNotification(context, arguments);
    }

    @NonNull
    @Override
    protected NotificationCompat.Builder onExtendBuilder(@NonNull Context context, @NonNull NotificationCompat.Builder builder, @NonNull NotificationArguments arguments) {
        builder.getExtras().putBundle(PUSH_MESSAGE_BUNDLE_EXTRA, arguments.getMessage().getPushBundle());
        return super.onExtendBuilder(context, builder, arguments);
    }

    @Override
    @DrawableRes
    public int getSmallIcon() {
        int id = pluginManager.getNotificationIcon();

        return id != 0 ? id : super.getSmallIcon();
    }

    @Override
    @DrawableRes
    public int getLargeIcon() {
        int id = pluginManager.getNotificationLargeIcon();

        return id != 0 ? id : super.getLargeIcon();
    }

    @Override
    @ColorInt
    public int getDefaultAccentColor() {
        int id = pluginManager.getNotificationAccentColor();

        return id != 0 ? id : super.getDefaultAccentColor();
    }

    private boolean shouldDisplay(@NonNull Context context, @NonNull NotificationArguments arguments) {
        // If push does not define foreground display behavior fallback to plugin
        if (arguments.getMessage().getExtra(PushMessage.EXTRA_FOREGROUND_DISPLAY) == null) {
            return pluginManager.isForegroundNotificationsEnabled() || !GlobalActivityMonitor.shared(context).isAppForegrounded();
        }
        return true;
    }
}
