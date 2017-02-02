/*
 Copyright 2009-2017 Urban Airship Inc. All rights reserved.

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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.urbanairship.Logger;
import com.urbanairship.messagecenter.MessageActivity;
import com.urbanairship.richpush.RichPushInbox;

public class CustomMessageActivity extends MessageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && "CLOSE".equals(getIntent().getAction())) {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && "CLOSE".equals(intent.getAction())) {
            finish();
            return;
        }

        String messageId = null;

        // Handle the "com.urbanairship.VIEW_RICH_PUSH_MESSAGE" intent action with the message
        // ID encoded in the intent's data in the form of "message:<MESSAGE_ID>
        if (getIntent() != null && getIntent().getData() != null
                && RichPushInbox.VIEW_MESSAGE_INTENT_ACTION.equals(getIntent().getAction())) {
            messageId = intent.getData().getSchemeSpecificPart();

            Logger.debug("Relaunching activity");

            finish();

            Intent restartIntent = new Intent()
                    .setClass(this, this.getClass())
                    .setAction(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                    .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, messageId, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            if (intent.getExtras() != null) {
                restartIntent.putExtras(intent.getExtras());
            }

            this.startActivity(restartIntent);
        }
    }
}
