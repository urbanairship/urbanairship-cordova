/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.urbanairship.cordova.Utils;
import com.urbanairship.Logger;
import com.urbanairship.push.PushMessage;
import com.urbanairship.util.UAStringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
            Logger.error("Error constructing notification object", e);
            return null;
        }
    }
}
