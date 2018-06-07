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

package com.urbanairship.push;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;

import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.NotificationActionButtonGroup;

import java.util.Map;

/**
 * Utility class for adding notification button action groups via XML
 */
public class NotificationButtonGroupUtils {
    /**
     * Adds notification action button groups from an xml file.
     * Example entry:
     * <pre>{@code
     * <UrbanAirshipActionButtonGroup id="custom_group">
     *  <UrbanAirshipActionButton
     *      foreground="true"
     *      id="yes"
     *      android:icon="@drawable/ua_ic_notification_button_accept"
     *      android:label="@string/ua_notification_button_yes"/>
     *  <UrbanAirshipActionButton
     *      foreground="false"
     *      id="no"
     *      android:icon="@drawable/ua_ic_notification_button_decline"
     *      android:label="@string/ua_notification_button_no"/>
     * </UrbanAirshipActionButtonGroup> }</pre>
     *
     * @param context The application context.
     * @param resId The xml resource ID.
     */
    public static void addNotificationActionButtonGroups(@NonNull Context context, @XmlRes int resId) {
        Map<String, NotificationActionButtonGroup> groups = ActionButtonGroupsParser.fromXml(context, resId);
        for (Map.Entry<String, NotificationActionButtonGroup> entry : groups.entrySet()) {
            UAirship.shared().getPushManager().addNotificationActionButtonGroup(entry.getKey(), entry.getValue());
        }
    }
}
