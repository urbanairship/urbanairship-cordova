/* Copyright Urban Airship and Contributors */

package events;

import com.urbanairship.cordova.PluginLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Deep link event when a new deep link is received.
 */
public class PreferenceCenterEvent implements Event {
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
            PluginLogger.error(e, "Error constructing deep preference center event");
        }
        return jsonObject;
    }
}
