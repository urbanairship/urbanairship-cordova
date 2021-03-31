# Urban Airship Accengage Cordova Plugin

This plugin supports Cordova apps running on both iOS and Android.

### Issues

Please visit http://support.urbanairship.com/ for any issues integrating or using this plugin.

### Requirements:
 - cordova >= 9.0.1
 - cordova-ios >= 5.0.1
 - cococapods >= 1.7.3
 - urbanairship-cordova >= 10.1.0

#### iOS:
- Xcode 11+

### Setup instructions

Remove the old Accengage cordova plugin

        cordova plugin rm com.bma4s.sdk.plugins.cordova

Install the new plugin using Cordova CLI:

        cordova plugin add urbanairship-accengage-cordova

**Android only**: Add a reference to your google-services.json file in the app's `config.xml`

    <platform name="android">
        ...
        <resource-file src="google-services.json" target="app/google-services.json" />
    </platform>

**iOS only**: Add your Apple Developer Account Team ID to the [build.json](https://cordova.apache.org/docs/en/latest/guide/platforms/ios/#using-buildjson).

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

Your iOS builds will need to reference the build.json using Cordova's `--buildConfig` flag.

### Initialize Airship

This can be accomplished by either calling `takeOff` when the device is ready, or automatically by providing config in the `config.xml` file.

Example calling takeOff when the device is ready:

    // TakeOff
    UAirship.takeOff({
      production: {
        appKey: "<APP_KEY>",
        appSecret: "<APP_SECRET>"
      },
      development: {
        appKey: "<APP_KEY>",
        appSecret: "<APP_SECRET>"
      },
      site: "EU" // Optional. Add if app uses Airship's EU cloud site.
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
    
Example config to allow automatic takeOff:

    <!-- Airship app credentials -->
    <preference name="com.urbanairship.production_app_key" value="Your Production App Key" />
    <preference name="com.urbanairship.production_app_secret" value="Your Production App Secret" />
    <preference name="com.urbanairship.development_app_key" value="Your Development App Key" />
    <preference name="com.urbanairship.development_app_secret" value="Your Development App Secret" />

    <!-- Optional -->
    <!-- Use EU cloud site -->
    <preference name="com.urbanairship.site" value="EU" />

    <!-- If the app is in production or not. If not set, Airship will auto detect the mode. -->
    <preference name="com.urbanairship.in_production" value="true | false" />

    <!-- Airship development log level defaults to debug -->
    <preference name="com.urbanairship.development_log_level" value="none | error | warn | info | debug | verbose" />

    <!-- Airship production log level defaults to error -->
    <preference name="com.urbanairship.production_log_level" value="none | error | warn | info | debug | verbose" />

    <!-- Enables/disables auto launching the message center when the corresponding push is opened. -->
    <preference name="com.urbanairship.auto_launch_message_center" value="true | false" />

    <!-- Android Notification Settings -->
    <preference name="com.urbanairship.notification_icon" value="ic_notification" />
    <preference name="com.urbanairship.notification_large_icon" value="ic_notification_large" />
    <preference name="com.urbanairship.notification_accent_color" value="#0000ff" />

    <!-- iOS Foreground Presentation Options -->
    <preference name="com.urbanairship.ios_foreground_notification_presentation_alert" value="true | false"/>
    <preference name="com.urbanairship.ios_foreground_notification_presentation_badge" value="true | false"/>
    <preference name="com.urbanairship.ios_foreground_notification_presentation_sound" value="true | false"/>

    <!-- iOS Auto Clear Badge -->
    <preference name="com.urbanairship.clear_badge_onlaunch" value="true | false" />

### Notification Service Extension

To take advantage of notification attachments, such as images, animated gifs, and video, you will need to create a [notification service extension](https://developer.apple.com/documentation/usernotifications/modifying_content_in_newly_delivered_notifications).

Follow the steps in the [iOS Notification Service Extension Guide](https://docs.airship.com/platform/ios/getting-started/#notification-service-extension).

### Enable user notifications

Push notifications can enabled as soon as the application is ready by the following method:

    UAirship.setUserNotificationsEnabled(true)
    
### More features

See our [Cordova documentation](https://docs.airship.com/platform/cordova/getting-started) for more information.
