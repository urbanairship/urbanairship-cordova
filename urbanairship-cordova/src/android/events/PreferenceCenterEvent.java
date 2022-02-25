/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import com.urbanairship.cordova.PluginLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Preference Center event when the open preference center listener is called.
 */
public class PreferenceCenterEvent implements com.urbanairship.cordova.events.Event {
    private static final String EVENT_PREFERENCE_CENTER_ACTION = "urbanairship.open_preference_center";

    private final String preferenceCenterId;

    public PreferenceCenterEvent(String preferenceCenterId) {
        this.preferenceCenterId = preferenceCenterId;
    }

    public String getPreferenceCenterId() {
        return preferenceCenterId;
    }

    @Override
    public String getEventName() {
        return EVENT_PREFERENCE_CENTER_ACTION;
    }

    @Override
    public JSONObject getEventData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("preferenceCenterId", preferenceCenterId);
        } catch (JSONException e) {
            PluginLogger.error(e, "Error constructing preference center event");
        }
        return jsonObject;
    }
}
