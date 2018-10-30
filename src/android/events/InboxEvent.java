/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import org.json.JSONObject;

/**
 * Inbox update event.
 */
public class InboxEvent implements Event {
    private static final String EVENT_INBOX_UPDATED = "urbanairship.inbox_updated";


    @Override
    public String getEventName() {
        return EVENT_INBOX_UPDATED;
    }

    @Override
    public JSONObject getEventData() {
        return null;
    }
}
