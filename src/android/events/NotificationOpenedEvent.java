/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Notification opened event.
 */
public class NotificationOpenedEvent extends PushEvent {

    private static final String EVENT_NOTIFICATION_OPENED = "urbanairship.notification_opened";

    private static final String ACTION_ID = "actionID";
    private static final String IS_FOREGROUND = "isForeground";

    private final AirshipReceiver.ActionButtonInfo actionButtonInfo;


    /**
     * Creates an event for a notification response.
     *
     * @param notificationInfo The notification info.
     */
    public NotificationOpenedEvent(@NonNull AirshipReceiver.NotificationInfo notificationInfo) {
        this(notificationInfo, null);
    }

    /**
     * Creates an event for a notification action button response.
     *
     * @param notificationInfo The notification info.
     * @param actionButtonInfo The action button info.
     */
    public NotificationOpenedEvent(@NonNull AirshipReceiver.NotificationInfo notificationInfo, @Nullable AirshipReceiver.ActionButtonInfo actionButtonInfo) {
        super(notificationInfo.getNotificationId(), notificationInfo.getMessage());
        this.actionButtonInfo = actionButtonInfo;
    }

    @Override
    public String getEventName() {
        return EVENT_NOTIFICATION_OPENED;
    }


    @Override
    public JSONObject getEventData() {
        JSONObject jsonObject = super.getEventData();

        try {
            if (actionButtonInfo != null) {
                jsonObject.put(ACTION_ID, actionButtonInfo.getButtonId());
                jsonObject.put(IS_FOREGROUND, actionButtonInfo.isForeground());
            } else {
                jsonObject.put(IS_FOREGROUND, true);
            }
        } catch (JSONException e) {
            Logger.error("Error constructing notification object", e);
        }

        return jsonObject;
    }
}
