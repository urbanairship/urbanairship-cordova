/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import com.urbanairship.Logger;
import com.urbanairship.push.PushMessage;

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

    public PushEvent(Integer notificationId, PushMessage message) {
        this.notificationId = notificationId;
        this.message = message;
    }

    @Override
    public String getEventName() {
        return EVENT_PUSH_RECEIVED;
    }

    @Override
    public JSONObject getEventData() {
        JSONObject data = new JSONObject();

        Map<String, String> extras = new HashMap<String, String>();
        for (String key : message.getPushBundle().keySet()) {
            if ("android.support.content.wakelockid".equals(key)) {
                continue;
            }
            if ("google.sent_time".equals(key)) {
                extras.put(key, Long.toString(message.getPushBundle().getLong(key)));
                continue;
            }
            if ("google.ttl".equals(key)) {
                extras.put(key, Integer.toString(message.getPushBundle().getInt(key)));
                continue;
            }
            extras.put(key, message.getPushBundle().getString(key));
        }

        try {
            data.putOpt("message", message.getAlert());
            data.putOpt("extras", new JSONObject(extras));
            data.putOpt("notification_id", notificationId);
        } catch (JSONException e) {
            Logger.error("Error constructing notification object", e);
        }

        return data;
    }
}
