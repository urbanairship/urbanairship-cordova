package com.urbanairship.phonegap;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushMessage;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.util.UAStringUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

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

    public static final String ACTION_CHANNEL_REGISTRATION = "com.urbanairship.cordova.ACTION_CHANNEL_REGISTRATION";
    public static final String ACTION_PUSH_RECEIVED = "com.urbanairship.cordova.ACTION_PUSH_RECEIVED";
    public static final String EXTRA_ERROR = "com.urbanairship.cordova.EXTRA_ERROR";
    public static final String EXTRA_PUSH = "com.urbanairship.cordova.EXTRA_PUSH";
    public static final String EXTRA_NOTIFICATION_ID = "com.urbanairship.cordova.EXTRA_NOTIFICATION_ID";

    private final static List<String> knownActions = Arrays.asList("enablePush", "disablePush", "enableLocation", "disableLocation", "enableBackgroundLocation",
            "disableBackgroundLocation", "isPushEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime", "isLocationEnabled",
            "getIncoming", "getChannelID", "getQuietTime", "getTags", "getAlias", "setAlias", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "recordCurrentLocation", "clearNotifications", "registerPushListener", "registerChannelListener",
            "setAnalyticsEnabled", "isAnalyticsEnabled", "setNamedUser", "getNamedUser");

    public static PushMessage incomingPush = null;
    public static Integer incomingNotificationId = null;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private BroadcastReceiver channelUpdateReceiver;
    private BroadcastReceiver pushReceiver;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Logger.info("Initializing PushNotificationPlugin");
        Autopilot.automaticTakeOff(cordova.getActivity().getApplication());
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

         // Handle any Google Play services errors
        if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
            PlayServicesUtils.handleAnyPlayServicesError(UAirship.getApplicationContext());
        }
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

    void registerPushListener(JSONArray data, final CallbackContext callbackContext) {
        Logger.debug("Listening for push events.");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(cordova.getActivity());

        if (pushReceiver != null) {
            manager.unregisterReceiver(pushReceiver);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PUSH_RECEIVED);
        pushReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.debug("Received push.");

                PushMessage message = intent.getParcelableExtra(EXTRA_PUSH);
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                JSONObject data = notificationObject(message, notificationId != -1 ? notificationId : null);

                PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                result.setKeepCallback(true);

                callbackContext.sendPluginResult(result);
            }
        };

        manager.registerReceiver(pushReceiver, intentFilter);
    }

    void registerChannelListener(JSONArray data, final CallbackContext callbackContext) {
        Logger.debug("Listening for channel updates.");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(cordova.getActivity());

        if (channelUpdateReceiver != null) {
            manager.unregisterReceiver(channelUpdateReceiver);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CHANNEL_REGISTRATION);
        channelUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.debug("Received channel update.");

                JSONObject data = new JSONObject();
                try {
                    if (!intent.hasExtra(EXTRA_ERROR)) {
                        data.put("channelID", UAirship.shared().getPushManager().getChannelId());
                    } else {
                        data.put("error", "Invalid registration.");
                    }
                } catch (JSONException e) {
                    Logger.error("Error in channel registration", e);
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                result.setKeepCallback(true);

                callbackContext.sendPluginResult(result);
            }
        };

        manager.registerReceiver(channelUpdateReceiver, intentFilter);
    }

    void clearNotifications(JSONArray data, CallbackContext callbackContext) {
        Context context = UAirship.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        callbackContext.success();
    }

    void enablePush(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getPushManager().setUserNotificationsEnabled(true);
        callbackContext.success();
    }

    void disablePush(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getPushManager().setUserNotificationsEnabled(false);
        callbackContext.success();
    }

    void enableLocation(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().setLocationUpdatesEnabled(true);
        callbackContext.success();
    }

    void disableLocation(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().setLocationUpdatesEnabled(false);
        callbackContext.success();
    }

    void enableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().setBackgroundLocationAllowed(true);
        callbackContext.success();
    }

    void disableBackgroundLocation(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().setBackgroundLocationAllowed(false);
        callbackContext.success();
    }

    void isPushEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().getUserNotificationsEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void isSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isSoundEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void isVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isVibrateEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void isQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isQuietTimeEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void isInQuietTime(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isInQuietTime() ? 1 : 0;
        callbackContext.success(value);
    }

    void isLocationEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getLocationManager().isLocationUpdatesEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void getIncoming(JSONArray data, CallbackContext callbackContext) {
        JSONObject notificationObject = notificationObject(PushNotificationPlugin.incomingPush, PushNotificationPlugin.incomingNotificationId);

        callbackContext.success(notificationObject);

        // Reset incoming push data until the next background push comes in
        PushNotificationPlugin.incomingPush = null;
        PushNotificationPlugin.incomingNotificationId = null;
    }

    void getChannelID(JSONArray data, CallbackContext callbackContext) {
        String channelId = UAirship.shared().getPushManager().getChannelId();
        channelId = channelId != null ? channelId : "";
        callbackContext.success(channelId);
    }

    void getQuietTime(JSONArray data, CallbackContext callbackContext) {
        Date[] quietTime = UAirship.shared().getPushManager().getQuietTimeInterval();

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
        Set<String> tags = UAirship.shared().getPushManager().getTags();
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
        String alias = UAirship.shared().getPushManager().getAlias();
        alias = alias != null ? alias : "";
        callbackContext.success(alias);
    }

    void setAlias(JSONArray data, CallbackContext callbackContext) {
        try {
            String alias = data.getString(0);
            if (alias.equals("")) {
                alias = null;
            }

            Logger.debug("Settings alias: " + alias);

            UAirship.shared().getPushManager().setAlias(alias);

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading alias in callback", e);
            callbackContext.error("Error reading alias in callback");
        }
    }

    void setTags(JSONArray data, CallbackContext callbackContext) {
        try {
            HashSet<String> tagSet = new HashSet<String>();
            JSONArray tagsArray = data.getJSONArray(0);
            for (int i = 0; i < tagsArray.length(); ++i) {
                tagSet.add(tagsArray.getString(i));
            }

            Logger.debug("Settings tags: " + tagSet);
            UAirship.shared().getPushManager().setTags(tagSet);

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading tags JSON", e);
            callbackContext.error("Error reading tags JSON");
        }
    }

    void setSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean soundPreference = data.getBoolean(0);
            UAirship.shared().getPushManager().setSoundEnabled(soundPreference);
            Logger.debug("Settings Sound: " + soundPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading soundEnabled in callback", e);
            callbackContext.error("Error reading soundEnabled in callback");
        }
    }

    void setVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean vibrationPreference = data.getBoolean(0);
            UAirship.shared().getPushManager().setVibrateEnabled(vibrationPreference);
            Logger.debug("Settings Vibrate: " + vibrationPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading vibrateEnabled in callback", e);
            callbackContext.error("Error reading vibrateEnabled in callback");
        }
    }

    void setQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean quietPreference = data.getBoolean(0);
            UAirship.shared().getPushManager().setQuietTimeEnabled(quietPreference);
            Logger.debug("Settings QuietTime: " + quietPreference);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading quietTimeEnabled in callback", e);
            callbackContext.error("Error reading quietTimeEnabled in callback");
        }
    }

    void setQuietTime(JSONArray data, CallbackContext callbackContext) {
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
            UAirship.shared().getPushManager().setQuietTimeInterval(start.getTime(), end.getTime());

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading quietTime JSON", e);
            callbackContext.error("Error reading quietTime JSON");
        }
    }

    void recordCurrentLocation(JSONArray data, CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().requestSingleLocation();
        callbackContext.success();
    }

    void setAnalyticsEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean enabled = data.getBoolean(0);
            Logger.debug("Settings analyticsEnabled: " + enabled);
            UAirship.shared().getAnalytics().setEnabled(enabled);
            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading analyticsEnabled in callback", e);
            callbackContext.error("Error reading analyticsEnabled in callback");
        }
    }

    void isAnalyticsEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getAnalytics().isEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    void getNamedUser(JSONArray data, CallbackContext callbackContext) {
        String namedUserId = UAirship.shared().getPushManager().getNamedUser().getId();
        namedUserId = namedUserId != null ? namedUserId : "";
        callbackContext.success(namedUserId);
    }

    void setNamedUser(JSONArray data, CallbackContext callbackContext) {
        try {
            String namedUserId = data.getString(0);
            if (UAStringUtil.isEmpty(namedUserId)) {
                namedUserId = null;
            }

            Logger.debug("Settings named user: " + namedUserId);

            UAirship.shared().getPushManager().getNamedUser().setId(namedUserId);

            callbackContext.success();
        } catch (JSONException e) {
            Logger.error("Error reading named user ID in callback", e);
            callbackContext.error("Error reading named user ID in callback");
        }
    }

    private static JSONObject notificationObject(PushMessage message, Integer notificationId) {
        JSONObject data = new JSONObject();

        if (message == null) {
            return data;
        }

        Map<String, String> extras = new HashMap<String, String>();
        for (String key : message.getPushBundle().keySet()) {
            extras.put(key, message.getPushBundle().getString(key));
        }

        try {
            data.putOpt("message", message.getAlert());
            data.putOpt("extras", new JSONObject(extras));
            data.putOpt("notification_id", notificationId);
        } catch (JSONException e) {
            Logger.error("Error constructing notification object", e);
        }

        return data;
    }
}
