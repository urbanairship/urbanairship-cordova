package com.urbanairship.phonegap;

import android.app.NotificationManager;
import android.content.Context;

import android.os.RemoteException;

import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.util.ServiceNotBoundException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PushNotificationPlugin extends CordovaPlugin {

    private final static List<String> knownActions = Arrays.asList("enablePush", "disablePush", "enableLocation", "disableLocation", "enableBackgroundLocation",
            "disableBackgroundLocation", "isPushEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime", "isLocationEnabled",
            "getIncoming", "getPushID", "getQuietTime", "getTags", "getAlias", "setAlias", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "recordCurrentLocation", "clearNotifications");

    public static  String incomingAlert = "";
    public static Map<String, String> incomingExtras = new HashMap<String, String>();

    // Used to raise pushes and registration from the PushReceiver
    private static PushNotificationPlugin instance;

    private PushPreferences pushPrefs;
    private LocationPreferences locationPrefs;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public PushNotificationPlugin() {
        instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Logger.info("Initializing PushNotificationPlugin");
        Autopilot.automaticTakeOff(cordova.getActivity().getApplication());
        pushPrefs = PushManager.shared().getPreferences();
        locationPrefs = UALocationManager.shared().getPreferences();
    }

    private static JSONObject notificationObject(String message,
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

    static void raisePush(String message, Map<String, String> extras) {
        if (instance == null) {
            return;
        }

        sendEvent("urbanairship.push", notificationObject(message, extras).toString());
    }

    static void raiseRegistration(Boolean valid, String pushID) {
        if (instance == null) {
            return;
        }

        JSONObject data = new JSONObject();
        try {
            if (valid) {
                data.put("pushID", pushID);
            } else {
                data.put("error", "Invalid registration.");
            }
        } catch (JSONException e) {
            Logger.error("Error in raiseRegistration", e);
        }

        sendEvent("urbanairship.registration", data.toString());
    }

    static void sendEvent(String event, String data) {
        if (instance == null) {
            return;
        }

        Logger.info("Sending event " + event + ": " + data);
        String eventURL = String.format("javascript:try{cordova.fireDocumentEvent('%s', %s);}catch(e){console.log('exception firing event %s from native');};", event, data, event);

        instance.webView.loadUrl(eventURL);
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
        if (!knownActions.contains(action)) {
            Logger.debug("Invalid action: " + action);
            return false;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.debug("Plugin Execute: " + action);
                    Method method = PushNotificationPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                    method.invoke(PushNotificationPlugin.this, data, callbackContext);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
        });

        return true;
    }

    void clearNotifications(JSONArray data, CallbackContext callbackContext) {
        Context context = UAirship.shared().getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        callbackContext.success();
    }

    void enablePush(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            PushManager.enablePush();
            callbackContext.success();
        }
    }

    void disablePush(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            PushManager.disablePush();
            callbackContext.success();
        }
    }

    void enableLocation(JSONArray data, CallbackContext callbackContext) {
        if (requireLocationServiceEnabled(callbackContext)) {
            UALocationManager.enableLocation();
            callbackContext.success();
        }
    }

    void disableLocation(JSONArray data, CallbackContext callbackContext) {
        if (requireLocationServiceEnabled(callbackContext)) {
            UALocationManager.disableLocation();
            callbackContext.success();
        }
    }

    void enableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        if (requireLocationServiceEnabled(callbackContext)) {
            UALocationManager.enableBackgroundLocation();
            callbackContext.success();
        }
    }

    void disableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        if (requireLocationServiceEnabled(callbackContext)) {
            UALocationManager.disableBackgroundLocation();
            callbackContext.success();
        }
    }

    void isPushEnabled(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            int value = this.pushPrefs.isPushEnabled() ? 1 : 0;
            callbackContext.success(value);
        }
    }

    void isSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            int value = this.pushPrefs.isSoundEnabled() ? 1 : 0;
            callbackContext.success(value);
        }
    }

    void isVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            int value = this.pushPrefs.isVibrateEnabled() ? 1 : 0;
            callbackContext.success(value);
        }
    }

    void isQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            int value = this.pushPrefs.isQuietTimeEnabled() ? 1 : 0;
            callbackContext.success(value);
        }
    }

    void isInQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            int value = this.pushPrefs.isInQuietTime() ? 1 : 0;
            callbackContext.success(value);
        }
    }

    void isLocationEnabled(JSONArray data, CallbackContext callbackContext) {
        if (requireLocationServiceEnabled(callbackContext)) {
            int value = this.locationPrefs.isLocationEnabled() ? 1 : 0;
            callbackContext.success(value);
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
        if (requirePushServiceEnabled(callbackContext)) {
            String pushID = PushManager.shared().getAPID();
            pushID = pushID != null ? pushID : "";
            callbackContext.success(pushID);
        }
    }

    void getQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

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

        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put("startHour", startHour);
            returnObject.put("startMinute", startMinute);
            returnObject.put("endHour", endHour);
            returnObject.put("endMinute", endMinute);

            Logger.debug("Returning quiet time");
            callbackContext.success(returnObject);
        } catch (JSONException e) {
            callbackContext.error("Error building quietTime JSON");
        }
    }

    void getTags(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

        Set<String> tags = PushManager.shared().getTags();
        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put("tags", new JSONArray(tags));

            Logger.debug("Returning tags");
            callbackContext.success(returnObject);
        } catch (JSONException e) {
            Logger.error("Error building tags JSON", e);
            callbackContext.error("Error building tags JSON");
        }
    }

    void getAlias(JSONArray data, CallbackContext callbackContext) {
        if (requirePushServiceEnabled(callbackContext)) {
            String alias = PushManager.shared().getAlias();
            alias = alias != null ? alias : "";
            callbackContext.success(alias);
        }
    }

    void setAlias(JSONArray data, CallbackContext callbackContext) {
        try {
            String alias = data.getString(0);
            if (alias.equals("")) {
                alias = null;
            }

            Logger.debug("Settings alias: " + alias);
            PushManager.shared().setAlias(alias);

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading alias in callback", e);
            callbackContext.error("Error reading alias in callback");
        }
    }

    void setTags(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

        try {
            HashSet<String> tagSet = new HashSet<String>();
            JSONArray tagsArray = data.getJSONArray(0);
            for (int i = 0; i < tagsArray.length(); ++i) {
                tagSet.add(tagsArray.getString(i));
            }

            Logger.debug("Settings tags: " + tagSet);
            PushManager.shared().setTags(tagSet);

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading tags JSON", e);
            callbackContext.error("Error reading tags JSON");
        }
    }

    void setSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

        try {
            boolean soundPreference = data.getBoolean(0);
            this.pushPrefs.setSoundEnabled(soundPreference);
            Logger.debug("Settings Sound: " + soundPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading soundEnabled in callback", e);
            callbackContext.error("Error reading soundEnabled in callback");
        }
    }

    void setVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

        try {
            boolean vibrationPreference = data.getBoolean(0);
            this.pushPrefs.setVibrateEnabled(vibrationPreference);
            Logger.debug("Settings Vibrate: " + vibrationPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading vibrateEnabled in callback", e);
            callbackContext.error("Error reading vibrateEnabled in callback");
        }
    }

    void setQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

        try {
            boolean quietPreference = data.getBoolean(0);
            this.pushPrefs.setQuietTimeEnabled(quietPreference);
            Logger.debug("Settings QuietTime: " + quietPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading quietTimeEnabled in callback", e);
            callbackContext.error("Error reading quietTimeEnabled in callback");
        }
    }

    void setQuietTime(JSONArray data, CallbackContext callbackContext) {
        if (!requirePushServiceEnabled(callbackContext)) {
            return;
        }

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

            Logger.debug("Settings QuietTime. Start: " + start.getTime() + ", End: " + end.getTime());
            this.pushPrefs.setQuietTimeInterval(start.getTime(), end.getTime());
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading quietTime JSON", e);
            callbackContext.error("Error reading quietTime JSON");
        }
    }

    void recordCurrentLocation(JSONArray data, CallbackContext callbackContext) {
        if (!requireLocationServiceEnabled(callbackContext)) {
            return;
        }

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
    }

    // Helpers

    private boolean requirePushServiceEnabled(CallbackContext callbackContext) {
        if (!UAirship.shared().getAirshipConfigOptions().pushServiceEnabled) {
            Logger.warn("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            callbackContext.error("pushServiceEnabled must be enabled in the airshipconfig.properties file");
            return false;
        }

        return true;
    }

    private boolean requireLocationServiceEnabled(CallbackContext callbackContext) {
        if (!UAirship.shared().getAirshipConfigOptions().locationOptions.locationServiceEnabled) {
            Logger.warn("locationServiceEnabled must be enabled in the location.properties file");
            callbackContext.error("locationServiceEnabled must be enabled in the location.properties file");
            return false;
        }

        return true;
    }
}
