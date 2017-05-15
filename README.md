# Urban Airship PhoneGap/Cordova Plugin

This plugin supports PhoneGap/Cordova apps running on both iOS and Android.

### Resources:
 - [Getting started guide](http://docs.urbanairship.com/platform/cordova/)
 - [JSDocs](http://docs.urbanairship.com/reference/libraries/urbanairship-cordova/latest/)
 - [Migration docs](MIGRATION.md)

### Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
[Code Contribution Agreement](http://docs.urbanairship.com/contribution-agreement.html).

### Issues

Please visit http://support.urbanairship.com/ for any issues integrating or using this plugin.

### Requirements:
 - Cordova-CLI >= 6.4.0
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

4. *(iOS Only)* Create platform/ios/build.json or modify your own build.json to add your Apple Developer Account Team ID.

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

5. Enable user notifications

        // Enable user notifications (will prompt the user to accept push notifications)
        UAirship.setUserNotificationsEnabled(true, function (enabled) {
            console.log("User notifications are enabled! Fire away!")
        })

### Sample

A sample can be found in the Example directory. 

1. Add your UA credentials to the `config_sample.xml` file in the root directory and save.
2. Add your development team id to the `build_sample.json` file in the root directory and save.
3. Run the script with the command `./create_sample.sh PROJECT_PATH PROJECT_NAME`
4. cd to the newly-created project directory, e.g. sample/test
5. Build the platform you want to test.
   * iOS
      1. Build with command `cordova build ios --emulator`
      2. After successful build, connect an iOS device to test
      3. Run on device with command `cordova run ios --device --developmentTeam=XXXXXXXXXX` 
         * Please refer to "[Signing an App](https://cordova.apache.org/docs/en/latest/guide/platforms/ios/#signing-an-app)" for more information about code signing.
   * Android
      1. Build with command `cordova build android` in test directory
      2. After successful build, connect an android device to test
      3. Test with command `cordova run android`
 
