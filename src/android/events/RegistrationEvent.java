/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import com.urbanairship.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Registration event.
 */
public class RegistrationEvent implements Event {

    private static final String EVENT_CHANNEL_UPDATED = "urbanairship.registration";

    private static final String CHANNEL_ID = "channelID";
    private static final String REGISTRATION_TOKEN = "registrationToken";

    private final String channel;
    private final String registrationToken;
    private final boolean success;


    public RegistrationEvent(String channel, String registrationToken, boolean success) {
        this.channel = channel;
        this.registrationToken = registrationToken;
        this.success = success;
    }

    @Override
    public String getEventName() {
        return EVENT_CHANNEL_UPDATED;
    }

    @Override
    public JSONObject getEventData() {
        JSONObject data = new JSONObject();
        try {
            if (success) {
                data.put(CHANNEL_ID, channel);
                if (registrationToken != null) {
                    data.put(REGISTRATION_TOKEN, registrationToken);
                }

            } else {
                data.put("error", "Invalid registration.");
            }
        } catch (JSONException e) {
            Logger.error("Error in channel registration", e);
        };

        return data;
    }
}
