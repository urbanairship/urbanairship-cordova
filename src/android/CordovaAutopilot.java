/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;

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
import com.urbanairship.push.PushMessage;
import com.urbanairship.cordova.events.ShowInboxEvent;


/**
 * The Urban Airship autopilot to automatically handle takeOff.
 */
public class CordovaAutopilot extends Autopilot {

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        AirshipConfigOptions configOptions = PluginManager.shared(context).getAirshipConfig();
        if (configOptions != null) {
            PluginLogger.setLogLevel(configOptions.getLoggerLevel());
        }
        return configOptions;
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
    public void onAirshipReady(@NonNull UAirship airship) {
        Context context = UAirship.getApplicationContext();
        final PluginManager pluginManager = PluginManager.shared(context);

        if (pluginManager.getEnablePushOnLaunch()) {
            airship.getPushManager().setUserNotificationsEnabled(true);
        }

        // Cordova notification factory
        airship.getPushManager().setNotificationFactory(new CordovaNotificationFactory(context));

        // Deep link
        airship.getActionRegistry().getEntry(DeepLinkAction.DEFAULT_REGISTRY_NAME).setDefaultAction(new DeepLinkAction() {
            @NonNull
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

        loadCustomNotificationButtonGroups(context, airship);

        // Replace the message center actions to control auto launch behavior
        airship.getActionRegistry()
                .getEntry(OverlayRichPushMessageAction.DEFAULT_REGISTRY_NAME)
                .setDefaultAction(new CustomOverlayRichPushMessageAction());

        airship.getActionRegistry()
                .getEntry(OpenRichPushInboxAction.DEFAULT_REGISTRY_NAME)
                .setDefaultAction(new CustomOpenRichPushMessageAction());
    }

    private void loadCustomNotificationButtonGroups(Context context, UAirship airship) {
        String packageName = UAirship.shared().getPackageName();
        @XmlRes int resId = context.getResources().getIdentifier("ua_custom_notification_buttons", "xml", packageName);

         if (resId != 0) {
            airship.getPushManager().addNotificationActionButtonGroups(context, resId);
        }
    }

    private static void sendShowInboxEvent(@NonNull ActionArguments arguments) {
        Context context = UAirship.getApplicationContext();
        String messageId = arguments.getValue().getString();

        if (messageId.equalsIgnoreCase(OverlayRichPushMessageAction.MESSAGE_ID_PLACEHOLDER)) {
            PushMessage pushMessage = arguments.getMetadata().getParcelable(ActionArguments.PUSH_MESSAGE_METADATA);
            if (pushMessage != null && pushMessage.getRichPushMessageId() != null) {
                messageId = pushMessage.getRichPushMessageId();
            } else if (arguments.getMetadata().containsKey(ActionArguments.RICH_PUSH_ID_METADATA)) {
                messageId = arguments.getMetadata().getString(ActionArguments.RICH_PUSH_ID_METADATA);
            } else {
                messageId = null;
            }
        }

        PluginManager.shared(context).sendShowInboxEvent(new ShowInboxEvent(messageId));
    }

    private static class CustomOverlayRichPushMessageAction extends OverlayRichPushMessageAction {
        @NonNull
        @Override
        public ActionResult perform(@NonNull ActionArguments arguments) {
            if (PluginManager.shared(UAirship.getApplicationContext()).getAutoLaunchMessageCenter()) {
                return super.perform(arguments);
            } else {
                sendShowInboxEvent(arguments);
                return ActionResult.newEmptyResult();
            }
        }
    }

    private static class CustomOpenRichPushMessageAction extends OpenRichPushInboxAction {
        @NonNull
        @Override
        public ActionResult perform(@NonNull ActionArguments arguments) {
            if (PluginManager.shared(UAirship.getApplicationContext()).getAutoLaunchMessageCenter()) {
                return super.perform(arguments);
            } else {
                sendShowInboxEvent(arguments);
                return ActionResult.newEmptyResult();
            }
        }
    }
}
