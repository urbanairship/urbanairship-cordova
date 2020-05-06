/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionCompletionCallback;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.ActionRunRequest;
import com.urbanairship.channel.AttributeEditor;
import com.urbanairship.channel.TagGroupsEditor;
import com.urbanairship.cordova.events.DeepLinkEvent;
import com.urbanairship.cordova.events.Event;
import com.urbanairship.cordova.events.NotificationOpenedEvent;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.json.JsonMap;
import com.urbanairship.json.JsonValue;
import com.urbanairship.messagecenter.Inbox;
import com.urbanairship.messagecenter.Message;
import com.urbanairship.messagecenter.MessageCenter;
import com.urbanairship.push.PushMessage;
import com.urbanairship.util.UAStringUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private final static List<String> AIRSHIP_ACTIONS = Arrays.asList("setUserNotificationsEnabled",
            "isUserNotificationsEnabled", "isSoundEnabled", "isVibrateEnabled", "isQuietTimeEnabled", "isInQuietTime",
            "getLaunchNotification", "getChannelID", "getQuietTime", "getTags", "setTags", "setSoundEnabled", "setVibrateEnabled",
            "setQuietTimeEnabled", "setQuietTime", "clearNotifications", "setAnalyticsEnabled", "isAnalyticsEnabled",
            "setNamedUser", "getNamedUser", "runAction", "editNamedUserTagGroups", "editChannelTagGroups", "displayMessageCenter", "markInboxMessageRead",
            "deleteInboxMessage", "getInboxMessages", "displayInboxMessage", "refreshInbox", "getDeepLink", "setAssociatedIdentifier",
            "isAppNotificationsEnabled", "dismissMessageCenter", "dismissInboxMessage", "setAutoLaunchDefaultMessageCenter",
            "getActiveNotifications", "clearNotification", "editChannelAttributes", "trackScreen", "setDataCollectionEnabled", "isDataCollectionEnabled",
            "setPushTokenRegistrationEnabled", "isPushTokenRegistrationEnabled");

    /*
     * These actions are available even if airship is not ready.
     */
    private final static List<String> GLOBAL_ACTIONS = Arrays.asList("takeOff", "registerListener", "setAndroidNotificationConfig");
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final String NOTIFICATION_ICON_KEY = "icon";
    private static final String NOTIFICATION_LARGE_ICON_KEY = "largeIcon";
    private static final String ACCENT_COLOR_KEY = "accentColor";
    private static final String DEFAULT_CHANNEL_ID_KEY = "defaultChannelId";

    private static final String ATTRIBUTE_OPERATION_KEY = "key";
    private static final String ATTRIBUTE_OPERATION_VALUE = "value";
    private static final String ATTRIBUTE_OPERATION_TYPE = "action";
    private static final String ATTRIBUTE_OPERATION_VALUETYPE = "type";
    private static final String ATTRIBUTE_OPERATION_SET = "set";
    private static final String ATTRIBUTE_OPERATION_REMOVE = "remove";

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
     * All methods will be executed in the ExecutorService. Any exceptions thrown by the actions are automatically caught
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
                    if ("clearNotification".equals(action)) {
                        clearNotification(data, callbackContext);
                    } else if ("clearNotifications".equals(action)) {
                        clearNotifications(data, callbackContext);
                    } else if ("deleteInboxMessage".equals(action)) {
                        deleteInboxMessage(data, callbackContext);
                    } else if ("dismissInboxMessage".equals(action)) {
                        dismissInboxMessage(data, callbackContext);
                    } else if ("dismissMessageCenter".equals(action)) {
                        dismissMessageCenter(data, callbackContext);
                    } else if ("displayInboxMessage".equals(action)) {
                        displayInboxMessage(data, callbackContext);
                    } else if ("displayMessageCenter".equals(action)) {
                        displayMessageCenter(data, callbackContext);
                    } else if ("editChannelAttributes".equals(action)) {
                        editChannelAttributes(data, callbackContext);
                    } else if ("editChannelTagGroups".equals(action)) {
                        editChannelTagGroups(data, callbackContext);
                    } else if ("editNamedUserTagGroups".equals(action)) {
                        editNamedUserTagGroups(data, callbackContext);
                    } else if ("getActiveNotifications".equals(action)) {
                        getActiveNotifications(data, callbackContext);
                    } else if ("getChannelID".equals(action)) {
                        getChannelID(data, callbackContext);
                    } else if ("getDeepLink".equals(action)) {
                        getDeepLink(data, callbackContext);
                    } else if ("getInboxMessages".equals(action)) {
                        getInboxMessages(data, callbackContext);
                    } else if ("getLaunchNotification".equals(action)) {
                        getLaunchNotification(data, callbackContext);
                    } else if ("getNamedUser".equals(action)) {
                        getNamedUser(data, callbackContext);
                    } else if ("getQuietTime".equals(action)) {
                        getQuietTime(data, callbackContext);
                    } else if ("getTags".equals(action)) {
                        getTags(data, callbackContext);
                    } else if ("isAnalyticsEnabled".equals(action)) {
                        isAnalyticsEnabled(data, callbackContext);
                    } else if ("isAppNotificationsEnabled".equals(action)) {
                        isAppNotificationsEnabled(data, callbackContext);
                    } else if ("isDataCollectionEnabled".equals(action)) {
                        isDataCollectionEnabled(data, callbackContext);
                    } else if ("isInQuietTime".equals(action)) {
                        isInQuietTime(data, callbackContext);
                    } else if ("isPushTokenRegistrationEnabled".equals(action)) {
                        isPushTokenRegistrationEnabled(data, callbackContext);
                    } else if ("isQuietTimeEnabled".equals(action)) {
                        isQuietTimeEnabled(data, callbackContext);
                    } else if ("isSoundEnabled".equals(action)) {
                        isSoundEnabled(data, callbackContext);
                    } else if ("isUserNotificationsEnabled".equals(action)) {
                        isUserNotificationsEnabled(data, callbackContext);
                    } else if ("isVibrateEnabled".equals(action)) {
                        isVibrateEnabled(data, callbackContext);
                    } else if ("markInboxMessageRead".equals(action)) {
                        markInboxMessageRead(data, callbackContext);
                    } else if ("refreshInbox".equals(action)) {
                        refreshInbox(data, callbackContext);
                    } else if ("registerListener".equals(action)) {
                        registerListener(data, callbackContext);
                    } else if ("runAction".equals(action)) {
                        runAction(data, callbackContext);
                    } else if ("setAnalyticsEnabled".equals(action)) {
                        setAnalyticsEnabled(data, callbackContext);
                    } else if ("setAndroidNotificationConfig".equals(action)) {
                        setAndroidNotificationConfig(data, callbackContext);
                    } else if ("setAssociatedIdentifier".equals(action)) {
                        setAssociatedIdentifier(data, callbackContext);
                    } else if ("setAutoLaunchDefaultMessageCenter".equals(action)) {
                        setAutoLaunchDefaultMessageCenter(data, callbackContext);
                    } else if ("setDataCollectionEnabled".equals(action)) {
                        setDataCollectionEnabled(data, callbackContext);
                    } else if ("setNamedUser".equals(action)) {
                        setNamedUser(data, callbackContext);
                    } else if ("setPushTokenRegistrationEnabled".equals(action)) {
                        setPushTokenRegistrationEnabled(data, callbackContext);
                    } else if ("setQuietTime".equals(action)) {
                        setQuietTime(data, callbackContext);
                    } else if ("setQuietTimeEnabled".equals(action)) {
                        setQuietTimeEnabled(data, callbackContext);
                    } else if ("setSoundEnabled".equals(action)) {
                        setSoundEnabled(data, callbackContext);
                    } else if ("setTags".equals(action)) {
                        setTags(data, callbackContext);
                    } else if ("setUserNotificationsEnabled".equals(action)) {
                        setUserNotificationsEnabled(data, callbackContext);
                    } else if ("setVibrateEnabled".equals(action)) {
                        setVibrateEnabled(data, callbackContext);
                    } else if ("takeOff".equals(action)) {
                        takeOff(data, callbackContext);
                    } else if ("trackScreen".equals(action)) {
                        trackScreen(data, callbackContext);
                    } else {
                        PluginLogger.debug("No implementation for action: %s", action);
                        callbackContext.error("No implementation for action " + action);
                    }
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
    private void registerListener(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
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
    private void takeOff(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
        JsonMap config = JsonValue.wrapOpt(data.getJSONObject(0)).optMap();
        JsonMap prod = config.opt("production").optMap();
        JsonMap dev = config.opt("development").optMap();

        pluginManager.editConfig()
                .setProductionConfig(prod.opt("appKey").optString(), prod.opt("appSecret").optString())
                .setDevelopmentConfig(dev.opt("appKey").optString(), dev.opt("appSecret").optString())
                .setCloudSite(config.opt("site").optString())
                .setDataCollectionOptInEnabled(config.opt("dataCollectionOptInEnabled").getBoolean(false))
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
    private void setAndroidNotificationConfig(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONObject config = data.getJSONObject(0);

        // Factory will pull the latest values from the config.
        pluginManager.editConfig()
                .setNotificationIcon(config.optString(NOTIFICATION_ICON_KEY))
                .setNotificationLargeIcon(config.optString(NOTIFICATION_LARGE_ICON_KEY))
                .setNotificationAccentColor(config.optString(ACCENT_COLOR_KEY))
                .setDefaultNotificationChannelId(config.optString(DEFAULT_CHANNEL_ID_KEY))
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
    private void setAutoLaunchDefaultMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void clearNotifications(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        NotificationManagerCompat.from(context).cancelAll();
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
    private void setUserNotificationsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void isUserNotificationsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().getUserNotificationsEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification sound is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isSoundEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isSoundEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if notification vibration is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isVibrateEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isVibrateEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if quiet time is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isQuietTimeEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isQuietTimeEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Checks if the device is currently in quiet time.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isInQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void getLaunchNotification(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void getDeepLink(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void getChannelID(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String channelId = UAirship.shared().getChannel().getId();
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
    private void getQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void getTags(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        Set<String> tags = UAirship.shared().getChannel().getTags();
        PluginLogger.debug("Returning tags");
        callbackContext.success(new JSONArray(tags));
    }

    /**
     * Sets the tags.
     * <p/>
     * Expected arguments: An array of Strings
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void setTags(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        HashSet<String> tagSet = new HashSet<String>();
        JSONArray tagsArray = data.getJSONArray(0);
        for (int i = 0; i < tagsArray.length(); ++i) {
            tagSet.add(tagsArray.getString(i));
        }

        PluginLogger.debug("Settings tags: %s", tagSet);
        UAirship.shared().getChannel().setTags(tagSet);

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
    @SuppressWarnings("deprecation")
    private void setSoundEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    @SuppressWarnings("deprecation")
    private void setVibrateEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    @SuppressWarnings("deprecation")
    private void setQuietTimeEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    @SuppressWarnings("deprecation")
    private void setQuietTime(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void setAnalyticsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void isAnalyticsEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void setAssociatedIdentifier(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void getNamedUser(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void setNamedUser(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void editNamedUserTagGroups(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
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
    private void editChannelTagGroups(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);

        PluginLogger.debug("Editing channel tag groups: %s", operations);

        TagGroupsEditor editor = UAirship.shared().getChannel().editTagGroups();
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
    private void runAction(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
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
    private void displayMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        String messageId = data.optString(0);

        PluginLogger.debug("Displaying Message Center");
        if (!UAStringUtil.isEmpty(messageId)) {
            Intent intent = new Intent(cordova.getActivity(), CustomMessageCenterActivity.class)
                    .setAction(MessageCenter.VIEW_MESSAGE_INTENT_ACTION)
                    .setPackage(cordova.getActivity().getPackageName())
                    .setData(Uri.fromParts(MessageCenter.MESSAGE_DATA_SCHEME, messageId, null))
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
    private void dismissMessageCenter(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
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
    private void deleteInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String messageId = data.getString(0);
        Message message = MessageCenter.shared().getInbox().getMessage(messageId);

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
    private void markInboxMessageRead(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String messageId = data.getString(0);
        Message message = MessageCenter.shared().getInbox().getMessage(messageId);

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
    private void getInboxMessages(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray messagesJson = new JSONArray();

        for (Message message : MessageCenter.shared().getInbox().getMessages()) {
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
    private void displayInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        final String messageId = data.getString(0);
        Message message = MessageCenter.shared().getInbox().getMessage(messageId);

        if (message == null) {
            callbackContext.error("Message not found: " + messageId);
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), CustomMessageActivity.class)
                        .setAction(MessageCenter.VIEW_MESSAGE_INTENT_ACTION)
                        .setPackage(cordova.getActivity().getPackageName())
                        .setData(Uri.fromParts(MessageCenter.MESSAGE_DATA_SCHEME, messageId, null))
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
    private void dismissInboxMessage(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        PluginLogger.debug("Dismissing Inbox Message");
        Intent intent = new Intent(cordova.getActivity(), CustomMessageActivity.class)
                .setAction(CustomMessageActivity.CLOSE_INTENT_ACTION)
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
    private void refreshInbox(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageCenter.shared().getInbox().fetchMessages(new Inbox.FetchMessagesCallback() {
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
    private void isAppNotificationsEnabled(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isOptIn() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Gets currently active notifications.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void getActiveNotifications(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) {
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
    private void clearNotification(@NonNull JSONArray data, @NonNull final CallbackContext callbackContext) throws JSONException {
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
            id = Integer.parseInt(parts[0]);
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

    /**
     * Edits the channel attributes.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void editChannelAttributes(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        JSONArray operations = data.getJSONArray(0);
        PluginLogger.debug("Editing channel attributes: %s", operations);

        AttributeEditor editor = UAirship.shared().getChannel().editAttributes();
        for (int i = 0; i < operations.length(); i++) {
            JSONObject operation = operations.optJSONObject(i);
            if (operation == null) {
                continue;
            }

            String action = operation.optString(ATTRIBUTE_OPERATION_TYPE);
            String key = operation.optString(ATTRIBUTE_OPERATION_KEY);

            if (ATTRIBUTE_OPERATION_SET.equals(action)) {
                Object value = operation.opt(ATTRIBUTE_OPERATION_VALUE);
                String valueType = (String) operation.opt(ATTRIBUTE_OPERATION_VALUETYPE);
                if ("date".equals(valueType)) {
                    // Date type doesn't survive through the JS to native bridge. Value contains number of milliseconds since the epoch.
                    try {
                        Date date = new Date((Long) value);
                        if (date != null) {
                            editor.setAttribute(key, date);
                        }
                    } catch (Exception e) {
                        PluginLogger.warn(e, "Failed to parse date attribute: %d", value);
                    }
                } else if ("string".equals(valueType)) {
                    editor.setAttribute(key, (String) value);
                } else if ("number".equals(valueType)) {
                    editor.setAttribute(key, ((Number) value).doubleValue());
                } else if (value instanceof String) {
                    editor.setAttribute(key, (String) value);
                } else if (value instanceof Number) {
                    editor.setAttribute(key, ((Number) value).doubleValue());
                }
            } else if (ATTRIBUTE_OPERATION_REMOVE.equals(action)) {
                editor.removeAttribute(key);
            }
        }

        editor.apply();
        callbackContext.success();
    }

    /**
     * Initiates screen tracking for a specific app screen.
     * <p/>
     * Expected arguments: String
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void trackScreen(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        String screen = data.getString(0);
        UAirship.shared().getAnalytics().trackScreen(screen);
        callbackContext.success();
    }

    /**
     * Enables or disables data collection.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void setDataCollectionEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().setDataCollectionEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Checks if data collection is enabled or not.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isDataCollectionEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().isDataCollectionEnabled() ? 1 : 0;
        callbackContext.success(value);
    }

    /**
     * Sets whether the push token is sent during channel registration.
     * <p/>
     * Expected arguments: Boolean
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void setPushTokenRegistrationEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) throws JSONException {
        boolean enabled = data.getBoolean(0);
        UAirship.shared().getPushManager().setPushTokenRegistrationEnabled(enabled);
        callbackContext.success();
    }

    /**
     * Determines whether the push token is sent during channel registration.
     *
     * @param data The call data.
     * @param callbackContext The callback context.
     */
    private void isPushTokenRegistrationEnabled(@NonNull JSONArray data, @NonNull CallbackContext callbackContext) {
        int value = UAirship.shared().getPushManager().isPushTokenRegistrationEnabled() ? 1 : 0;
        callbackContext.success(value);
    }
}
