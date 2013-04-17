package com.urbanairship.phonegap.plugins;

import android.os.RemoteException;

import com.urbanairship.Logger;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.util.ServiceNotBoundException;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PushNotificationPlugin extends CordovaPlugin {
    final static String TAG = PushNotificationPlugin.class.getSimpleName();

    static PushNotificationPlugin instance = new PushNotificationPlugin();
    PushPreferences pushPrefs = PushManager.shared().getPreferences();
    LocationPreferences locationPrefs = UALocationManager.shared().getPreferences();


    static public String incomingAlert = "";
    static public Map<String, String> incomingExtras = new HashMap<String, String>();

    public PushNotificationPlugin() {
        instance = this;
    }

    public static PushNotificationPlugin getInstance() {
        return instance;
    }

    private JSONObject notificationObject(String message,
            Map<String, String> extras) {
        JSONObject data = new JSONObject();
        try {
            data.put("message", message);
            data.put("extras", new JSONObject(extras));
        } catch (JSONException e) {
            Logger.error("Error constructing notification object", e);
        }
        return data;
    }

    public void raisePush(String message, Map<String, String> extras) {
        JSONObject data = notificationObject(message, extras);
        String js = String.format(
                "window.pushNotification.pushCallback(%s);",
                data.toString());
        Logger.info("Javascript Calling back: " + js);

        try {
            this.webView.sendJavascript(js);
        } catch (NullPointerException npe) {
            Logger.info("unable to send javascript in raisepush");
        } catch (Exception e) {
            Logger.error("unexpected exception in raisePush", e);
        }
    }

    public void raiseRegistration(Boolean valid, String pushID) {
        JSONObject data = new JSONObject();
        try {
            data.put("valid", valid);
            data.put("pushID", pushID);
        } catch (JSONException e) {
            Logger.error("Error In raiseRegistration", e);
        }
        String js = String.format(
                "window.pushNotification.registrationCallback(%s);",
                data.toString());
        Logger.info("Javascript Calling back: " + js);

        try {
            this.webView.sendJavascript(js);
        } catch (NullPointerException npe) {
            Logger.info("unable to send javascript in raiseRegistration");
        } catch (Exception e) {
            Logger.error("unexpected exception in raisePush", e);
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        Logger.debug("Plugin Execute: " + action + " passed");
        // Core API
        // Top level enabling/disabling
        if (action.equals("enablePush")) {
            if (this.pushPrefs != null) {
                PushManager.enablePush();
                PushManager.shared().setIntentReceiver(PushNotificationPluginIntentReceiver.class);
                callbackContext.success();
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("disablePush")) {
            if (this.pushPrefs != null) {
                PushManager.disablePush();
                callbackContext.success();
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("enableLocation")) {
            if (this.locationPrefs != null) {
                UALocationManager.enableLocation();
                callbackContext.success();
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }
        } else if (action.equals("disableLocation")) {
            if (this.locationPrefs != null) {
                UALocationManager.disableLocation();
                callbackContext.success();
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }
        } else if (action.equals("enableBackgroundLocation")) {
            if (this.locationPrefs != null) {
                UALocationManager.enableBackgroundLocation();
                callbackContext.success();
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }
        } else if (action.equals("disableBackgroundLocation")) {
            if (this.locationPrefs != null) {
                UALocationManager.disableBackgroundLocation();
                callbackContext.success();
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }

            // is* Functions

        } else if (action.equals("isPushEnabled")) {
            if (this.pushPrefs != null) {
                int value = this.pushPrefs.isPushEnabled() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("isSoundEnabled")) {
            if (this.pushPrefs != null) {
                int value = this.pushPrefs.isSoundEnabled() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("isVibrateEnabled")) {
            if (this.pushPrefs != null) {
                int value = this.pushPrefs.isVibrateEnabled() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("isQuietTimeEnabled")) {
            if (this.pushPrefs != null) {
                int value = this.pushPrefs.isQuietTimeEnabled() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("isInQuietTime")) {
            if (this.pushPrefs != null) {
                int value = this.pushPrefs.isInQuietTime() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("isLocationEnabled")) {
            if (this.locationPrefs != null) {
                int value = this.locationPrefs.isLocationEnabled() ? 1 : 0;
                callbackContext.success(value);
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }
            // Getters

        } else if (action.equals("getIncoming")) {
            String alert = PushNotificationPlugin.incomingAlert;
            Map<String, String> extras = PushNotificationPlugin.incomingExtras;
            JSONObject obj = notificationObject(alert, extras);

            callbackContext.success(obj);

            //reset incoming push data until the next background push comes in
            PushNotificationPlugin.incomingAlert = "";
            PushNotificationPlugin.incomingExtras = new HashMap<String,String>();
        } else if (action.equals("getPushID")) {
            if (this.pushPrefs != null) {
                String pushID = PushManager.shared().getAPID();
                pushID = pushID != null ? pushID : "";
                callbackContext.success(pushID);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }
        } else if (action.equals("getQuietTime")) {
            if (this.pushPrefs != null ) {
                Logger.debug("Returning quiet time");
                try {
                    callbackContext.success(getQuietTime());
                } catch (JSONException e) {
                    callbackContext.error("Error building quietTime JSON");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("getTags")) {
            if (this.pushPrefs != null) {
                Logger.debug("Returning tags");
                Set<String> tags = PushManager.shared().getTags();

                try {
                    JSONObject returnObject = new JSONObject();
                    returnObject.put("tags", new JSONArray(tags));
                    callbackContext.success(returnObject);
                } catch (JSONException e) {
                    Logger.error("Error building tags JSON", e);
                    callbackContext.error("Error building tags JSON");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("getAlias")) {
            if (this.pushPrefs != null) {
                String alias = PushManager.shared().getAlias();
                alias = alias != null ? alias : "";
                callbackContext.success(alias);
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

            // Setters
        } else if (action.equals("setAlias")) {
            try {
                String alias = data.getString(0);
                if (alias.equals("")) {
                    alias = null;
                }

                Logger.debug("Settings alias: " + alias);
                callbackContext.success();

            } catch (JSONException e) {
                Logger.error("Error reading alias in callback", e);
                callbackContext.error("Error reading alias in callback");
            }

        } else if (action.equals("setTags")) {
            if (this.pushPrefs != null) {
                try {
                    setTags(data);
                    callbackContext.success();
                } catch (JSONException e) {
                    Logger.error("Error reading tags JSON", e);
                    callbackContext.error("Error reading tags JSON");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("setSoundEnabled")) {
            if (this.pushPrefs != null) {
                try {
                    boolean soundPreference = data.getBoolean(0);
                    this.pushPrefs.setSoundEnabled(soundPreference);
                    Logger.debug("Settings Sound: " + soundPreference);
                    callbackContext.success();
                } catch (JSONException e) {
                    Logger.error("Error reading soundEnabled in callback", e);
                    callbackContext.error("Error reading soundEnabled in callback");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("setVibrateEnabled")) {
            if (this.pushPrefs != null) {
                try {
                    boolean vibrationPreference = data.getBoolean(0);
                    this.pushPrefs.setVibrateEnabled(vibrationPreference);
                    Logger.debug("Settings Vibrate: " + vibrationPreference);
                    callbackContext.success();
                } catch (JSONException e) {
                    Logger.error("Error reading vibrateEnabled in callback", e);
                    callbackContext.error("Error reading vibrateEnabled in callback");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("setQuietTimeEnabled")) {
            if (this.pushPrefs != null) {
                try {
                    boolean quietPreference = data.getBoolean(0);
                    this.pushPrefs.setQuietTimeEnabled(quietPreference);
                    Logger.debug("Settings QuietTime: " + quietPreference);
                    callbackContext.success();
                } catch (JSONException e) {
                    Logger.error("Error reading quietTimeEnabled in callback", e);
                    callbackContext.error("Error reading quietTimeEnabled in callback");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

        } else if (action.equals("setQuietTime")) {
            if (this.pushPrefs != null ) {
                try {
                    setQuietTime(data);
                    callbackContext.success();
                } catch (JSONException e) {
                    Logger.error("Error reading quietTime JSON", e);
                    callbackContext.error("Error reading quietTime JSON");
                }
            } else {
                callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            }

            // Location
        } else if (action.equals("recordCurrentLocation")) {
            if (this.locationPrefs != null) {
                try {
                    Logger.debug("LOGGING LOCATION");
                    UALocationManager.shared().recordCurrentLocation();
                } catch (ServiceNotBoundException e) {
                    Logger.debug("Location not bound, binding now");
                    UALocationManager.bindService();
                } catch (RemoteException e) {
                    Logger.error("Caught RemoteException in recordCurrentLocation", e);
                }
                callbackContext.success();
            } else {
                callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            }


            // Invalid action, send back an error

        } else {
            Logger.debug("Invalid action: " + action + " passed");
            return false;
        }
        // Logger.debug("Exec done on " + action);
        return true;
    }

    /**
     * Sets the tags in Urban Airship from a JSONArray
     * @param data JSONArray containing the tags
     * @throws JSONException
     */
    private void setTags(JSONArray data) throws JSONException {
        HashSet<String> tagSet = new HashSet<String>();
        JSONArray tagsArray = data.getJSONArray(0);
        for (int i = 0; i < tagsArray.length(); ++i) {
            tagSet.add(tagsArray.getString(i));
        }

        PushManager.shared().setTags(tagSet);
        Logger.debug("Settings tags: " + tagSet);
    }

    /**
     * Sets the quiet time in Urban Airship preferences from a JSONArray
     * @param data JSONArray containing the quiet time preferences
     * @throws JSONException
     */
    private void setQuietTime(JSONArray data) throws JSONException {
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        int startHour = data.getInt(0);
        int startMinute = data.getInt(1);
        int endHour = data.getInt(2);
        int endMinute = data.getInt(3);

        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMinute);

        Logger.debug("Settings QuietTime. Start: " + start + ", End: " + end);
        this.pushPrefs.setQuietTimeInterval(start.getTime(), end.getTime());
    }

    /**
     * Gets the current quiet time from Urban Airship preferences as a JSONObject
     * @return JSONObject of the quiet time
     * @throws JSONException
     */
    private JSONObject getQuietTime() throws JSONException {
        Date[] quietTime = this.pushPrefs.getQuietTimeInterval();

        int startHour = 0;
        int startMinute = 0;
        int endHour = 0;
        int endMinute = 0;

        if (quietTime != null) {

            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            start.setTime(quietTime[0]);
            end.setTime(quietTime[1]);

            startHour = start.get(Calendar.HOUR_OF_DAY);
            startMinute = start.get(Calendar.MINUTE);
            endHour = end.get(Calendar.HOUR_OF_DAY);
            endMinute = end.get(Calendar.MINUTE);
        }

        JSONObject returnObject = new JSONObject();
        returnObject.put("startHour", startHour);
        returnObject.put("startMinute", startMinute);
        returnObject.put("endHour", endHour);
        returnObject.put("endMinute", endMinute);
        return returnObject;
    }
}
