/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.urbanairship.push.notifications.DefaultNotificationFactory;


/**
 * Notification factory that pulls its config from the {@link PluginManager}.
 */
public class CordovaNotificationFactory extends DefaultNotificationFactory {

    private PluginManager pluginManager;
    private int appIcon;

    public CordovaNotificationFactory(@NonNull Context context) {
        super(context);
        appIcon = context.getApplicationInfo().icon;
        pluginManager = PluginManager.shared(context);
    }

    @Override
    public int getLargeIcon() {
        return pluginManager.getNotificationLargeIcon();
    }

    @Override
    public int getSmallIconId() {
        int icon = pluginManager.getNotificationIcon();
        if (icon != 0){
            return icon;
        }
        return appIcon;
    }

    @Override
    public Uri getSound() {
        return pluginManager.getNotificationSound();
    }

    @Override
    public int getColor() {
        return pluginManager.getNotificationAccentColor();
    }
}
