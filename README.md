# Urban Airship PhoneGap/Cordova Plugin

This plugin supports PhoneGap/Cordova apps running on both iOS and Android.

### Resources:
 - [Getting started guide](http://docs.urbanairship.com/platform/phonegap.html)
 - [JSDocs](http://docs.urbanairship.com/reference/libraries/urbanairship-cordova/latest/)
 - [Migration docs](MIGRATION.md)

### Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
[Code Contribution Agreement](http://docs.urbanairship.com/contribution-agreement.html).

### Issues

Please contact support@urbanairship.com for any issues integrating or using this plugin.

### Requirements:
 - Android [GCM Setup](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup)
 - iOS [APNS Setup](http://docs.urbanairship.com/reference/push-providers/apns.html)

### Quickstart

1. Install this plugin using PhoneGap/Cordova CLI:

        cordova plugin add urbanairship-cordova

2. Modify the config.xml file to set the Android minimum sdk version to 16:

        <platform name="android">
            <preference name="android-minSdkVersion" value="16" />
        </platform> 

3. Modify the config.xml file to contain (replacing with your configuration settings):

        <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="Your Production App Key" />
        <preference name="com.urbanairship.production_app_secret" value="Your Production App Secret" />
        <preference name="com.urbanairship.development_app_key" value="Your Development App Key" />
        <preference name="com.urbanairship.development_app_secret" value="Your Development App Secret" />

        <!-- Required for Android. -->
        <preference name="com.urbanairship.gcm_sender" value="Your GCM Sender ID" />

        <!-- If the app is in production or not -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Optional config values -->

        <!-- Enable push when the application launches -->
        <preference name="com.urbanairship.enable_push_onlaunch" value="true | false" />
        
        <!-- Enable Analytics when the application launches -->
        <!-- Warning: Features that depend on analytics being enabled may not work properly if analytics is disabled (reports, location segmentation, region triggers, push to local time). -->
        <preference name="com.urbanairship.enable_analytics" value="true | false" />

        <!-- Override the Android notification icon -->
        <preference name="com.urbanairship.notification_icon" value="ic_notification" />

        <!-- Override the Android notification large icon -->
        <preference name="com.urbanairship.notification_large_icon" value="ic_notification_large" />
    
        <!-- Override the Android notification sound (sound file should be in res/raw)-->
        <preference name="com.urbanairship.notification_sound" value="push" />

        <!-- Specify the notification accent color for Android API 21+ (Lollipop) -->
        <preference name="com.urbanairship.notification_accent_color" value="#0000ff" />

        <!-- Clear the iOS badge on launch -->
        <preference name="com.urbanairship.clear_badge_onlaunch" value="true | false" />

        <!-- Enables/disables auto launching the message center when the corresponding push is opened. -->
        <preference name="com.urbanairship.auto_launch_message_center" value="true | false" />

        <!-- iOS 10 alert foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_alert" value="true"/>

        <!-- iOS 10 badge foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_badge" value="true"/>

        <!-- iOS 10 sound foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_sound" value="true"/>


4. Enable user notifications
```
    // Enable user notifications (will prompt the user to accept push notifications)
    UAirship.setUserNotificationsEnabled(true, function (enabled) {
        console.log("User notifications are enabled! Fire away!")
    })
```

#### iOS
Enable Push Notifications in the project editor's Capabilities pane:

![Alt text](phonegap-enable-push.png?raw=true "Enable Push Notifications")

### Sample

A sample can be found in Example. To run it, copy the files:
- Example/index.html to www/index.html
- Example/css/* to www/css
- Example/js/* to www/js

Add the device plugin: `cordova plugin add cordova-plugin-device`
