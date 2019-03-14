/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionCompletionCallback;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.ActionRunRequest;
import com.urbanairship.cordova.events.DeepLinkEvent;
import com.urbanairship.cordova.events.Event;
import com.urbanairship.cordova.events.NotificationOpenedEvent;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.json.JsonValue;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.TagGroupsEditor;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.HelperActivity;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The Urban Airship Cordova plugin.
 */
public class UAirshipPlugin extends CordovaPlugin {

    /**
     * These actions are only available after takeOff.
     */
    private final static List<String> AIRSHIP_ACTIONS = Arrays.asList("setUserNotificationsEnabled", "setLocationEnabled", "setBackgroundLocationEnabled",
            "isUserNotificationsEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime", "isLocationEnabled", "isBackgroundLocationEnabled",
            "getLaunchNotification", "getChannelID", "getQuietTime", "getTags", "getAlias", "setAlias", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "recordCurrentLocation", "clearNotifications", "setAnalyticsEnabled", "isAnalyticsEnabled",
            "setNamedUser", "getNamedUser", "runAction", "editNamedUserTagGroups", "editChannelTagGroups", "displayMessageCenter", "markInboxMessageRead",
            "deleteInboxMessage", "getInboxMessages", "displayInboxMessage", "overlayInboxMessage", "refreshInbox", "getDeepLink", "setAssociatedIdentifier",
            "isAppNotificationsEnabled", "dismissMessageCenter", "dismissInboxMessage", "dismissOverlayInboxMessage", "setAutoLaunchDefaultMessageCenter",
            "getActiveNotifications", "clearNotification");


    /*
     * These actions are available even if airship is not ready.
     */
    private final static List<String> GLOBAL_ACTIONS = Arrays.asList("takeOff", "registerListener", "setAndroidNotificationConfig");
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Context context;
    private PluginManager pluginManager;

    @Override
    public void initialize(@NonNull CordovaInterface cordova, @NonNull CordovaWebView webView) {
        super.initialize(cordova, webView);
        PluginLogger.info("Initializing Urban Airship cordova plugin.");
        context = cordova.getActivity().getApplicationContext();

        Autopilot.automaticTakeOff(context);
        pluginManager = PluginManager.shared(context);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (pluginManager.isAirshipAvailable()) {
            // Handle any Google Play services errors
            if (PlayServicesUtils.isGooglePlayStoreAvailable(cordova.getActivity())) {
                PlayServicesUtils.handleAnyPlayServicesError(cordova.getActivity());
            }

            pluginManager.checkOptInStatus();
        }
    }


    /**
     * To extend the plugin, add the actions to either {@link #AIRSHIP_ACTIONS} or {#link #GLOBAL_ACTIONS} and
     * then define the method with the signature `void <CORDOVA_ACTION>(JSONArray data, final
     * CallbackContext callbackContext)` and it will automatically be called. All methods will be
     * ( executed in the ExecutorService. Any exceptions thrown by the actions are automatically caught
     * and the callbackContext will return an error result.
     */
    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
        final boolean isGlobalAction = GLOBAL_ACTIONS.contains(action);
        final boolean isAirshipAction = AIRSHIP_ACTIONS.contains(action);

