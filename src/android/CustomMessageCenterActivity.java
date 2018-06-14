/* Copyright 2018 Urban Airship and Contributors */

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
