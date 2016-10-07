/*
 Copyright 2009-2016 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.cordova;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.actions.Action;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionCompletionCallback;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.ActionRunRequest;
import com.urbanairship.actions.ActionValue;
import com.urbanairship.actions.ActionValueException;
import com.urbanairship.actions.LandingPageActivity;
import com.urbanairship.cordova.events.DeepLinkEvent;
import com.urbanairship.cordova.events.Event;
import com.urbanairship.cordova.events.PushEvent;
import com.urbanairship.cordova.events.NotificationOpenedEvent;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.json.JsonValue;
import com.urbanairship.messagecenter.MessageActivity;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.TagGroupsEditor;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.UAStringUtil;
import com.urbanairship.util.HelperActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
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


/**
 * The Urban Airship Cordova plugin.
 */
public class UAirshipPlugin extends CordovaPlugin {

    /**
     * List of Cordova "actions". To extend the plugin, add the action below and then define the method
     * with the signature `void <CORDOVA_ACTION>(JSONArray data, final CallbackContext callbackContext)`
     * and it will automatically be called. All methods will be executed in the ExecutorService. Any
     * exceptions thrown by the actions are automatically caught and the callbackContext will return
     * an error result.
     */
    private final static List<String> knownActions = Arrays.asList("setUserNotificationsEnabled", "setLocationEnabled", "setBackgroundLocationEnabled",
            "isUserNotificationsEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime", "isLocationEnabled", "isBackgroundLocationEnabled",
            "getLaunchNotification", "getChannelID", "getQuietTime", "getTags", "getAlias", "setAlias", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "recordCurrentLocation", "clearNotifications", "registerListener", "setAnalyticsEnabled", "isAnalyticsEnabled",
            "setNamedUser", "getNamedUser", "runAction", "editNamedUserTagGroups", "editChannelTagGroups", "displayMessageCenter", "markInboxMessageRead",
            "deleteInboxMessage", "getInboxMessages", "displayInboxMessage", "overlayInboxMessage", "refreshInbox", "getDeepLink", "setAssociatedIdentifier",
            "isAppNotificationsEnabled", "setDisplayASAPEnabled");

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Logger.info("Initializing Urban Airship cordova plugin.");
        Autopilot.automaticTakeOff(cordova.getActivity().getApplication());
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        // Handle any Google Play services errors
        if (PlayServicesUtils.isGooglePlayStoreAvailable(cordova.getActivity())) {
            PlayServicesUtils.handleAnyPlayServicesError(cordova.getActivity());
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
                    Method method = UAirshipPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                    method.invoke(UAirshipPlugin.this, data, callbackContext);
                } catch (Exception e) {
                    Logger.error("Action failed to execute: " + action, e);
                    callbackContext.error("Action " + action + " failed with exception: " + e.getMessage());
                }
            }
        });

        return true;
    }

    @Override
    public void onReset() {
        super.onReset();
        UAirshipPluginManager.shared().setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UAirshipPluginManager.shared().setListener(null);
    }

    /**
     * Registers for events.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void registerListener(JSONArray data, final CallbackContext callbackContext) {
        if (callbackContext == null) {
            UAirshipPluginManager.shared().setListener(null);
            return;
        }

        UAirshipPluginManager.shared().setListener(new UAirshipPluginManager.Listener() {
            @Override
            public void onEvent(Event event) {
                JSONObject eventData = new JSONObject();

                try {
                    eventData.putOpt("eventType", event.getEventName());
                    eventData.putOpt("eventData", event.getEventData());
                } catch (JSONException e) {
                    Logger.error("Failed to create event: " + event);
                    return;
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventData);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        });
    }

    /**
     * Clears all notifications posted by the application.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void clearNotifications(JSONArray data, CallbackContext callbackContext) {
        Context context = UAirship.getApplicationContext();
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
    void setUserNotificationsEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().getPushManager().setUserNotificationsEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Enables or disables display ASAP mode for in-app messages.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setDisplayASAPEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().getInAppMessageManager().setDisplayAsapEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Checks if user notifications are enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isUserNotificationsEnabled(JSONArray data, CallbackContext callbackContext) {
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
    void setLocationEnabled(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);

        if (enabled && shouldRequestPermissions()) {
            RequestPermissionsTask task = new RequestPermissionsTask(UAirship.getApplicationContext(), new RequestPermissionsTask.Callback() {
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

        private final Context context;
        private Callback callback;

        public interface Callback {
            void onResult(boolean enabled);
        }

        RequestPermissionsTask(Context context, Callback callback) {
            this.context = context;
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
    void isLocationEnabled(JSONArray data, CallbackContext callbackContext) {
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
    void setBackgroundLocationEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    void isBackgroundLocationEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getLocationManager().isBackgroundLocationAllowed() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification sound is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isSoundEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isSoundEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification vibration is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isVibrateEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isVibrateEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if quiet time is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isQuietTimeEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if the device is currently in quiet time.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isInQuietTime(JSONArray data, CallbackContext callbackContext) {
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
    void getLaunchNotification(JSONArray data, CallbackContext callbackContext) {
        boolean clear = data.optBoolean(0, false);
        NotificationOpenedEvent event = UAirshipPluginManager.shared().getLastLaunchNotificationEvent(clear);

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
    void getDeepLink(JSONArray data, CallbackContext callbackContext) {
        boolean clear = data.optBoolean(0, false);
        DeepLinkEvent event = UAirshipPluginManager.shared().getLastDeepLinkEvent(clear);
        String deepLink = event == null ? null : event.getDeepLink();
        callbackContext.success(deepLink);
    }

    /**
     * Returns the channel ID.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getChannelID(JSONArray data, CallbackContext callbackContext) {
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
    void getQuietTime(JSONArray data, CallbackContext callbackContext) throws JSONException {
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

        Logger.debug("Returning quiet time");
        callbackContext.success(returnObject);
    }

    /**
     * Returns the tags as an array.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getTags(JSONArray data, CallbackContext callbackContext) throws JSONException {
        Set<String> tags = UAirship.shared().getPushManager().getTags();
        Logger.debug("Returning tags");
        callbackContext.success(new JSONArray(tags));
    }

    /**
     * Returns the alias.
     * <p/>
     * Expected arguments: String
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void getAlias(JSONArray data, CallbackContext callbackContext) {
        String alias = UAirship.shared().getPushManager().getAlias();
        alias = alias != null ? alias : "";
        callbackContext.success(alias);
    }

    /**
     * Sets the alias.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void setAlias(JSONArray data, CallbackContext callbackContext) throws JSONException {
        String alias = data.getString(0);
        if (alias.equals("")) {
            alias = null;
        }

        Logger.debug("Settings alias: " + alias);

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
    void setTags(JSONArray data, CallbackContext callbackContext) throws JSONException {
        HashSet<String> tagSet = new HashSet<String>();
        JSONArray tagsArray = data.getJSONArray(0);
        for (int i = 0; i < tagsArray.length(); ++i) {
            tagSet.add(tagsArray.getString(i));
        }

        Logger.debug("Settings tags: " + tagSet);
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
    void setSoundEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean soundPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setSoundEnabled(soundPreference);
        Logger.debug("Settings Sound: " + soundPreference);
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
    void setVibrateEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean vibrationPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setVibrateEnabled(vibrationPreference);
        Logger.debug("Settings Vibrate: " + vibrationPreference);
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
    void setQuietTimeEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean quietPreference = data.getBoolean(0);
        UAirship.shared().getPushManager().setQuietTimeEnabled(quietPreference);
        Logger.debug("Settings QuietTime: " + quietPreference);
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
    void setQuietTime(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    }

    /**
     * Records the current location.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void recordCurrentLocation(JSONArray data, CallbackContext callbackContext) {
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
    void setAnalyticsEnabled(JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        Logger.debug("Settings analyticsEnabled: " + enabled);
        UAirship.shared().getAnalytics().setEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Checks if analytics is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void isAnalyticsEnabled(JSONArray data, CallbackContext callbackContext) {
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
    void setAssociatedIdentifier(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    void getNamedUser(JSONArray data, CallbackContext callbackContext) {
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
    void setNamedUser(JSONArray data, CallbackContext callbackContext) throws JSONException {
        String namedUserId = data.getString(0);
        if (UAStringUtil.isEmpty(namedUserId)) {
            namedUserId = null;
        }

        Logger.debug("Setting named user: " + namedUserId);

        UAirship.shared().getNamedUser().setId(namedUserId);

        callbackContext.success();
    }

    /**
     * Edits the named user tag groups.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void editNamedUserTagGroups(JSONArray data, CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);

        Logger.debug("Editing named user tag groups: " + operations);

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
    void editChannelTagGroups(JSONArray data, CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);

        Logger.debug("Editing channel tag groups: " + operations);

        TagGroupsEditor editor = UAirship.shared().getPushManager().editTagGroups();
        applyTagGroupOperations(editor, operations);
        editor.apply();

        callbackContext.success();
    }

    /**
     * Runs an Urban Airship action.
     *
     * Expected arguments: String - action name, * - the action value
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    void runAction(JSONArray data, final CallbackContext callbackContext) throws JSONException, ActionValueException {
        final String actionName = data.getString(0);
        final Object actionValue = data.opt(1);

        ActionRunRequest.createRequest(actionName)
                .setValue(actionValue)
                .run(new ActionCompletionCallback() {
                    @Override
                    public void onFinish(ActionArguments arguments, ActionResult result) {

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
        }

        return String.format("Action %s failed with unspecified error", name);
    }

    /**
     * Helper method to create a notification JSONObject.
     *
     * @param message The push message.
     * @param notificationId The notification ID.
     * @return A JSONObject containing the notification data.
     */
    private static JSONObject notificationObject(PushMessage message, Integer notificationId) {
        JSONObject data = new JSONObject();

        if (message == null) {
            return data;
        }

        Map<String, String> extras = new HashMap<String, String>();
        for (String key : message.getPushBundle().keySet()) {
            if ("android.support.content.wakelockid".equals(key)) {
                continue;
            }
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
     * @throws JSONException
     */
    void displayMessageCenter(JSONArray data, CallbackContext callbackContext) throws JSONException {
        String messageId = data.optString(0);

        Logger.debug("Displaying Message Center");
        if (messageId != null) {
            UAirship.shared().getInbox().startMessageActivity(messageId);
        } else {
            UAirship.shared().getInbox().startInboxActivity();
        }
        callbackContext.success();
    }

    /**
     * Deletes an inbox message.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void deleteInboxMessage(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    void markInboxMessageRead(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    void getInboxMessages(JSONArray data, CallbackContext callbackContext) throws JSONException {
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
    void displayInboxMessage(JSONArray data, CallbackContext callbackContext) throws JSONException {
        final String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), MessageActivity.class)
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
     * Displays an inbox message using the LandingPageActivity.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void overlayInboxMessage(JSONArray data, CallbackContext callbackContext) throws JSONException {
        final String messageId = data.getString(0);
        RichPushMessage message = UAirship.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), LandingPageActivity.class)
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
     * Refreshes the inbox.
     *
     * @param data The call data. The message ID is expected to be the first entry.
     * @param callbackContext The callback context.
     * @throws JSONException
     */
    void refreshInbox(JSONArray data, final CallbackContext callbackContext) throws JSONException {
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
     * @throws JSONException
     */
    void isAppNotificationsEnabled(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        int value = UAirship.shared().getPushManager().isOptIn() ? 1 : 0;
        callbackContext.success(value);
    }
}
