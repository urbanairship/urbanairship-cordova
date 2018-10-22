/* Copyright 2018 Urban Airship and Contributors */

package com.urbanairship.cordova;

import android.content.Intent;
import android.os.Bundle;
import com.urbanairship.widget.UAWebView;

import com.urbanairship.actions.LandingPageActivity;

public class CustomLandingPageActivity extends LandingPageActivity {

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getIntent() != null && "CANCEL".equals(getIntent().getAction())) {
            finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent != null && "CANCEL".equals(intent.getAction())) {
            finish();
            return;
        }

        super.onNewIntent(intent);
    }
}
