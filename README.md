# Urban Airship PhoneGap/Cordova Plugin

## Platform Support

This plugin supports PhoneGap/Cordova apps running on both iOS and Android.

## Version Requirements

This repo is meant to work with PhoneGap 2.6.0+ and the latest version of the Urban Airship library.
Please upgrade your PhoneGap application to use 2.6 if you wish to use this library. More documentation and
integration guides for IOS and Android are availble on our
[website](https://docs.urbanairship.com/display/DOCS/Client%3A+PhoneGap).

## Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
Code Contribution Agreement (http://urbanairship.com/legal/contribution-agreement/).

# Javascript API

## Basic Example

For a more complete example, check out the sample app [index.html](https://github.com/urbanairship/phonegap-ua-push/blob/master/ios-sample/www/index.html#L18-227)

    push = window.pushNotification;

    // Callback for when a device has registered with Urban Airship.
    // https://docs.urbanairship.com/display/DOCS/Server%3A+Android+Push+API#ServerAndroidPushAPI-Registration
    push.registerEvent('registration', function (error, id) {
        if (error) {
            console.log('there was an error registering for push notifications');
        } else {
            console.log("Registered with ID: " + id);
        } 
    });

    // Callback for when the app is running, and receives a push.
    push.registerEvent('push', function (push) {
        console.log("Got push: " + push.message)
    });

    // Set tags on a device, that you can push to
    // https://docs.urbanairship.com/display/DOCS/Server%3A+Tag+API
    push.setTags(["loves_cats", "shops_for_games"], function () {
        push.getTags(function (obj) {
            obj.tags.forEach(function (tag) {
                console.log("Tag: " + tag);
            });
        });
    });

    // Set an alias, this lets you tie a device to a user in your system
    // https://docs.urbanairship.com/display/DOCS/Server%3A+iOS+Push+API#ServeriOSPushAPI-Alias
    push.setAlias("awesomeuser22", function () {
        push.getAlias(function (alias) {
            console.log("The user formerly known as " + alias)
        });
    });

    // Check if push is enabled
    push.isPushEnabled(function (enabled) {
        if (enabled) {
            console.log("Push is enabled! Fire away!");
        }
    })

This module is for using Urban Airship within a javascript environment.

It follows an async callback-based approach to Javascript libraries. Anyone familiar with this style of programming should be immediately able to use the library to full effect.

# Data objects

The Urban Airship javascript API provides standard instances for some of our data. This allows us to clearly explain what kind of data we're working with when we pass it around throughout the API.

## Push

    Push = {
        message: "Your team just scored!",
        extras: {
            "url": "/game/5555"
        }
    }

## Quiet Time

    // Quiet time set to 10PM - 6AM
    QuietTime = {
        startHour: 22,
        startMinute: 0,
        endHour: 6,
        endMinute: 0
    }

A push is an object that contains the data associated with a Push. The extras dictionary can contain arbitrary key and value data, that you can use inside your application.

# API

**All methods without a return value return undefined**

## Top-level calls

### enablePush()

Enable push on the device. This sends a registration to the backend server.

### disablePush()

Disable push on the device. You will no longer be able to recieve push notifications.

### enableLocation()

Enable location updates on the device.

### disableLocation()

Disable location updates on the device.

### enableBackgroundLocation()

Enable background location updates on the device.

### disableBackgroundLocation()

Disable background location updates on the device.

### registerForNotificationTypes(bitmask)
**Note::** iOS Only

On iOS, registration for push requires specifying what combination of badges, sound and
alerts are desired.  This function must be explicitly called in order to begin the
registration process.  For example:

    push.registerForNotificationTypes(push.notificationType.sound | push.notificationType.alert)

*Available notification types:*

* notificationType.sound
* notificationType.alert
* notificationType.badge

## Status Functions

*Callback arguments:* (Boolean status)

All status callbacks are passed a boolean indicating the result of the request:

    push.isPushEnabled(function (has_push) {
        if (has_push) {
            $('#pushEnabled').prop("checked", true)
        }
    })

### isPushEnabled(callback)

*Callback arguments* (Boolean enabled)

Indicates whether push is enabled.

### isSoundEnabled(callback)
**Note:** Android Only

*Callback arguments:* (Boolean enabled)

Indicates whether sound is enabled.

### isVibrateEnabled(callback)
**Note:** Android Only

*Callback arguments:* (Boolean enabled)

Indicates whether vibration is enabled.

### isQuietTimeEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether Quiet Time is enabled.

### isLocationEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether location is enabled.

### isBackgroundLocationEnabled(callback)

*Callback arguments:* (Boolean enabled)

Indicates whether background location updates are enabled.

### isInQuietTime(callback)

*Callback arguments:* (Boolean inQuietTime)

Indicates whether Quiet Time is currently in effect.

## Getters

### getIncoming(callback)

*Callback arguments:* (Push incomingPush)

Get information about the push that caused the application to be launched. When a user clicks on a push to launch your app, this functions callback will be passed a Push object consisting of the alert message, and an object containing extra key/value pairs.  Otherwise the incoming message and extras will be an empty string and an empty object, respectively.

    push.getIncoming(function (incoming) {
        if (incoming.message) {
            alert("Incoming push message: " + incoming.message;
        }

        if (incoming.extras.url) {
            showPage(incoming.extras.url);
        }
    })

### getPushID(callback)

*Callback arguments:* (String id)

Get the push identifier for the device. The push ID is used to send messages to the device for testing, and is the canoncial identifer for the device in Urban Airship.

**Note:** iOS will always have a push identifier. Android will always have one once the application has had a successful registration.

### getQuietTime(callback)

*Callback arguments:* (QuietTime currentQuietTime)

Get the current quiet time.

### getTags(callback)

*Callback arguments:* (Array currentTags)

Get the current tags.

### getAlias(callback)

*Callback arguments:* (String currentAlias)

Get the current tags.

## Setters

### setTags(Array tags, callback)

Set tags for the device.

### setAlias(String alias, callback)

Set alias for the device.

### setSoundEnabled(Boolean enabled, callback)
**Note:** Android Only, iOS sound settings come in the push

Set whether the device makes sound on push.

### setVibrateEnabled(Boolean enabled, callback)
**Note:** Android Only

Set whether the device vibrates on push.

### setQuietTimeEnabled(Boolean enabled, callback)

Set whether quiet time is on.

### setQuietTime(QuietTime newQuietTime, callback)

Set the quiet time for the device.

### setAutobadgeEnabled(Boolean enabled, callback)
**Note:** iOS only

Set whether the UA Autobadge feature is enabled.

### setBadgeNumber(Int badge, callback)
**Note:** iOS only

Set the current application badge number

### resetBadge(callback)
**Note:** iOS only

Reset the badge number to zero

## Location

### recordCurrentLocation(callback)

Report the location of the device.

# Events

## Incoming Push

*Callback arguments:* (Push push)

This event is trigerred when your application is open, and a push comes in.

    push.registerEvent('push', function (push) {
        alert(push.message);
    });


## Registration

*Callback arguments:* (Boolean error, String id)

This event is trigerred when your application recieves a registration response from Urban Airship.

    push.registerEvent('registration', function (error, id) {
        if (error) {
            console.log('There was an error registering for push notifications.');
        } else {
            console.log("Registered with ID: " + id);
        } 
    });
	
