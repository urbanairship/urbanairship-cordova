/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.UAirship;
import com.urbanairship.cordova.events.RegistrationEvent;
import com.urbanairship.cordova.events.DeepLinkEvent;
import com.urbanairship.cordova.events.Event;
import com.urbanairship.cordova.events.InboxEvent;
import com.urbanairship.cordova.events.PushEvent;
import com.urbanairship.cordova.events.NotificationOpenedEvent;
import com.urbanairship.cordova.events.NotificationOptInEvent;
import com.urbanairship.push.PushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles state and events for the Cordova Plugin.
 */
public class PluginManager {

    /**
     * Interface when a new event is received.
     */
    public interface Listener {
        void onEvent(Event event);
    }

    private static final PluginManager shared = new PluginManager();

    private static final String NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY = "com.urbanairship.notification_opt_in_status_preferences";
    private static final String UA_PLUGIN_SHARED_PREFERENCES_FILE = "com.urbanairship.ua_plugin_shared_preferences";

    private NotificationOpenedEvent notificationOpenedEvent;
    private DeepLinkEvent deepLinkEvent = null;
    private Listener listener = null;

    private SharedPreferences preferences;

    private List<Event> pendingEvents = new ArrayList<Event>();


    /**
     * Singleton access.
     *
     * @return The manager instance.
     */
    public static PluginManager shared() {
        return shared;
    }

    /**
     * Called when the inbox is updated.
     */
    void inboxUpdated() {
        notifyListener(new InboxEvent());
    }

    /**
     * Called when a push is received.
     *
     * @param notificationId The notification ID.
     * @param pushMessage The push message.
     */
    void pushReceived(Integer notificationId, PushMessage pushMessage) {
        notifyListener(new PushEvent(notificationId, pushMessage));
    }

    /**
     * Called when a new deep link is received.
     *
     * @param deepLink The deep link.
     */
    void deepLinkReceived(String deepLink) {
        synchronized (shared) {
            DeepLinkEvent event = new DeepLinkEvent(deepLink);
            this.deepLinkEvent = event;

            if (!notifyListener(event)) {
                pendingEvents.add(event);
            }
        }
    }

    /**
     * Called on app resume and when registration changes.
     *
     * @param context The application context.
     */
    void checkOptInStatus(Context context) {
        boolean optIn = UAirship.shared().getPushManager().isOptIn();

        if (preferences == null) {
            preferences = context.getSharedPreferences(UA_PLUGIN_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        }

        // Check preferences for opt-in
        if (preferences.getBoolean(NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY, false) != optIn) {
            preferences.edit().putBoolean(NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY , optIn).apply();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(NOTIFICATION_OPT_IN_STATUS_EVENT_PREFERENCES_KEY, optIn);
            editor.apply();

            notifyListener(new NotificationOptInEvent(optIn));
        }
    }

    /**
     * Called when the notification is opened.
     *
     * @param notificationInfo The notification info.
     */
    void notificationOpened(@NonNull AirshipReceiver.NotificationInfo notificationInfo) {
        notificationOpened(new NotificationOpenedEvent(notificationInfo));
    }

    /**
     * Called when the notification is opened.
     *
     * @param notificationInfo The notification info.
     */
    void notificationOpened(@NonNull AirshipReceiver.NotificationInfo notificationInfo, @Nullable AirshipReceiver.ActionButtonInfo actionButtonInfo) {
        notificationOpened(new NotificationOpenedEvent(notificationInfo, actionButtonInfo));
    }

    private void notificationOpened(NotificationOpenedEvent event) {
        synchronized (shared) {
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
    void channelUpdated(String channel, boolean success) {
        notifyListener(new RegistrationEvent(channel, UAirship.shared().getPushManager().getRegistrationToken(), success));
    }

    /**
     * Returns the last deep link event.
     *
     * @param clear {@code true} to clear the event, otherwise {@code false}.
     * @return The deep link event, or null if the event is not available.
     */
    public DeepLinkEvent getLastDeepLinkEvent(boolean clear) {
        synchronized (shared) {
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
    public NotificationOpenedEvent getLastLaunchNotificationEvent(boolean clear) {
        synchronized (shared) {
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
    public void setListener(Listener listener) {
        synchronized (shared) {
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
     * Helper method to notify the listener of the event.
     *
     * @param event The event.
     * @return {@code true} if the listener was notified, otherwise {@code false}.
     */
    private boolean notifyListener(Event event) {
        synchronized (shared) {
            if (listener != null) {
                listener.onEvent(event);
                return true;
            }
            return false;
        }
    }
}
