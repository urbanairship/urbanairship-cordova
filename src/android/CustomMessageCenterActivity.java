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
import android.os.Bundle;

import com.urbanairship.messagecenter.MessageCenterActivity;
import com.urbanairship.messagecenter.MessageCenterFragment;
import com.urbanairship.richpush.RichPushInbox;

public class CustomMessageCenterActivity extends MessageCenterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && "CLOSE".equals(getIntent().getAction())) {
            finish();
            return;
        }

        if (getIntent() != null && getIntent().getData() != null && RichPushInbox.VIEW_INBOX_INTENT_ACTION.equals(getIntent().getAction())) {
            String messageId = getIntent().getData().getSchemeSpecificPart();
            getSupportFragmentManager().executePendingTransactions();
            MessageCenterFragment fragment = (MessageCenterFragment) getSupportFragmentManager().findFragmentByTag("MESSAGE_CENTER_FRAGMENT");
            fragment.setMessageID(messageId);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && "CLOSE".equals(intent.getAction())) {
            finish();
            return;
        }

        if (intent != null && intent.getData() != null && intent.getAction() != null) {
            String s = intent.getAction();
            if (s.equals(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION) || s.equals(RichPushInbox.VIEW_INBOX_INTENT_ACTION)) {
                String messageId = getIntent().getData().getSchemeSpecificPart();
                MessageCenterFragment fragment = (MessageCenterFragment) getSupportFragmentManager().findFragmentByTag("MESSAGE_CENTER_FRAGMENT");
                fragment.setMessageID(messageId);

            }
        }
    }
}
