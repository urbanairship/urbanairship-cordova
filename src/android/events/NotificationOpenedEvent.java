/*
 Copyright 2009-2017 Urban Airship Inc. All rights reserved.

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

package com.urbanairship.cordova.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.Logger;
import com.urbanairship.push.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
