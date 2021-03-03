/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import com.urbanairship.cordova.PluginLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Deep link event when a new deep link is received.
 */
public class DeepLinkEvent implements Event {
    private static final String EVENT_DEEPLINK_ACTION = "urbanairship.deep_link";

    private final String deepLink;

    public DeepLinkEvent(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getDeepLink() {
        return deepLink;
    }

    @Override
    public String getEventName() {
        return EVENT_DEEPLINK_ACTION;
    }

    @Override
    public JSONObject getEventData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("deepLink", deepLink);
        } catch (JSONException e) {
            PluginLogger.error(e, "Error constructing deep link event");
        }
        return jsonObject;
    }
}
