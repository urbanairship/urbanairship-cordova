# Urban Airship Cordova Plugin

### Resources:
 - [Getting started guide](http://docs.urbanairship.com/platform/cordova.html)
 - [Github repo](https://github.com/urbanairship/urbanairship-cordova)

### Installation

1. Install this plugin using Cordova CLI:

        cordova plugin add urbanairship-cordova


2. Modify the config.xml file to contain (replacing with your configuration settings):

        <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="Your Production App Key" />
        <preference name="com.urbanairship.production_app_secret" value="Your Production App Secret" />
        <preference name="com.urbanairship.development_app_key" value="Your Development App Key" />
        <preference name="com.urbanairship.development_app_secret" value="Your Development App Secret" />

        <!-- Required for Android. Make sure to prefix the sender ID with sender:-->
        <preference name="com.urbanairship.gcm_sender" value="sender:Your GCM Sender ID" />

        <!-- If the app is in production or not -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Optional config values -->

        <!-- Enable push when the application launches -->
        <preference name="com.urbanairship.enable_push_onlaunch" value="true | false" />

        <!-- Enable Analytics when the application launches -->
        <!-- Warning: Features that depend on analytics being enabled may not work properly if analytics is disabled (reports, location segmentation, region triggers, push to local time). -->
        <preference name="com.urbanairship.enable_analytics" value="true | false" />

        <!-- Urban Airship development log level defaults to debug -->
        <preference name="com.urbanairship.development_log_level" value="none | error | warn | info | debug | verbose" />

        <!-- Urban Airship production log level defaults to error -->
        <preference name="com.urbanairship.production_log_level" value="none | error | warn | info | debug | verbose" />

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
        <preference name="com.urbanairship.ios_foreground_notification_presentation_alert" value="true | false"/>

        <!-- iOS 10 badge foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_badge" value="true | false"/>

        <!-- iOS 10 sound foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_sound" value="true | false"/>

3. *(iOS Only)* Add your Apple Developer Account Team ID to the [build.json](https://cordova.apache.org/docs/en/latest/guide/platforms/ios/#using-buildjson):

        {
            "ios": {
                "debug": {
                    "developmentTeam": "XXXXXXXXXX"
                },
                "release": {
                    "developmentTeam": "XXXXXXXXXX"
                }
            }
        }
    Your iOS builds will need to reference the build.json using Cordova's "--buildConfig" flag.

4. Enable user notifications:

        // Enable user notifications (will prompt the user to accept push notifications)
        UAirship.setUserNotificationsEnabled(true, function (enabled) {
            console.log("User notifications are enabled! Fire away!")
        })
