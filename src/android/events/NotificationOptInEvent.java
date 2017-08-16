/* Copyright 2017 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import android.support.annotation.NonNull;

import com.urbanairship.Logger;
import com.urbanairship.push.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Notification opt-in status event.
 */
public class NotificationOptInEvent implements Event {

    private static final String NOTIFICATION_OPT_IN_STATUS_EVENT = "urbanairship.notification_opt_in_status";
    private static final String OPT_IN = "optIn";

    private final boolean optIn;

    /**
     * Default constructor.
     *
     * @param optIn The app opt-in status.
     */
    public NotificationOptInEvent(boolean optIn) {
        this.optIn = optIn;
    }

    @NonNull
    @Override
    public String getEventName() {
        return NOTIFICATION_OPT_IN_STATUS_EVENT;
    }

    @Override
    public JSONObject getEventData() {
        JSONObject data = new JSONObject();

        try {
            data.put(OPT_IN, optIn);
        } catch (JSONException e) {
            Logger.error("Error adding opt-in event data", e);
        }

        return data;
    }
}
