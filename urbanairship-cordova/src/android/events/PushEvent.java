/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.urbanairship.cordova.PluginLogger;
import com.urbanairship.cordova.Utils;
import com.urbanairship.push.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Push event.
 */
public class PushEvent implements Event {

    private static final String EVENT_PUSH_RECEIVED = "urbanairship.push";

    private final PushMessage message;
    private final Integer notificationId;

    public PushEvent(@Nullable Integer notificationId, @NonNull PushMessage message) {
        this.notificationId = notificationId;
        this.message = message;
    }

    @NonNull
    @Override
    public String getEventName() {
        return EVENT_PUSH_RECEIVED;
    }

    @Nullable
    @Override
    public JSONObject getEventData() {
        try {
            return Utils.notificationObject(message, message.getNotificationTag(), notificationId);
        } catch (JSONException e) {
            PluginLogger.error(e, "Error constructing notification object");
            return null;
        }
    }
}
