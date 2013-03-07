package com.urbanairship.phonegap.plugins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.phonegap.sample.UAPhonegapSample;
import com.urbanairship.push.PushManager;
import com.urbanairship.util.ServiceNotBoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PushNotificationPluginIntentReceiver extends BroadcastReceiver {

    private Map<String, String> getNotificationExtras(Intent intent) {
        Map<String, String> extrasMap = new HashMap<String, String>();

        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {

            // ignore standard C2DM extra key
            List<String> ignoredKeys = (List<String>) Arrays.asList(
                    "collapse_key",// c2dm collapse key
                    "from",// c2dm sender
                    PushManager.EXTRA_NOTIFICATION_ID,// int id of generated
                    // notification
                    // (ACTION_PUSH_RECEIVED
                    // only)
                    PushManager.EXTRA_PUSH_ID,// internal UA push id
                    PushManager.EXTRA_ALERT);// ignore alert
            if (ignoredKeys.contains(key)) {
                continue;
            }

            extrasMap.put(key, intent.getStringExtra(key));
        }

        return extrasMap;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.info("Received intent: " + intent.toString());
        String action = intent.getAction();

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {
            int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

            String alert = intent.getStringExtra(PushManager.EXTRA_ALERT);
            Map<String, String> extras = getNotificationExtras(intent);

            Logger.info("Received push notification. Alert: " + alert
                    + ". Payload: " + extras.toString() + ". NotificationID="
                    + id);

            PushNotificationPlugin plugin = PushNotificationPlugin
                    .getInstance();
            Logger.info("Got Extras: " + extras);
            Logger.info("Got Alert: " + alert);

            plugin.raisePush(alert, extras);

        } else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {

            String alert = intent.getStringExtra(PushManager.EXTRA_ALERT);
            Map<String, String> extras = getNotificationExtras(intent);

            Logger.info("User clicked notification. Message: " + alert
                    + ". Payload: " + extras.toString());

            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.setClass(UAirship.shared().getApplicationContext(),
                    UAPhonegapSample.class);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PushNotificationPlugin.incomingAlert = alert;
            PushNotificationPlugin.incomingExtras = extras;

            Logger.info("Plugin Awesome user clicked!");

            UAirship.shared().getApplicationContext().startActivity(launch);

        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            PushNotificationPlugin plugin = PushNotificationPlugin
                    .getInstance();
            String apid = intent.getStringExtra(PushManager.EXTRA_APID);
            Boolean valid = intent.getBooleanExtra(
                    PushManager.EXTRA_REGISTRATION_VALID, false);
            Logger.info("Registration complete. APID:"
                    + intent.getStringExtra(PushManager.EXTRA_APID)
                    + ". Valid: "
                    + intent.getBooleanExtra(
                            PushManager.EXTRA_REGISTRATION_VALID, false));
            plugin.raiseRegistration(valid, apid);

        } else if (action
                .equals(UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_SERVICE_BOUND))) {
            try {
                UALocationManager.shared().recordCurrentLocation();
                Logger.info("Location successfully recorded on Intent");
            } catch (ServiceNotBoundException e) {
                Logger.error("Recording current location on Intent failed");
                e.printStackTrace();
            } catch (RemoteException e) {
                Logger.error("zomg flailsauce");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
