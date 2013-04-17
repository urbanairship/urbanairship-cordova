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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PushNotificationPlugin extends CordovaPlugin {

    final static List<String> knownActions = Arrays.asList("enablePush", "disablePush", "enableLocation", "disableLocation", "enableBackgroundLocation",
            "disableBackgroundLocation", "isPushEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime", "isLocationEnabled",
            "getIncoming", "getPushID", "getQuietTime", "getTags", "getAlias", "setAlias", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "recordCurrentLocation");

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

        if (!knownActions.contains(action)) {
            Logger.debug("Invalid action: " + action);
            return false;
        }

        try {
            Logger.debug("Plugin Execute: " + action);
            Method method = PushNotificationPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
            method.invoke(this, data, callbackContext);
            return true;
        } catch (NoSuchMethodException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }

        return false;
    }

    // Actions

    void enablePush(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            PushManager.enablePush();
            PushManager.shared().setIntentReceiver(PushNotificationPluginIntentReceiver.class);
            callbackContext.success();
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }
    }

    void disablePush(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            PushManager.disablePush();
            callbackContext.success();
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void enableLocation(JSONArray data, CallbackContext callbackContext) {
        if (this.locationPrefs != null) {
            UALocationManager.enableLocation();
            callbackContext.success();
        } else {
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
        }

    }

    void disableLocation(JSONArray data, CallbackContext callbackContext) {
        if (this.locationPrefs != null) {
            UALocationManager.disableLocation();
            callbackContext.success();
        } else {
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
        }
    }

    void enableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        if (this.locationPrefs != null) {
            UALocationManager.enableBackgroundLocation();
            callbackContext.success();
        } else {
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
        }

    }

    void disableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        if (this.locationPrefs != null) {
            UALocationManager.disableBackgroundLocation();
            callbackContext.success();
        } else {
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
        }

    }

    void isPushEnabled(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            int value = this.pushPrefs.isPushEnabled() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }
    }

    void isSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            int value = this.pushPrefs.isSoundEnabled() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }
    }

    void isVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            int value = this.pushPrefs.isVibrateEnabled() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void isQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            int value = this.pushPrefs.isQuietTimeEnabled() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void isInQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            int value = this.pushPrefs.isInQuietTime() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void isLocationEnabled(JSONArray data, CallbackContext callbackContext) {
        if (this.locationPrefs != null) {
            int value = this.locationPrefs.isLocationEnabled() ? 1 : 0;
            callbackContext.success(value);
        } else {
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
        }

    }

    void getIncoming(JSONArray data, CallbackContext callbackContext) {
        String alert = PushNotificationPlugin.incomingAlert;
        Map<String, String> extras = PushNotificationPlugin.incomingExtras;
        JSONObject obj = notificationObject(alert, extras);

        callbackContext.success(obj);

        //reset incoming push data until the next background push comes in
        PushNotificationPlugin.incomingAlert = "";
        PushNotificationPlugin.incomingExtras = new HashMap<String,String>();

    }

    void getPushID(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            String pushID = PushManager.shared().getAPID();
            pushID = pushID != null ? pushID : "";
            callbackContext.success(pushID);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void getQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null ) {
            Logger.debug("Returning quiet time");
            try {
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
                callbackContext.success(returnObject);
            } catch (JSONException e) {
                callbackContext.error("Error building quietTime JSON");
            }
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void getTags(JSONArray data, CallbackContext callbackContext) {
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

    }

    void getAlias(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            String alias = PushManager.shared().getAlias();
            alias = alias != null ? alias : "";
            callbackContext.success(alias);
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }
    }

    void setAlias(JSONArray data, CallbackContext callbackContext) {
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

    }

    void setTags(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null) {
            try {
                HashSet<String> tagSet = new HashSet<String>();
                JSONArray tagsArray = data.getJSONArray(0);
                for (int i = 0; i < tagsArray.length(); ++i) {
                    tagSet.add(tagsArray.getString(i));
                }

                PushManager.shared().setTags(tagSet);
                Logger.debug("Settings tags: " + tagSet);;
                callbackContext.success();
            } catch (JSONException e) {
                Logger.error("Error reading tags JSON", e);
                callbackContext.error("Error reading tags JSON");
            }
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }

    }

    void setSoundEnabled(JSONArray data, CallbackContext callbackContext) {
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

    }

    void setVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
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

    }

    void setQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
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
    }

    void setQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (this.pushPrefs != null ) {
            try {
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
                callbackContext.success();
            } catch (JSONException e) {
                Logger.error("Error reading quietTime JSON", e);
                callbackContext.error("Error reading quietTime JSON");
            }
        } else {
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
        }
    }

    void recordCurrentLocation(JSONArray data, CallbackContext callbackContext) {
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

    }
}
