package com.urbanairship.phonegap;

import android.app.Application;

import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

public class PushAutopilot extends Autopilot {

    @Override
    public void execute(Application application) {
        UAirship.takeOff(application);
        PushManager.shared().setIntentReceiver(PushReceiver.class);
        if (UAirship.shared().getAirshipConfigOptions().pushServiceEnabled) {
            PushManager.enablePush();
        }
    }
}
