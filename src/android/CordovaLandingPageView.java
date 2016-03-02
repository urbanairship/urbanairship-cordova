/*
Copyright 2009-2016 Urban Airship Inc. All rights reserved.

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

package com.urbanairship.cordova;

import android.content.Context;
import android.util.AttributeSet;

import com.urbanairship.widget.UAWebView;

public class CordovaLandingPageView extends UAWebView {
    public CordovaLandingPageView(Context context) {
        super(context);
    }

    public CordovaLandingPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CordovaLandingPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CordovaLandingPageView(Context context, AttributeSet attrs, int defStyle, int defResStyle) {
        super(context, attrs, defStyle, defResStyle);
    }

    @Override
    protected void initializeView() {
        /*
         * From http://developer.android.com/reference/android/webkit/WebSettings.html#setDatabaseEnabled(boolean):
         * "This setting is global in effect, across all WebView instances in a process. Note you should only
         *  modify this setting prior to making any WebView page load within a given process, as the WebView
         *  implementation may ignore changes to this setting after that point."
         *
         * So in order to not break applications that depend on the web store APIs, we must enable
         * this setting for our landing page in case it is the first web view to load content.
         */
        this.getSettings().setDatabaseEnabled(true);
    }
}
