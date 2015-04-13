# Urban Airship PhoneGap/Cordova Plugin

### Platform Support

This plugin supports PhoneGap/Cordova apps running on both iOS and Android.

### Version Requirements

This plugin is meant to work with Cordova 3.4.0+ and and the latest version of the Urban Airship library.
More documentation and integration guides for iOS and Android are available on our
[website](https://docs.urbanairship.com/display/DOCS/Client%3A+PhoneGap). 

### Older PhoneGap versions

An older unsupported version of the plugin for Phonegap 3.0 can be found [here](https://github.com/urbanairship/phonegap-ua-push/tree/2.3.3)
and for Phonegap 2.6 - 2.9 can be found [here](https://github.com/urbanairship/phonegap-ua-push/tree/1.0.8).

### Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
Code Contribution Agreement (http://urbanairship.com/legal/contribution-agreement/).

## Migration

A migration guide for newer releases of the plugin can be found [here](MIGRATION.md).

## Installation

#### Automatic Installation using PhoneGap/Cordova CLI (iOS and Android)
1. For iOS, make sure you update your iOS project to Cordova iOS version 3.4.1 before installing this plugin.

        cordova platform update ios

2. For Android, make sure the Android SDK is up to date. Android Support v4 library revision 22+ and Google Play Service GCM 7.0+ are required. Google Play Service Location 7.0+ is required if using location services.

3. Install this plugin using PhoneGap/Cordova cli:

        phonegap plugin add https://github.com/urbanairship/phonegap-ua-push.git

4. Modify the config.xml file to contain (replacing with your configuration settings):

        <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="PRODUCTION_APP_KEY" />
        <preference name="com.urbanairship.production_app_secret" value="PRODUCTION_APP_SECRET" />
        <preference name="com.urbanairship.development_app_key" value="DEVELOPMENT_APP_KEY" />
        <preference name="com.urbanairship.development_app_secret" value="DEVELOPMENT_APP_SECRET" />

        <!-- If the app is in production or not -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Enable push when the application launches (instead of waiting for enablePush js call).  Defaults to false -->
        <preference name="com.urbanairship.enable_push_onlaunch" value="true | false" />

        <!-- Only required for Android. -->
        <preference name="com.urbanairship.gcm_sender" value="GCM_SENDER_ID" />


5. If your app supports Android API < 14, then you have to manually instrument any Android Activities to have proper analytics.
See [Instrumenting Android Analytics](http://docs.urbanairship.com/build/android_features.html#setting-up-analytics-minor-assembly-required). 

#### iOS manual installation (unnecessary if installed automatically)
1. Add src/ios/UAirshipPlugin to your project
1. Copy src/ios/Airship to your project's directory
1. Add Airship as a Header search path (Project -> Build Settings -> Header Search Path)
1. Add Airship/libUAirship-*.a as a library (Target -> Build Phases -> Link Binary With Libraries)
1. Make sure the following frameworks are linked (Target -> Build Phases -> Link Binary With Libraries):


        CFNetwork.framework
        CoreGraphics.framework
        Foundation.framework
        MobileCoreServices.framework
        Security.framework
        SystemConfiguration.framework
        UIKit.framework
        libz.dylib
        libsqlite3.dylib
        CoreTelephony.framework
        CoreLocation.framework
        AudioToolbox.framework
        StoreKit.framework

1. Modify the cordova plugin.xml file to include the UAirshipPlugin and preferences:


        <feature name="UAirship">
            <param name="ios-package" value="UAirshipPlugin" />
            <param name="onload" value="true" />
        </feature>
        
         <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="PRODUCTION_APP_KEY" />
        <preference name="com.urbanairship.production_app_secret" value="PRODUCTION_APP_SECRET" />
        <preference name="com.urbanairship.development_app_key" value="DEVELOPMENT_APP_KEY" />
        <preference name="com.urbanairship.development_app_secret" value="DEVELOPMENT_APP_SECRET" />

        <!-- If the app is in production or not -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Enable push when the application launches (instead of waiting for enablePush js call).  Defaults to false -->
        <preference name="com.urbanairship.enable_push_onlaunch" value="true | false" />

1. Copy www/UrbanAirship.js into the project's www directory

1. Require the UrbanAirship module `var UrbanAirship = cordova.require('<Path to UrbanAirship.js>')`

#### Android manual installation (unnecessary if installed automatically)
1. Copy src/Android/*.java files to your project's src/com/urbanairship/cordova/ directory
1. Install [Urban Airship Android SDK](http://docs.urbanairship.com/build/push/android.html#urban-airship-sdk-setup)
1. Install [Google Play Services](http://docs.urbanairship.com/build/push/android.html#urban-airship-sdk-setup) (version 7.0 or greater)
1. Install [Android v4 support library](http://developer.android.com/tools/support-library/setup.html) (revision 22 or greater)

1. Modify the AndroidManifest.xml to include these permissions:


        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.VIBRATE" />
        <uses-permission android:name="android.permission.GET_ACCOUNTS" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />
        <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
        <!-- MODIFICATION REQUIRED, replace $PACKAGE_NAME with your app's package name -->
        <uses-permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" />
    
        <!-- MODIFICATION REQUIRED, replace $PACKAGE_NAME with your app's package name -->
        <permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" android:protectionLevel="signature" />

        <!-- MODIFICATION REQUIRED, replace $PACKAGE_NAME with your app's package name -->
        <permission android:name="$PACKAGE_NAME.permission.UA_DATA" android:protectionLevel="signature" />
        <uses-permission android:name="$PACKAGE_NAME.permission.UA_DATA" />

1. Modify the AndroidManifest.xml Application section to include:

         <receiver android:name="com.urbanairship.cordova.IntentReceiver"
                        android:exported="false">

                <intent-filter>
                    <action android:name="com.urbanairship.push.CHANNEL_UPDATED" />
                    <action android:name="com.urbanairship.push.OPENED" />
                    <action android:name="com.urbanairship.push.DISMISSED" />
                    <action android:name="com.urbanairship.push.RECEIVED" />

                    <!-- MODIFICATION REQUIRED - Use your package name as the category -->
                    <category android:name="$PACKAGE_NAME" />
                </intent-filter>
         </receiver>

         <receiver android:name="com.urbanairship.CoreReceiver"
                  android:exported="false">

            <intent-filter android:priority="-999">
                <action android:name="com.urbanairship.push.OPENED" />

                <!-- MODIFICATION REQUIRED - Use your package name as the category -->
                <category android:name="$PACKAGE_NAME" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.urbanairship.push.GCMPushReceiver" android:permission="com.google.android.c2dm.permission.SEND">        
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
                <!-- MODIFICATION REQUIRED, replace $PACKAGE_NAME with your app's package name -->
                <category android:name="$PACKAGE_NAME" /> 
            </intent-filter>
        </receiver>
        
        <meta-data android:name="com.urbanairship.autopilot" android:value="com.urbanairship.cordova.CordovaAutopilot" />
        
        <service android:name="com.urbanairship.push.PushService" android:label="Push Notification Service"/>
        <service android:name="com.urbanairship.analytics.EventService" android:label="Event Service"/>
        <service android:name="com.urbanairship.richpush.RichPushUpdateService"/>
        <service android:name="com.urbanairship.actions.ActionService"/>
        <service android:name="com.urbanairship.location.LocationService" android:label="Segments Service"/>

        <provider android:name="com.urbanairship.UrbanAirshipProvider"
            <!-- MODIFICATION REQUIRED, replace $PACKAGE_NAME with your app's package name -->
            android:authorities="$PACKAGE_NAME.urbanairship.provider" 
            android:exported="false"
            android:multiprocess="true" />
        
        <activity android:name="com.urbanairship.CoreActivity"/>
        <activity android:name="com.urbanairship.actions.ActionActivity"/>

        <activity android:name="com.urbanairship.google.PlayServicesErrorActivity"
            android:theme="@android:style/Theme.Translucent.NoTitleBar"/>

        <activity
            android:name="com.urbanairship.actions.LandingPageActivity"
            android:exported="false">

            <intent-filter>
                <action android:name="com.urbanairship.actions.SHOW_LANDING_PAGE_INTENT_ACTION"/>
                <data android:scheme="http" />
                <data android:scheme="https" />
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </activity>


1. Modify the cordova plugin.xml file to include the UAirshipPlugin:

        <feature name="UAirship">
            <param name="android-package" value="com.urbanairship.cordova.UAirshipPlugin" />
            <param name="onload" value="true" />
        </feature>

         <!-- Urban Airship app credentials -->
        <preference name="com.urbanairship.production_app_key" value="PRODUCTION_APP_KEY" />
        <preference name="com.urbanairship.production_app_secret" value="PRODUCTION_APP_SECRET" />
        <preference name="com.urbanairship.development_app_key" value="DEVELOPMENT_APP_KEY" />
        <preference name="com.urbanairship.development_app_secret" value="DEVELOPMENT_APP_SECRET" />

        <!-- If the app is in production or not -->
        <preference name="com.urbanairship.in_production" value="true | false" />

        <!-- Enable push when the application launches (instead of waiting for enablePush js call).  Defaults to false -->
        <preference name="com.urbanairship.enable_push_onlaunch" value="true | false" />

        <!-- Only required for Android. -->
        <preference name="com.urbanairship.gcm_sender" value="GCM_SENDER_ID" />

1. If your app supports Android API < 14 (pre ICS), then need to manually instrument any Android Activities
to get proper analytics.  
See [Instrumenting Android Analytics](http://docs.urbanairship.com/build/android_features.html#setting-up-analytics-minor-assembly-required).

1. Copy www/UrbanAirship.js into the project's www directory

1. Require the UrbanAirship module `var UrbanAirship = cordova.require('<Path to UrbanAirship.js>')`

## Example
A full example can be found in Example. To run it, copy the files:
- Example/index.html to www/index.html
- Example/css/* to www/css
- Example/js/* to www/js

Add the device plugin: `cordova plugin add org.apache.cordova.device`

#### Basic Example
    
    // Register for any Urban Airship events
    document.addEventListener("urbanairship.registration", function (event) {
        if (event.error) {
            console.log('There was an error registering for push notifications');
        } else {
            console.log("Registered with ID: " + event.channelID);
        } 
    }, false)

    document.addEventListener("urbanairship.push", function (event) {
        console.log("Incoming push: " + event.message)
    }, false)

    // Set tags on a device, that you can push to
    // https://docs.urbanairship.com/display/DOCS/Server%3A+Tag+API
    UAirship.setTags(["loves_cats", "shops_for_games"], function () {
        UAirship.getTags(function (obj) {
            obj.tags.forEach(function (tag) {
                console.log("Tag: " + tag);
            });
        });
    });

    // Set an alias, this lets you tie a device to a user in your system
    // https://docs.urbanairship.com/display/DOCS/Server%3A+iOS+Push+API#ServeriOSPushAPI-Alias
    UAirship.setAlias("awesomeuser22", function () {
        UAirship.getAlias(function (alias) {
            console.log("The user formerly known as " + alias)
        });
    });

    // Check if user notifications are enabled
    UAirship.isUserNotificationsEnabled(function (enabled) {
        if (enabled) {
            console.log("User notifications are enabled! Fire away!");
        }
    })

## Data objects

The Urban Airship javascript API provides standard instances for some of our data. This allows us to clearly explain what kind of data we're working with when we pass it around throughout the API.

#### Push

    Push = {
        message: "Your team just scored!",
        extras: {
            "url": "/game/5555"
        }
    }

A push is an object that contains the data associated with a Push. The extras dictionary can contain arbitrary key and value data, that you can use inside your application.

#### Quiet Time

    // Quiet time set to 10PM - 6AM
    QuietTime = {
        startHour: 22,
        startMinute: 0,
        endHour: 6,
        endMinute: 0
    }

## API

**All methods without a return value return undefined**

### Top-level calls

#### setUserNotificationsEnabled()

Enables or disables user notifications on the device. This sends a registration to the back end server.

#### setLocationEnabled()

Enables or disables Urban Airship location services on the device.

#### setBackgroundLocationEnabled()

Enables or disables background location on the device.

#### setNotificationTypes(bitmask)
**Note::** iOS Only

On iOS, registration for push requires specifying what combination of badges, sound and
alerts are desired. This function must be explicitly called in order to begin the
registration process. For example:

    UAirship.setNotificationTypes(UAirship.notificationType.sound | UAirship.notificationType.alert)

*Available notification types:*

* notificationType.sound
* notificationType.alert
* notificationType.badge

### Status Functions

*Callback arguments:* (Boolean status)

All status callbacks are passed a boolean indicating the result of the request:

    UAirship.isUserNotificationsEnabled(function(isEnabled) {
        if (isEnabled) {
            $('#pushEnabled').prop("checked", true)
        }
    })

#### isUserNotificationsEnabled(callback)

*Callback arguments* (Boolean enabled)

Indicates whether user notifications are enabled.

#### isSoundEnabled(callback)
**Note:** Android Only

*Callback arguments:* (Boolean enabled)

Indicates whether sound is enabled.

#### isVibrateEnabled(callback)
**Note:** Android Only

*Callback arguments:* (Boolean enabled)

Indicates whether vibration is enabled.

#### isQuietTimeEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether Quiet Time is enabled.

#### isAnalyticsEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether analytics is enabled.

#### isLocationEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether location is enabled.

#### isBackgroundLocationEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether background location updates are enabled.

#### isInQuietTime(callback)

*Callback arguments:* (Boolean inQuietTime)

Indicates whether Quiet Time is currently in effect.

### Getters

#### getLaunchNotification(callback)

*Callback arguments:* (Boolean clear)

Returns the last notification that launched the application and takes a boolean to clear the notification or not.

#### getChannelID(callback)

*Callback arguments:* (String ID)

Get the push identifier for the device. The channel ID is used to send messages to the device for testing, and is the canonical identifier for the device in Urban Airship.

**Note:** iOS will always have a push identifier. Android will always have one once the application has had a successful registration.

#### getQuietTime(callback)

*Callback arguments:* (QuietTime currentQuietTime)

Get the current quiet time.

#### getTags(callback)

*Callback arguments:* (Array currentTags)

Get the current tags.

#### getAlias(callback)

*Callback arguments:* (String currentAlias)

Get the alias.

#### getNamedUser(callback)

*Callback arguments:* (String namedUser)

Get the named user ID.

#### getBadgeNumber(callback)
**Note:** iOS only

*Callback arguments:* (Int badgeNumber)

Get the current application badge number.

### Setters

#### setTags(Array tags, callback)

Set tags for the device.

#### setAlias(String alias, callback)

Set alias for the device.

#### setNamedUser(String namedUser, callback)

Set the named user ID for the device.

#### setSoundEnabled(Boolean enabled, callback)
**Note:** Android Only, iOS sound settings come in the push.

Set whether the device makes sound on push.

#### setVibrateEnabled(Boolean enabled, callback)
**Note:** Android Only

Set whether the device vibrates on push.

#### setQuietTimeEnabled(Boolean enabled, callback)

Set whether quiet time is on.

#### setQuietTime(QuietTime newQuietTime, callback)

Set the quiet time for the device.

#### setAnalyticsEnabled(Boolean enabled, callback)

Enables or disables analytics. Disabling analytics will delete any locally stored events and prevent any events from uploading. Features that depend on analytics being enabled may not work properly if it's disabled (reports, region triggers, location segmentation, push to local time).

#### setAutobadgeEnabled(Boolean enabled, callback)
**Note:** iOS only

Set whether the UA Auto badge feature is enabled.

#### setBadgeNumber(Int badge, callback)
**Note:** iOS only

Set the current application badge number.

#### resetBadge(callback)
**Note:** iOS only

Reset the badge number to zero.

#### clearNotifications(callback)
**Note:** Android only

Clears the notifications posted by the application.

### Location

#### recordCurrentLocation(callback)

Report the location of the device.

### Events

**Note:** If your application supports Android and it listens to any of the events, you should 
start listening for events on both 'deviceReady' and 'resume' and stop listening for events on 'pause'.  
This will prevent the events from being handled in the background.

### Incoming Push

Event:

    {
        message: <Alert Message>,
        extras: <Extras Dictionary>
    }

This event is triggered when a push notification is received.

    document.addEventListener('urbanairship.push', function(event) {
        alert(event.message);
    });


### Registration

Event:

    {
        error: <Error message when registration failed>,
        channelID: <Push address>
    }

This event is triggered when your application receives a registration response from Urban Airship.

    document.addEventListener('urbanairship.registration', function(event) {
        if (event.error) {
            console.log('There was an error registering for push notifications.');
        } else {
            console.log("Registered with ID: " + event.channelID);
        } 
    });
    
