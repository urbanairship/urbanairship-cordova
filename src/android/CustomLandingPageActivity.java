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

        /*
         * From http://developer.android.com/reference/android/webkit/WebSettings.html#setDatabaseEnabled(boolean):
         * "This setting is global in effect, across all WebView instances in a process. Note you should only
         *  modify this setting prior to making any WebView page load within a given process, as the WebView
         *  implementation may ignore changes to this setting after that point."
         *
         * So in order to not break applications that depend on the web store APIs, we must enable
         * this setting for our landing page in case it is the first web view to load content.
         */
        UAWebView webView = (UAWebView) findViewById(android.R.id.primary);
        webView.getSettings().setDatabaseEnabled(true);
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
