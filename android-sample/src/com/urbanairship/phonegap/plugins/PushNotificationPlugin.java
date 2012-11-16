package com.urbanairship.phonegap.plugins;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.RemoteException;

import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.Logger;
import com.urbanairship.phonegap.sample.IntentReceiver;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.util.ServiceNotBoundException;

public class PushNotificationPlugin extends Plugin {
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
            this.sendJavascript(js);
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
            this.sendJavascript(js);
        } catch (NullPointerException npe) {
            Logger.info("unable to send javascript in raiseRegistration");
        } catch (Exception e) {
            Logger.error("unexpected exception in raisePush", e);
        }
    }

    @Override
    public PluginResult execute(String action, JSONArray data, String callbackId) {
        Logger.debug("Plugin Execute: " + action + " passed");
        PluginResult result = null;

        // Core API

        // Top level enabling/disabling

        if (action.equals("enablePush")) {
            PushManager.enablePush();
            PushManager.shared().setIntentReceiver(IntentReceiver.class);
            result = new PluginResult(Status.OK);
        } else if (action.equals("disablePush")) {
            PushManager.disablePush();
            result = new PluginResult(Status.OK);
        } else if (action.equals("enableLocation")) {
            UALocationManager.enableLocation();
            result = new PluginResult(Status.OK);
        } else if (action.equals("disableLocation")) {
            UALocationManager.disableLocation();
            result = new PluginResult(Status.OK);
        } else if (action.equals("enableBackgroundLocation")) {
            UALocationManager.enableBackgroundLocation();
            result = new PluginResult(Status.OK);
        } else if (action.equals("disableBackgroundLocation")) {
            UALocationManager.disableBackgroundLocation();
            result = new PluginResult(Status.OK);

            // is* Functions

        } else if (action.equals("isPushEnabled")) {
            result = new PluginResult(Status.OK, this.pushPrefs.isPushEnabled());
        } else if (action.equals("isSoundEnabled")) {
            result = new PluginResult(Status.OK, this.pushPrefs.isSoundEnabled());
        } else if (action.equals("isVibrateEnabled")) {
            result = new PluginResult(Status.OK, this.pushPrefs.isVibrateEnabled());
        } else if (action.equals("isQuietTimeEnabled")) {
            result = new PluginResult(Status.OK,
                    this.pushPrefs.isQuietTimeEnabled());
        } else if (action.equals("isInQuietTime")) {
            result = new PluginResult(Status.OK, this.pushPrefs.isInQuietTime());
        } else if (action.equals("isLocationEnabled")) {
            result = new PluginResult(Status.OK, this.locationPrefs.isLocationEnabled());

            // Getters

        } else if (action.equals("getIncoming")) {
            String alert = PushNotificationPlugin.incomingAlert;
            Map<String, String> extras = PushNotificationPlugin.incomingExtras;
            JSONObject obj = notificationObject(alert, extras);
            result = new PluginResult(Status.OK, obj);
        } else if (action.equals("getPushID")) {
            String pushID = PushManager.shared().getAPID();
            pushID = pushID != null ? pushID : "";
            result = new PluginResult(Status.OK, pushID);
        } else if (action.equals("getQuietTime")) {
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

            try {
                returnObject.put("startHour", startHour);
                returnObject.put("startMinute", startMinute);
                returnObject.put("endHour", endHour);
                returnObject.put("endMinute", endMinute);
            } catch (JSONException e) {
                Logger.error("Error building quietTime JSON", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Returning start time");
            result = new PluginResult(Status.OK, returnObject);

        } else if (action.equals("getTags")) {
            Set<String> tags = PushManager.shared().getTags();
            JSONObject returnObject = new JSONObject();
            try {
                returnObject.put("tags", new JSONArray(tags));
            } catch (JSONException e) {
                Logger.error("Error buidling tags JSON", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Returning tags");
            result = new PluginResult(Status.OK, returnObject);
        } else if (action.equals("getAlias")) {
            String alias = PushManager.shared().getAlias();
            alias = alias != null ? alias : "";
            result = new PluginResult(Status.OK, alias);

            // Setters

        } else if (action.equals("setAlias")) {
            String alias = "";
            try {
                alias = data.getString(0);
            } catch (JSONException e) {
                Logger.error("Error reading alias in callback", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            if(alias.equals("")) {
                alias = null;
            }
            Logger.debug("Settings alias: " + alias);
            PushManager.shared().setAlias(alias);
            result = new PluginResult(Status.OK);
        } else if (action.equals("setTags")) {
            HashSet<String> tagSet = new HashSet<String>();
            try {
                JSONArray tagsArray = data.getJSONArray(0);
                for (int i = 0; i < tagsArray.length(); ++i) {
                    tagSet.add(tagsArray.getString(i));
                }
            } catch (JSONException e) {
                Logger.error("Error reading tags JSON", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            PushManager.shared().setTags(tagSet);
            Logger.debug("Settings tags: " + tagSet);
            result = new PluginResult(Status.OK);
        } else if (action.equals("setSoundEnabled")) {
            boolean pref;
            try {
                pref = data.getBoolean(0);
            } catch (JSONException e) {
                Logger.error("Error reading soundEnabled in callback", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Settings Sound: " + pref);
            this.pushPrefs.setSoundEnabled(pref);
            result = new PluginResult(Status.OK);
        } else if (action.equals("setVibrateEnabled")) {
            boolean pref;
            try {
                pref = data.getBoolean(0);
            } catch (JSONException e) {
                Logger.error("Error reading vibrateEnabled in callback", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Settings Vibrate: " + pref);
            this.pushPrefs.setVibrateEnabled(pref);
            result = new PluginResult(Status.OK);
        } else if (action.equals("setQuietTimeEnabled")) {
            boolean pref;
            try {
                pref = data.getBoolean(0);
            } catch (JSONException e) {
                Logger.error("Error reading quietTimeEnabled in callback", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Settings QuietTime: " + pref);
            this.pushPrefs.setQuietTimeEnabled(pref);
            result = new PluginResult(Status.OK);

        } else if (action.equals("setQuietTime")) {
            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            try {
                int startHour = data.getInt(0);
                int startMinute = data.getInt(1);
                int endHour = data.getInt(2);
                int endMinute = data.getInt(3);
                start.set(Calendar.HOUR_OF_DAY, startHour);
                start.set(Calendar.MINUTE, startMinute);
                end.set(Calendar.HOUR_OF_DAY, endHour);
                end.set(Calendar.MINUTE, endMinute);
            } catch (JSONException e) {
                Logger.error("Error reading quietTime JSON", e);
                result = new PluginResult(Status.ERROR);
                return result;
            }
            Logger.debug("Settings QuietTime. Start: " + start + ", End: " + end);
            this.pushPrefs.setQuietTimeInterval(start.getTime(), end.getTime());
            result = new PluginResult(Status.OK);

            // Location

        } else if (action.equals("recordCurrentLocation")) {
            try {
                Logger.debug("LOGGING LOCATION");
                UALocationManager.shared().recordCurrentLocation();
            } catch (ServiceNotBoundException e) {
                Logger.debug("Location not bound, binding now");
                UALocationManager.bindService();
            } catch (RemoteException e) {
                Logger.error("Caught RemoteException in recordCurrentLocation", e);
            }
            result = new PluginResult(Status.OK);

            // Invalid action, send back an error

        } else {
            Logger.debug("Invalid action: " + action + " passed");
            result = new PluginResult(Status.INVALID_ACTION);
        }
        // Logger.debug("Exec done on " + action);
        return result;
    }
}
