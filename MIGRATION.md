# Migration Guide

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


