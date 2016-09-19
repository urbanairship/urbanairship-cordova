# Migration Guide

## 5.x to 6.0.0

### Android Minimum SDK Version

Urban Airship Android 8.0.0 SDK requires the minimum sdk version to be 16.
Modify the config.xml file to contain:

    <platform name="android">
        <preference name="android-minSdkVersion" value="16" />
    </platform>

### Location Changes

The method `recordCurrentLocation` has been removed.

## 4.x to 5.0.0


All functions now take an optional failure callback.


### Run Action

The method ``runAction`` now returns the result's value in the success callback
and the result's error in the failure callback.

4.x example:

    UAirship.runAction("some_action", "some value", function (result) {
      if (result.error) {
         console.log("action failed": + result.error);
      } else {
         console.log("action finished": + result.value);
      }
    });

5.x example:

    UAirship.runAction("some_action", "some value", function (value) {
        console.log("action finished": + value);
    }, function (error) {
         console.log("action failed": + error);
    });


## 3.6.0 to 4.0.0

### Installation changes:

Now requires Cordova 5.4.0+, Cordova Android 4.1.0, and Cordova iOS 3.9.0.
The iOS Airship library is bitcode enabled for Xcode 7+.

## 2.8.x to 3.0.0

### Plugin Access:

The plugin is now attached to the window as `UAirship` instead of `PushNotification`.

### Installation changes

Plugin ID has been changed from `com.urbanairship.phonegap.PushNotification` to `com.urbanairship.cordova`. The old version
may need to be uninstalled manually before updating to the new version.

### Push Changes

To enable or disable push, use `setUserNotificationsEnabled` instead of `enablePush` or `disablePush`. The app will
continue to not prompt for push until user notifications is enabled. `registerNotificationTypes` has been renamed to
`setNotificationTypes`. Its only required to be called if the app only wants to register for specific types of notifications.
If it is not set, the app will register for all types - alert, sound, and vibrate.

The method `getPushID` has been replaced with `getChannelID`. It now returns the channel ID for both Android and iOS.

To access the launch notification, call `getLaunchNotification` instead of `getIncoming`. `getLaunchNotification` no longer
clears the notification on first access, instead it takes a flag as the first parameter to clear the notification or not.

Example:

	UAirship.getLaunchNotification(true, callback)


Fetching tags no longer returns an object with a tag array, instead it now returns just the tags.

Example:

	UAirship.getTags(function(tags) {
		tags.forEach(function(tag) {
			console.log("tag: " + tag);
		})
	})


### Location Changes:

To enable or disable location, use `setLocationEnabled` instead of `enableLocation` or `disableLocation`. Similarly with
background location, use `setBackgroundLocationEnabled`.


## 2.5.x to 2.6.0

### Android Installation changes:

Android plugin now requires Google Play Services and the Android Support v4 library. If the plugin was manually installed,
remove the old urbanairship.jar file and follow the manual setup instructions again in the README.

The custom_rules.xml file in the root of the android project needs to be deleted due to bug https://code.google.com/p/android/issues/detail?id=23271 and https://issues.apache.org/jira/browse/CB-7675.

## 2.3.x to 2.4.0

### Installation changes:

For iOS, make sure you update your iOS project to Cordova iOS version 3.4.1 before installing the phonegap-ua-push plugin.
For example:
```
1. cordova platform update ios
iOS project is now at version 3.4.1
2. phonegap local plugin add https://github.com/urbanairship/phonegap-ua-push.git
```

## 2.2.x to 2.3.0

### Config Changes:

In 2.2.x, push was enabled by default and would prompt the user to allow push before the application
was ready to prompt the user for push.  In 2.3.0, push is now disabled by default and will be enabled
when enablePush is called in javascript.  This allows the application to determine when to
prompt the user for push.

To keep the old 2.2.x behavior, you can set the config option 'com.urbanairship.enable_push_onlaunch'
to 'true' in the config.xml.


## 2.1.x to 2.2.0

### Installation changes:

The PushNotification.js no longer sets the PushNotification plugin at window.plugins.PushNotification 
and instead uses module.exports.  Instead of including the PushNotification.js script, at the top of the 
javascript file that the plugin is being used, include the following:

	var PushNotification = require('<Path to PushNotification.js>')

**Note:** If you are using automatic integration, the required step is done automatically.


### Event changes

Registration events and incoming push events are now standard dom events.

PushNotification.registerEvents is now removed.  The "registration" and "push" events
can now be accessed by listening for events on the document:

Events:

  	Name: 'urbanairship.registration'

	Event object: 
	{
	  error: <Error message if registration failed>,
	  pushID: <Push address>
	}
	
	Name: 'urbanairship.push'
	
	Event object: 
	{
	  message: <Alert message>,
	  extras: <Alert extras>
	}

Example:

	document.addEventListener("urbanairship.registration", function (event) {
		if (event.error) {
			console.log('there was an error registering for push notifications');
		} else {
			console.log("Registered with ID: " + event.pushID);
		} 
	}, false)
	
	document.addEventListener("urbanairship.push", function (event) {
		console.log("Incoming push: " + event.message)
	}, false)


**Note:** If your application supports Android and it listens to any of the events, you should start 
listening for events on both 'deviceReady' and 'resume' and stop listening for events on 'pause'. 
This will prevent the events from being handled in the background.
