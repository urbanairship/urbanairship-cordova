/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.support.annotation.NonNull;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.actions.Action;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionRegistry;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.OpenRichPushInboxAction;
import com.urbanairship.actions.OverlayRichPushMessageAction;
import com.urbanairship.richpush.RichPushInbox;

/**
 * The Urban Airship autopilot to automatically handle takeOff.
 */
public class CordovaAutopilot extends Autopilot {

    public static boolean isReady = false;

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(Context context) {
        return PluginConfig.shared(context).getAirshipConfig();
    }

    @Override
    public boolean isReady(@NonNull Context context) {
        if (PluginConfig.shared(context).getAirshipConfig() != null) {
            isReady = true;
        }

        return isReady;
    }

    @Override
    public void onAirshipReady(UAirship airship) {
        Context context = UAirship.getApplicationContext();
        PluginConfig pluginConfig = PluginConfig.shared(context);

        if (pluginConfig.getEnablePushOnLaunch()) {
            airship.getPushManager().setUserNotificationsEnabled(true);
        }

        // Large Icon
        airship.getPushManager()
                .getNotificationFactory()
                .setLargeIcon(pluginConfig.getNotificationLargeIcon());

        // Sound
        airship.getPushManager()
                .getNotificationFactory()
                .setSound(pluginConfig.getNotificationSound());

        // Deep link
        airship.getActionRegistry().getEntry(DeepLinkAction.DEFAULT_REGISTRY_NAME).setDefaultAction(new DeepLinkAction() {
            @Override
            public ActionResult perform(@NonNull ActionArguments arguments) {
                String deepLink = arguments.getValue().getString();
                if (deepLink != null) {
                    PluginManager.shared().deepLinkReceived(deepLink);
                }
                return ActionResult.newResult(arguments.getValue());
            }
        });

        // Auto launch message center
        final boolean autoLaunchMessageCenter = pluginConfig.getAutoLaunchMessageCenter();
        airship.getActionRegistry()
                .getEntry(OverlayRichPushMessageAction.DEFAULT_REGISTRY_NAME)
                .setPredicate(new ActionRegistry.Predicate() {
                    @Override
                    public boolean apply(ActionArguments actionArguments) {
                        if (actionArguments.getSituation() == Action.SITUATION_PUSH_OPENED) {
                            return autoLaunchMessageCenter;
                        }

                        return true;
                    }
                });

        // Auto launch message center
        airship.getActionRegistry()
                .getEntry(OpenRichPushInboxAction.DEFAULT_REGISTRY_NAME)
                .setPredicate(new ActionRegistry.Predicate() {
                    @Override
                    public boolean apply(ActionArguments actionArguments) {
                        if (actionArguments.getSituation() == Action.SITUATION_PUSH_OPENED) {
                            return autoLaunchMessageCenter;
                        }

                        return true;
                    }
                });

        // Inbox updates
        airship.getInbox().addListener(new RichPushInbox.Listener() {
            @Override
            public void onInboxUpdated() {
                PluginManager.shared().inboxUpdated();
            }
        });
    }
}
