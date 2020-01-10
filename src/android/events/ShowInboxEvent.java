/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.urbanairship.cordova.PluginLogger;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Show inbox event.
 */
public class ShowInboxEvent implements Event {

    private static final String SHOW_INBOX_EVENT = "urbanairship.show_inbox";
    private static final String MESSAGE_ID = "messageId";

    private final String messageId;

    /**
     * Default constructor.
     *
     * @param messageId The optional message ID.
     */
    public ShowInboxEvent(@Nullable String messageId) {
        this.messageId = messageId;
    }

    @Override
    @NonNull
    public String getEventName() {
        return SHOW_INBOX_EVENT;
    }

    @Override
    @Nullable
    public JSONObject getEventData() {
        JSONObject data = new JSONObject();

        try {
            if (messageId != null) {
                data.put(MESSAGE_ID, messageId);
            }
        } catch (JSONException e) {
            PluginLogger.error(e, "Error in show inbox event");
        }

        return data;
    }
}
