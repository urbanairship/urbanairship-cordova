# Urban Airship Cordova Plugin

This plugin supports Cordova apps running on both iOS and Android.

### Resources:
 - [Getting started guide](http://docs.urbanairship.com/platform/cordova/)
 - [JSDocs](http://docs.urbanairship.com/reference/libraries/urbanairship-cordova/latest/)
 - [Migration docs](MIGRATION.md)

### Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit our
[Contributor License Agreement](https://docs.google.com/forms/d/e/1FAIpQLScErfiz-fXSPpVZ9r8Di2Tr2xDFxt5MgzUel0__9vqUgvko7Q/viewform).

One of our engineers will verify receipt of the agreement before approving your pull request.

### Issues

Please visit http://support.urbanairship.com/ for any issues integrating or using this plugin.

### Requirements:
 - Cordova-CLI >= 7.0.0

#### iOS:
- Xcode 10+
- [APNS Setup](http://docs.urbanairship.com/reference/push-providers/apns.html)

#### Android
 - Android [FCM Setup](https://docs.urbanairship.com/tutorials/getting-started/push-providers/fcm/

### Quickstart

1. Install this plugin using Cordova CLI:

        cordova plugin add urbanairship-cordova

2. *(Android Only)* Add a reference to your google-services.json file in the app's `config.xml`:
```
       <platform name="android">
            ...
            <resource-file src="google-services.json" target="app/google-services.json" />
       </platform>
```

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

4. Initialize Urban Airship

    Either call takeOff when the device is ready:

        // TakeOff
        UAirship.takeOff({
          production: {
            appKey: "<APP_KEY>",
            appSecret: "<APP_SECRET>"
          },
          development: {
            appKey: "<APP_KEY>",
            appSecret: "<APP_SECRET>"
          }
        })

        // Configure Android
        UAirship.setAndroidNotificationConfig({
          icon: "ic_notification",
          largeIcon: "ic_notification_large",
          accentColor: "#FF0000"
        })

        // Configure iOS
        UAirship.setPresentationOptions(
          UAirship.presentationOptions.sound | UAirship.presentationOptions.alert
        )


    Alternatively you can configure Urban Airship through config.xml and it will takeOff automatically.

        <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="Your Production App Key" />
        <preference name="com.urbanairship.production_app_secret" value="Your Production App Secret" />
        <preference name="com.urbanairship.development_app_key" value="Your Development App Key" />
        <preference name="com.urbanairship.development_app_secret" value="Your Development App Secret" />

        <!-- Optional -->
        <!-- If the app is in production or not. If not set, Urban Airship will auto detect the mode. -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Urban Airship development log level defaults to debug -->
        <preference name="com.urbanairship.development_log_level" value="none | error | warn | info | debug | verbose" />

        <!-- Urban Airship production log level defaults to error -->
        <preference name="com.urbanairship.production_log_level" value="none | error | warn | info | debug | verbose" />

        <!-- Enables/disables auto launching the message center when the corresponding push is opened. -->
        <preference name="com.urbanairship.auto_launch_message_center" value="true | false" />

        <!-- Android Notification Settings -->
        <preference name="com.urbanairship.notification_icon" value="ic_notification" />
        <preference name="com.urbanairship.notification_large_icon" value="ic_notification_large" />
        <preference name="com.urbanairship.notification_accent_color" value="#0000ff" />

        <!-- iOS Foreround Presentation Options -->
        <preference name="com.urbanairship.ios_foreground_notification_presentation_alert" value="true | false"/>
        <preference name="com.urbanairship.ios_foreground_notification_presentation_badge" value="true | false"/>
        <preference name="com.urbanairship.ios_foreground_notification_presentation_sound" value="true | false"/>

        <!-- iOS Auto Clear Badge -->
        <preference name="com.urbanairship.clear_badge_onlaunch" value="true | false" />

    `UrbanAirship.takeOff` can be called multiple times but any changes to the app credentials will not apply until the next app start.

5. Enable user notifications:

        // Enable user notifications (will prompt the user to accept push notifications on iOS)
        UAirship.setUserNotificationsEnabled(true, function (enabled) {
            console.log("User notifications are enabled! Fire away!")
        })

6. *(Optional)* Listen for events:

        document.addEventListener("urbanairship.registration", onRegistration)
        document.addEventListener("urbanairship.push", onPushReceived)
        document.addEventListener("urbanairship.notification_opened", notificationOpened)
        document.addEventListener("urbanairship.deep_link", handleDeepLink)

### iOS Notification Service Extension

In order to take advantage of iOS 10 notification attachments, such as images,
animated gifs, and video, you will need to create a [notification service extension](https://developer.apple.com/reference/usernotifications/unnotificationserviceextension/)
by following the [iOS Notification Service Extension Guide](https://docs.urbanairship.com/tutorials/api/ios/notification-service-extension/).

### Android GoogleServicesPlugin

The plugin will automatically apply the `GoogleServicesPlugin` for FCM. This can cause conflicts with other plugins that also apply
the `GoogleServicesPlugin`. Applications can disable applying the plugin by setting the gradle property `uaSkipApplyGoogleServicesPlugin`
to `true`. See (Setting Gradle Properties)[https://cordova.apache.org/docs/en/latest/guide/platforms/android/#setting-gradle-properties]
for details on how to set a gradle property in a Cordova project.


### Sample

A sample can be found in the Example directory.

1. Add your UA credentials to the `config_sample.xml` file in the root directory and save.
2. Add your development team id to the `build_sample.json` file in the root directory and save.
3. Run the script with the command `./scripts/create_sample.sh PROJECT_PATH PROJECT_NAME`
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
