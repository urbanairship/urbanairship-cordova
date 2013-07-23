package com.urbanairship.phonegap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushReceiver extends BroadcastReceiver {

    private static final List<String> IGNORED_EXTRAS_KEYS = Arrays.asList(
            "collapse_key",// c2dm collapse key
            "from", // c2dm sender
            PushManager.EXTRA_NOTIFICATION_ID, // int id of generated
            PushManager.EXTRA_PUSH_ID, // internal UA push id
            PushManager.EXTRA_ALERT); // ignore alert

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.info("Received intent: " + intent.toString());
        String action = intent.getAction();

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {
            handlePushReceived(intent);
        } else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
            handleNotificationOpened(context, intent);
        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            handleRegistrationFinished(intent);
        }
    }

    private void handlePushReceived(Intent intent) {
        int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);
        String alert = intent.getStringExtra(PushManager.EXTRA_ALERT);
        Map<String, String> extras = getNotificationExtras(intent);

        Logger.info("Received push notification. Alert: " + alert +
                ". Payload: " + extras + ". NotificationID=" + id);

        PushNotificationPlugin.raisePush(alert, extras);
    }

    private void handleNotificationOpened(Context context, Intent intent) {
        String alert = intent.getStringExtra(PushManager.EXTRA_ALERT);
        Map<String, String> extras = getNotificationExtras(intent);

        Logger.info("User clicked notification. Message: " + alert
                + ". Payload: " + extras.toString());

        Intent launch = context.getPackageManager().getLaunchIntentForPackage(UAirship.getPackageName());
        launch.addCategory(Intent.CATEGORY_LAUNCHER);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PushNotificationPlugin.incomingAlert = alert;
        PushNotificationPlugin.incomingExtras = extras;

        context.startActivity(launch);
    }

    private void handleRegistrationFinished(Intent intent) {
        String apid = intent.getStringExtra(PushManager.EXTRA_APID);
        Boolean valid = intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false);

        Logger.info("Registration complete. APID:"
                + intent.getStringExtra(PushManager.EXTRA_APID)
                + ". Valid: "
                + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));

        PushNotificationPlugin.raiseRegistration(valid, apid);
    }


    private Map<String, String> getNotificationExtras(Intent intent) {
        Map<String, String> extrasMap = new HashMap<String, String>();

        for (String key : intent.getExtras().keySet()) {
            if (!IGNORED_EXTRAS_KEYS.contains(key)) {
                extrasMap.put(key, intent.getStringExtra(key));
            }
        }

        return extrasMap;
    }
}
