# Migration Guide

## 2.1.x to 2.2.0

### Installation changes:

The PushNotificaiton.js no longer sets the PushNotification plugin at window.plugins.PushNotification 
and instead uses module.exports.  Instead of including the PushNotification.js script, at the top of the 
javascript file that the plugin is being used, include the following:

	var PushNotification = require('<Path to PushNotification.js>')

**Note:** If you are using automatic integration, the require step is done automatically.


### Event changes

Registration events and incoming push events are now standard dom events.

  // Removed event registrations:
  PushNotification.registerEvent('registration', function (error, id) {});
  PushNotification.registerEvent('push', function (push) {});

  // Not the plugin will fire document events:

  Events: 
  	'urbanairship.registration'

  	Event object: 
  	{
  		error: <Error message if registration failed>,
  		pushID: <Push address>
  	}

	'urbanairship.push'

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




