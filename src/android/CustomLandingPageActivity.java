/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.urbanairship.actions.LandingPageActivity;

public class CustomLandingPageActivity extends LandingPageActivity {

    @NonNull
    public static final String CANCEL_INTENT_ACTION = "CANCEL";

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getIntent() != null && CANCEL_INTENT_ACTION.equals(getIntent().getAction())) {
            finish();
        }
    }

    @Override
    public void onNewIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        if (CANCEL_INTENT_ACTION.equals(intent.getAction())) {
            finish();
            return;
        }

        super.onNewIntent(intent);
    }
}
