/* Copyright 2018 Urban Airship and Contributors */

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
