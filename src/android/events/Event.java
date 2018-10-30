/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import org.json.JSONObject;

/**
 * Interface for Urban Airship Cordova events.
 */
public interface Event {

    /**
     * The event name.
     *
     * @return The event name.
     */
    String getEventName();

    /**
     * The event data.
     *
     * @return The event data.
     */
    JSONObject getEventData();
}
