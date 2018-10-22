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

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(Context context) {
        return PluginManager.shared(context).getAirshipConfig();
    }

    @Override
    public boolean isReady(@NonNull Context context) {
        return PluginManager.shared(context).getAirshipConfig() != null;
    }

    @Override
    public boolean allowEarlyTakeOff(@NonNull Context context) {
        return false;
    }

    @Override
    public void onAirshipReady(UAirship airship) {
        Context context = UAirship.getApplicationContext();
        final PluginManager pluginManager = PluginManager.shared(context);

        if (pluginManager.getEnablePushOnLaunch()) {
            airship.getPushManager().setUserNotificationsEnabled(true);
        }

        // Cordova notification factory
        airship.getPushManager().setNotificationFactory(new CordovaNotificationFactory(context));

        // Deep link
        airship.getActionRegistry().getEntry(DeepLinkAction.DEFAULT_REGISTRY_NAME).setDefaultAction(new DeepLinkAction() {
            @Override
            public ActionResult perform(@NonNull ActionArguments arguments) {
                String deepLink = arguments.getValue().getString();
                if (deepLink != null) {
                    pluginManager.deepLinkReceived(deepLink);
                }
                return ActionResult.newResult(arguments.getValue());
            }
        });

        // Auto launch message center
        airship.getActionRegistry()
                .getEntry(OverlayRichPushMessageAction.DEFAULT_REGISTRY_NAME)
                .setPredicate(new ActionRegistry.Predicate() {
                    @Override
                    public boolean apply(ActionArguments actionArguments) {
                        if (actionArguments.getSituation() == Action.SITUATION_PUSH_OPENED) {
                            return pluginManager.getAutoLaunchMessageCenter();
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
                            return pluginManager.getAutoLaunchMessageCenter();
                        }

                        return true;
                    }
                });

        // Inbox updates
        airship.getInbox().addListener(new RichPushInbox.Listener() {
            @Override
            public void onInboxUpdated() {
                pluginManager.inboxUpdated();
            }
        });
    }
}
