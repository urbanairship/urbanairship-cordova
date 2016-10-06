# Urban Airship Cordova Plugin

### Resources:
 - [Getting started guide](http://docs.urbanairship.com/platform/phonegap.html)
 - [Github repo](https://github.com/urbanairship/phonegap-ua-push)

### Installation

1. Install this plugin using PhoneGap/Cordova CLI:

        cordova plugin add urbanairship-cordova

2. Modify the config.xml file to contain (replacing with your configuration settings):

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

        <!-- iOS 10 alert foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_alert" value="true"/>

        <!-- iOS 10 badge foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_badge" value="true"/>

        <!-- iOS 10 sound foreground notification presentation option -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_sound" value="true"/>
