/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * Inbox update event.
 */
public class InboxEvent implements Event {
    private static final String EVENT_INBOX_UPDATED = "urbanairship.inbox_updated";

    @Override
    @NonNull
    public String getEventName() {
        return EVENT_INBOX_UPDATED;
    }

    @Override
    @Nullable
    public JSONObject getEventData() {
        return null;
    }
}
