/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.urbanairship.cordova.PluginLogger;

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


    public RegistrationEvent(@Nullable String channel, @Nullable String registrationToken, boolean success) {
        this.channel = channel;
        this.registrationToken = registrationToken;
        this.success = success;
    }

    @NonNull
    @Override
    public String getEventName() {
        return EVENT_CHANNEL_UPDATED;
    }

    @Nullable
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
            PluginLogger.error(e, "Error in channel registration");
        }

        return data;
    }
}