        if (!isAirshipAction && !isGlobalAction) {
            PluginLogger.debug("Invalid action: %s", action);
            return false;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (isAirshipAction && !pluginManager.isAirshipAvailable()) {
                    callbackContext.error("TakeOff not called. Unable to process action: " + action);
                    return;
                }


                try {
                    PluginLogger.debug("Plugin Execute: %s", action);
                    Method method = UAirshipPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                    method.invoke(UAirshipPlugin.this, data, callbackContext);
                } catch (Exception e) {
                    PluginLogger.error(e, "Action failed to execute: %s", action);
                    callbackContext.error("Action " + action + " failed with exception: " + e.getMessage());
                }
            }
        });

        return true;
    }

    @Override
    public void onReset() {
        super.onReset();
        pluginManager.setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pluginManager.setListener(null);
    }

    /**
     * Registers for events.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void registerListener(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        pluginManager.setListener(new PluginManager.Listener() {
            @Override
            public void onEvent(@NonNull Event event) {
                JSONObject eventData = new JSONObject();

                try {
                    eventData.putOpt("eventType", event.getEventName());
                    eventData.putOpt("eventData", event.getEventData());
                } catch (JSONException e) {
                    PluginLogger.error("Failed to create event: %s", event);
                    return;
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventData);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        });
    }

    /**
     * Initializes the Urban Airship plugin.
     *
     * @param data The data.
     * @param callbackContext THe callback context.
     * @throws JSONException
     */
    void takeOff(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
        JSONObject config = data.getJSONObject(0);
        JSONObject prod = config.getJSONObject("production");
        JSONObject dev = config.getJSONObject("development");

        pluginManager.editConfig()
                .setProductionConfig(prod.getString("appKey"), prod.getString("appSecret"))
                .setDevelopmentConfig(dev.getString("appKey"), dev.getString("appSecret"))
                .apply();

        final CountDownLatch latch = new CountDownLatch(1);

        // TakeOff must be called on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Autopilot.automaticTakeOff(context);

                if (!pluginManager.isAirshipAvailable()) {
                    callbackContext.error("Airship config is invalid. Unable to takeOff.");
                } else {
                    callbackContext.success();
                }

                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            PluginLogger.error(e, "Failed to takeOff");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Configures the notification factory for Android.
     *
     * @param data The data.
     * @param callbackContext THe callback context.
     * @throws JSONException
     */
    void setAndroidNotificationConfig(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONObject config = data.getJSONObject(0);

        // Factory will pull the latest values from the config.
        pluginManager.editConfig()
                .setNotificationIcon(config.optString("icon"))
                .setNotificationLargeIcon(config.optString("largeIcon"))
                .setNotificationAccentColor(config.optString("accentColor"))
                .apply();

        callbackContext.success();
    }

    /**
     * Enables/disables auto launching the message center.
     *
     * @param data The data.
     * @param callbackContext THe callback context.
     * @throws JSONException
     */
    void setAutoLaunchDefaultMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);

        // Actions that check this value pull latest values from the config.
        pluginManager.editConfig()
                .setAutoLaunchMessageCenter(enabled)
                .apply();

        callbackContext.success();
    }

    /**
     * Clears all notifications posted by the application.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void clearNotifications(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        callbackContext.success();
    }

    /**
     * Enables or disables user notifications.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setUserNotificationsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().getPushManager().setUserNotificationsEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Checks if user notifications are enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isUserNotificationsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().getUserNotificationsEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Enables or disables Urban Airship location services.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setLocationEnabled(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);

        if (enabled && shouldRequestPermissions()) {
            RequestPermissionsTask task = new RequestPermissionsTask(context, new RequestPermissionsTask.Callback() {
                @Override
                public void onResult(boolean enabled) {
                    UAirship.shared().getLocationManager().setLocationUpdatesEnabled(enabled);
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            UAirship.shared().getLocationManager().setLocationUpdatesEnabled(enabled);
            callbackContext.success();
        }
    }


    /**
     * Determines if we should request permissions
     *
     * @return {@code true} if permissions should be requested, otherwise {@code false}.
     */
    private boolean shouldRequestPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }

        Context context = UAirship.getApplicationContext();
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }

    private static class RequestPermissionsTask extends AsyncTask<String[], Void, Boolean> {

        @SuppressLint("StaticFieldLeak")
        private final Context context;
        private final Callback callback;

        public interface Callback {
            void onResult(boolean enabled);
        }

        RequestPermissionsTask(@NonNull Context context, @NonNull Callback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String[]... strings) {
            int[] result = HelperActivity.requestPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            for (int element : result) {
                if (element == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (callback != null) {
                callback.onResult(result);
            }
        }
    }

    /**
     * Checks if location is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isLocationEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getLocationManager().isLocationUpdatesEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Enables are disables background location. Background location requires location to be enabled.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setBackgroundLocationEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().getLocationManager().setBackgroundLocationAllowed(enabled);
        callbackContext.success();
    }

    /**
     * Checks if background location is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isBackgroundLocationEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getLocationManager().isBackgroundLocationAllowed() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification sound is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isSoundEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isSoundEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification vibration is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isVibrateEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isVibrateEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if quiet time is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isQuietTimeEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isQuietTimeEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if the device is currently in quiet time.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isInQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isInQuietTime() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Returns the last notification that launched the application.
     * <p/>
     * Expected arguments: Boolean - `YES` to clear the notification
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getLaunchNotification(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        boolean clear = data.optBoolean(0, false);
        NotificationOpenedEvent event = pluginManager.getLastLaunchNotificationEvent(clear);

        if (event != null) {
            callbackContext.success(event.getEventData());
        } else {
            callbackContext.success(new JSONObject());
        }
    }

    /**
     * Returns the last deep link.
     * <p/>
     * Expected arguments: Boolean - `YES` to clear the deep link
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getDeepLink(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        boolean clear = data.optBoolean(0, false);
        DeepLinkEvent event = pluginManager.getLastDeepLinkEvent(clear);
        String deepLink = event == null ? null : event.getDeepLink();
        callbackContext.success(deepLink);
    }

    /**
     * Returns the channel ID.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getChannelID(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String channelId = UAirship.shared().getPushManager().getChannelId();
        channelId = channelId != null ? channelId : "";
        callbackContext.success(channelId);
    }

    /**
     * Returns the quiet time as an object with the following:
     * "startHour": Number,
     * "startMinute": Number,
     * "endHour": Number,
     * "endMinute": Number
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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

        JSONObject returnObject = new JSONObject();
        returnObject.put("startHour", startHour);
        returnObject.put("startMinute", startMinute);
        returnObject.put("endHour", endHour);
        returnObject.put("endMinute", endMinute);

        PluginLogger.debug("Returning quiet time");
        callbackContext.success(returnObject);
    }

    /**
     * Returns the tags as an array.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getTags(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        Set<String> tags = UAirship.shared().getPushManager().getTags();
        PluginLogger.debug("Returning tags");
        callbackContext.success(new JSONArray(tags));
    }

    /**
     * Returns the alias.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     * @deprecated Deprecated since 6.7.0 - to be removed in a future version of the plugin - please use getNamedUser
     */
    @Deprecated
    void getAlias(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String alias = UAirship.shared().getPushManager().getAlias();
        alias = alias != null ? alias : "";
        callbackContext.success(alias);
    }

    /**
     * Sets the alias.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     * @deprecated Deprecated since 6.7.0 - to be removed in a future version of the plugin - please use setNamedUser
     */
    @Deprecated
    void setAlias(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String alias = data.getString(0);
        if (alias.equals("")) {
            alias = null;
        }

        PluginLogger.debug("Settings alias: %s", alias);

        UAirship.shared().getPushManager().setAlias(alias);

        callbackContext.success();
    }

    /**
     * Sets the tags.
     * <p/>
     * Expected arguments: An array of Strings
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setTags(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        HashSet<String> tagSet = new HashSet<String>();
        JSONArray tagsArray = data.getJSONArray(0);
        for (int i = 0; i < tagsArray.length(); ++i) {
            tagSet.add(tagsArray.getString(i));
        }

        PluginLogger.debug("Settings tags: %s", tagSet);
        UAirship.shared().getPushManager().setTags(tagSet);

        callbackContext.success();
    }

    /**
     * Enables or disables notification sound.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setSoundEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean soundPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setSoundEnabled(soundPreference);
        PluginLogger.debug("Settings Sound: %s", soundPreference);
        callbackContext.success();
    }

    /**
     * Enables or disables notification vibration.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setVibrateEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean vibrationPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setVibrateEnabled(vibrationPreference);
        PluginLogger.debug("Settings Vibrate: %s.", vibrationPreference);
        callbackContext.success();
    }

    /**
     * Enables or disables quiet time.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setQuietTimeEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean quietPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setQuietTimeEnabled(quietPreference);
        PluginLogger.debug("Settings QuietTime: %s", quietPreference);
        callbackContext.success();
    }

    /**
     * Sets the quiet time.
     * <p/>
     * Expected arguments: Number - start hour, Number - start minute,
     * Number - end hour, Number - end minute
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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

        PluginLogger.debug("Settings QuietTime. Start: %s. End: %s.", start.getTime(), end.getTime());
        UAirship.shared().getPushManager().setQuietTimeInterval(start.getTime(), end.getTime());

        callbackContext.success();
    }

    /**
     * Records the current location.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void recordCurrentLocation(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        UAirship.shared().getLocationManager().requestSingleLocation();
        callbackContext.success();
    }

    /**
     * Enables or disables analytics.
     * <p/>
     * Disabling analytics will delete any locally stored events
     * and prevent any events from uploading. Features that depend on analytics being
     * enabled may not work properly if it's disabled (reports, region triggers,
     * location segmentation, push to local time).
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setAnalyticsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        PluginLogger.debug("Settings analyticsEnabled: %s", enabled);
        UAirship.shared().getAnalytics().setEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Checks if analytics is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isAnalyticsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getAnalytics().isEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Sets associated custom identifiers for use with the Connect data stream.
     * <p/>
     * Previous identifiers will be replaced by the new identifiers each time setAssociateIdentifier is called. It is a set operation.
     * <p/>
     * Expected arguments: String
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setAssociatedIdentifier(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String key = data.getString(0);
        String identifier = data.getString(1);

        UAirship.shared().getAnalytics()
                .editAssociatedIdentifiers()
                .addIdentifier(key, identifier)
                .apply();

        callbackContext.success();
    }

    /**
     * Returns the named user ID.
     * <p/>
     * Expected arguments: String
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getNamedUser(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String namedUserId = UAirship.shared().getNamedUser().getId();
        namedUserId = namedUserId != null ? namedUserId : "";
        callbackContext.success(namedUserId);
    }

    /**
     * Sets the named user ID.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setNamedUser(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String namedUserId = data.getString(0);
        if (UAStringUtil.isEmpty(namedUserId)) {
            namedUserId = null;
        }

        PluginLogger.debug("Setting named user: %s", namedUserId);

        UAirship.shared().getNamedUser().setId(namedUserId);

        callbackContext.success();
    }

    /**
     * Edits the named user tag groups.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void editNamedUserTagGroups(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);

        PluginLogger.debug("Editing named user tag groups: %s", operations);

        TagGroupsEditor editor = UAirship.shared().getNamedUser().editTagGroups();
        applyTagGroupOperations(editor, operations);
        editor.apply();

        callbackContext.success();
    }

    /**
     * Edits the channel tag groups.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void editChannelTagGroups(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);

        PluginLogger.debug("Editing channel tag groups: %s", operations);

        TagGroupsEditor editor = UAirship.shared().getPushManager().editTagGroups();
        applyTagGroupOperations(editor, operations);
        editor.apply();

        callbackContext.success();
    }

    /**
     * Runs an Urban Airship action.
     * <p>
     * Expected arguments: String - action name, * - the action value
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void runAction(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
        final String actionName = data.getString(0);
        final Object actionValue = data.opt(1);

        ActionRunRequest.createRequest(actionName)
                .setValue(actionValue)
                .run(new ActionCompletionCallback() {
                    @Override
                    public void onFinish(@NonNull ActionArguments arguments, @NonNull ActionResult result) {

                        if (result.getStatus() == ActionResult.STATUS_COMPLETED) {

                            /*
                             * We are wrapping the value in an object to preserve the type of data
                             * the action returns. CallbackContext.success does not allow all types.
                             * The value will be pulled out in the UAirship.js file before passing
                             * it back to the user.
                             */

                            Map<String, JsonValue> resultMap = new HashMap<String, JsonValue>();
                            resultMap.put("value", result.getValue().toJsonValue());

                            try {
                                callbackContext.success(new JSONObject(resultMap.toString()));
                            } catch (JSONException e) {
                                callbackContext.error("Failed to convert action results: " + e.getMessage());
                            }
                        } else {
                            callbackContext.error(createActionErrorMessage(actionName, result));
                        }
                    }
                });
    }

    /**
     * Helper method to create the action run error message.
     *
     * @param name The name of the action.
     * @param result The action result.
     * @return The action error message.
     */
    private static String createActionErrorMessage(String name, ActionResult result) {
        switch (result.getStatus()) {
            case ActionResult.STATUS_ACTION_NOT_FOUND:
                return String.format("Action %s not found", name);
            case ActionResult.STATUS_REJECTED_ARGUMENTS:
                return String.format("Action %s rejected its arguments", name);
            case ActionResult.STATUS_EXECUTION_ERROR:
                if (result.getException() != null) {
                    return result.getException().getMessage();
                }
            case ActionResult.STATUS_COMPLETED:
                return "";
        }

        return String.format("Action %s failed with unspecified error", name);
    }

    /**
     * Helper method to apply tag operations to a TagGroupsEditor.
     *
     * @param editor The editor.
     * @param operations The tag operations.
     */
    private static void applyTagGroupOperations(TagGroupsEditor editor, JSONArray operations) throws JSONException {
        for (int i = 0; i < operations.length(); i++) {
            JSONObject operation = operations.getJSONObject(i);

            JSONArray tags = operation.getJSONArray("tags");
            String group = operation.getString("group");
            String operationType = operation.getString("operation");

            HashSet<String> tagSet = new HashSet<String>();
            for (int j = 0; j < tags.length(); j++) {
                tagSet.add(tags.getString(j));
            }

            if (tagSet.isEmpty()) {
                continue;
            }

            if ("add".equals(operationType)) {
                editor.addTags(group, tagSet);
            } else if ("remove".equals(operationType)) {
                editor.removeTags(group, tagSet);
            }
        }
    }

    /**
     * Displays the message center.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     */
    void displayMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String messageId = data.optString(0);

        PluginLogger.debug("Displaying Message Center");
        if (!UAStringUtil.isEmpty(messageId)) {
            Intent intent = new Intent(cordova.getActivity(), CustomMessageCenterActivity.class)
                    .setAction(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                    .setPackage(cordova.getActivity().getPackageName())
                    .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, messageId, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            cordova.getActivity().startActivity(intent);
        } else {
            Intent intent = new Intent(cordova.getActivity(), CustomMessageCenterActivity.class)
                    .setPackage(cordova.getActivity().getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            cordova.getActivity().startActivity(intent);
        }
        callbackContext.success();
    }

    /**
     * Dismiss the message center.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     */
    void dismissMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        PluginLogger.debug("Dismissing Message Center");
        Intent intent = new Intent(cordova.getActivity(), CustomMessageCenterActivity.class)
                .setAction(CustomMessageCenterActivity.CLOSE_INTENT_ACTION);

        cordova.getActivity().startActivity(intent);

        callbackContext.success();
    }

    /**
     * Deletes an inbox message.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void deleteInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        message.delete();
        callbackContext.success();
    }

    /**
     * Marks an inbox message read.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void markInboxMessageRead(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        message.markRead();
        callbackContext.success();
    }

    /**
     * Gets the inbox listing.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void getInboxMessages(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray messagesJson = new JSONArray();

        for (RichPushMessage message : UAirship.shared().getInbox().getMessages()) {
            JSONObject messageJson = new JSONObject();
            messageJson.putOpt("id", message.getMessageId());
            messageJson.putOpt("title", message.getTitle());
            messageJson.putOpt("sentDate", message.getSentDateMS());
            messageJson.putOpt("listIconUrl", message.getListIconUrl());
            messageJson.putOpt("isRead", message.isRead());

            JSONObject extrasJson = new JSONObject();
            Bundle extras = message.getExtras();
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                extrasJson.putOpt(key, value);
            }

            messageJson.put("extras", extrasJson);

            messagesJson.put(messageJson);
        }

        callbackContext.success(messagesJson);
    }

    /**
     * Displays an inbox message.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void displayInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        final String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), CustomMessageActivity.class)
                        .setAction(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                        .setPackage(cordova.getActivity().getPackageName())
                        .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, messageId, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                cordova.getActivity().startActivity(intent);
            }
        });

        callbackContext.success();
    }

    /**
     * Dismiss the inbox message.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void dismissInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        PluginLogger.debug("Dismissing Inbox Message");
        Intent intent = new Intent(cordova.getActivity(), CustomMessageActivity.class)
                .setAction(CustomMessageActivity.CLOSE_INTENT_ACTION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        cordova.getActivity().startActivity(intent);

        callbackContext.success();
    }

    /**
     * Displays an inbox message using the CustomLandingPageActivity.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void overlayInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        final String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), CustomLandingPageActivity.class)
                        .setAction(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                        .setPackage(cordova.getActivity().getPackageName())
                        .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, messageId, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                cordova.getActivity().startActivity(intent);
            }
        });

        callbackContext.success();
    }

    /**
     * Dismiss the overlay inbox message.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     */
    void dismissOverlayInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        PluginLogger.debug("Dismissing Overlay Inbox Message");
        Intent intent = new Intent(cordova.getActivity(), CustomLandingPageActivity.class)
                .setAction("CANCEL")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        cordova.getActivity().startActivity(intent);

        callbackContext.success();
    }

    /**
     * Refreshes the inbox.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     */
    void refreshInbox(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UAirship.shared().getInbox().fetchMessages(new RichPushInbox.FetchMessagesCallback() {
                    @Override
                    public void onFinished(boolean success) {
                        if (success) {
                            callbackContext.success();
                        } else {
                            callbackContext.error("Inbox failed to refresh");
                        }
                    }
                });
            }
        });
    }

    /**
     * Checks if app notifications are enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isAppNotificationsEnabled(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isOptIn() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Gets currently active notifications.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getActiveNotifications(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            JSONArray notificationsJSON = new JSONArray();

            NotificationManager notificationManager = (NotificationManager) UAirship.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                int id = statusBarNotification.getId();
                String tag = statusBarNotification.getTag();
                PushMessage pushMessage = Utils.messageFromNotification(statusBarNotification);

                try {
                    notificationsJSON.put(Utils.notificationObject(pushMessage, tag, id));
                } catch (Exception e) {
                    PluginLogger.error(e, "Unable to serialize push message: %s", pushMessage);
                }
            }

            callbackContext.success(notificationsJSON);
        } else {
            callbackContext.error("Getting active notifications is only supported on Marshmallow and newer devices.");
        }
    }

    /**
     * Clears all notifications.
     *
     * @param data The call data.
     * @param callbackContext The callback context
     * @throws JSONException
     */
    void clearNotification(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
        final String identifier = data.getString(0);

        if (UAStringUtil.isEmpty(identifier)) {
            return;
        }

        String[] parts = identifier.split(":", 2);

        if (parts.length == 0) {
            callbackContext.error("Invalid identifier: " + identifier);
            return;
        }

        int id;
        String tag = null;

        try {
            id = Integer.valueOf(parts[0]);
        } catch (NumberFormatException e) {
            callbackContext.error("Invalid identifier: " + identifier);
            return;
        }

        if (parts.length == 2) {
            tag = parts[1];
        }


        NotificationManagerCompat.from(UAirship.getApplicationContext()).cancel(tag, id);

        callbackContext.success();
    }

}
