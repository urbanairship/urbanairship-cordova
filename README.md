# Urban Airship PhoneGap/Cordova Plugin

This plugin supports PhoneGap/Cordova apps running on both iOS and Android. Full documentation is
available [here](http://docs.urbanairship.com/platform/phonegap.html).

Requirements:
 - Cordova 5.4.0+, Cordova Android 4.1.0+, and Cordova iOS 3.9.0+
 - [GCM Setup](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup) or [APNS Setup](http://docs.urbanairship.com/reference/push-providers/apns.html)

## Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
[Code Contribution Agreement](http://docs.urbanairship.com/contribution-agreement.html).

## Issues

Please contact support@urbanairship.com for any issues integrating or using this plugin.

## Migration

A migration guide for newer releases of the plugin can be found [here](MIGRATION.md).

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

        <!-- Specify the notification accent color for Android API 21+ (Lollipop) -->
        <preference name="com.urbanairship.notification_accent_color" value="#0000ff" />

        <!-- Clear the iOS badge on launch -->
        <preference name="com.urbanairship.clear_badge_onlaunch" value="true | false" />


3. If your app supports Android API < 14, then you have to manually instrument any Android Activities to have proper analytics.
See [Gingerbread Support](http://docs.urbanairship.com/platform/android.html#gingerbread-support).

### Basic Example
    
    // Register for any Urban Airship events
    document.addEventListener("urbanairship.registration", function (event) {
        if (event.error) {
            console.log('There was an error registering for push notifications')
        } else {
            console.log("Registered with ID: " + event.channelID)
        } 
    })

    document.addEventListener("urbanairship.push", function (event) {
        console.log("Incoming push: " + event.message)
    })

    // Set tags on a device, that you can push to
    UAirship.setTags(["loves_cats", "shops_for_games"], function () {
        UAirship.getTags(function (tags) {
            tags.forEach(function (tag) {
                console.log("Tag: " + tag)
            })
        })
    })

    // Set an alias, this lets you tie a device to a user in your system
    UAirship.setAlias("awesomeuser22", function () {
        UAirship.getAlias(function (alias) {
            console.log("The user formerly known as " + alias)
        })
    })

    // Enable user notifications (will prompt the user to accept push notifications)
    UAirship.setUserNotificationsEnabled(true, function (enabled) {
        console.log("User notifications are enabled! Fire away!")
    })

## Sample

A sample can be found in Example. To run it, copy the files:
- Example/index.html to www/index.html
- Example/css/* to www/css
- Example/js/* to www/js

Add the device plugin: `cordova plugin add cordova-plugin-device`


