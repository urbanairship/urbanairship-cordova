// Init the plugin
var PushNotification = function () {

}

// Types

PushNotification.prototype.notificationType = {
  none: 0,
  badge: 1,
  sound: 2,
  alert: 4
}

// Helpers

PushNotification.prototype.failure = function (msg) {
  console.log("Javascript Callback Error: " + msg)
}


PushNotification.prototype.call_native = function (callback, name, args) {
  if(arguments.length == 2) {
    args = []
  }
  ret = cordova.exec(
  callback, // called when signature capture is successful
  this.failure, // called when signature capture encounters an error
  'PushNotificationPlugin', // Tell cordova that we want to run "PushNotificationPlugin"
  name, // Tell the plugin the action we want to perform
  args); // List of arguments to the plugin
  return ret;
}

PushNotification.prototype.isPlatformIOS = function () {
  return device.platform == "iPhone" || device.platform == "iPad" || device.platform == "iPod touch" || device.platform == "iOS"
}

// Core API

// Registration

PushNotification.prototype.registerForNotificationTypes = function (types, callback) {
  if(device.platform == "iPhone" || device.platform == "iPad" || device.platform == "iPod touch" || device.platform == "iOS") {
    this.call_native(callback, "registerForNotificationTypes", [types])
  }
}

// Top level enabling/disabling

PushNotification.prototype.enablePush = function (callback) {
  this.call_native(callback, "enablePush");
}

PushNotification.prototype.disablePush = function (callback) {
  this.call_native(callback, "disablePush");
}

PushNotification.prototype.enableLocation = function (callback) {
  this.call_native(callback, "enableLocation")
}

PushNotification.prototype.disableLocation = function (callback) {
  this.call_native(callback, "disableLocation")
}

PushNotification.prototype.enableBackgroundLocation = function (callback) {
  this.call_native(callback, "enableBackgroundLocation")
}

PushNotification.prototype.disableBackgroundLocation = function (callback) {
  this.call_native(callback, "disableBackgroundLocation")
}

// is* functions

PushNotification.prototype.isPushEnabled = function (callback) {
  this.call_native(callback, "isPushEnabled");
}

PushNotification.prototype.isSoundEnabled = function (callback) {
  if(device.platform == "Android") {
    this.call_native(callback, "isSoundEnabled");
  }
}

PushNotification.prototype.isVibrateEnabled = function (callback) {
  if(device.platform == "Android") {
    this.call_native(callback, "isVibrateEnabled");
  }
}

PushNotification.prototype.isQuietTimeEnabled = function (callback) {
  this.call_native(callback, "isQuietTimeEnabled");
}

PushNotification.prototype.isInQuietTime = function (callback) {
  this.call_native(callback, "isInQuietTime");
}

PushNotification.prototype.isLocationEnabled = function (callback) {
  this.call_native(callback, "isLocationEnabled");
}

PushNotification.prototype.isBackgroundLocationEnabled = function (callback) {
  this.call_native(callback, "isBackgroundLocationEnabled");
}

// Getters

PushNotification.prototype.getIncoming = function (callback) {
  this.call_native(callback, "getIncoming");
}

PushNotification.prototype.getPushID = function (callback) {
  this.call_native(callback, "getPushID")
}

PushNotification.prototype.getQuietTime = function (callback) {
  this.call_native(callback, "getQuietTime");
}

PushNotification.prototype.getTags = function (callback) {
  this.call_native(callback, "getTags");
}

PushNotification.prototype.getAlias = function (callback) {
  this.call_native(callback, "getAlias");
}

// Setters

PushNotification.prototype.setAlias = function (alias, callback) {
  this.call_native(callback, "setAlias", [alias])
}

PushNotification.prototype.setTags = function (tags, callback) {
  this.call_native(callback, "setTags", [tags])
}

PushNotification.prototype.setSoundEnabled = function (bool, callback) {
  if(device.platform == "Android") {
    this.call_native(callback, "setSoundEnabled", [bool])
  }
}

PushNotification.prototype.setVibrateEnabled = function (bool, callback) {
  if(device.platform == "Android") {
    this.call_native(callback, "setVibrateEnabled", [bool])
  }
}

PushNotification.prototype.setQuietTimeEnabled = function (bool, callback) {
  this.call_native(callback, "setQuietTimeEnabled", [bool])
}

PushNotification.prototype.setQuietTime = function (startHour, startMinute, endHour, endMinute, callback) {
  this.call_native(callback, "setQuietTime", [startHour, startMinute, endHour, endMinute])
}

PushNotification.prototype.setAutobadgeEnabled = function (enabled, callback) {
  if (this.isPlatformIOS()) {
    this.call_native(callback, "setAutobadgeEnabled", [enabled]);
  }
}

PushNotification.prototype.setBadgeNumber = function (number, callback) {
  if (this.isPlatformIOS()) {
    this.call_native(callback, "setBadgeNumber", [number]);
  }
}

// Reset Badge

PushNotification.prototype.resetBadge = function (callback) {
  if (this.isPlatformIOS()) {
    this.call_native(callback, "resetBadge");
  }
}

// Clear notifications
PushNotification.prototype.clearNotifications = function (callback) {
  if (!this.isPlatformIOS()) {
    this.call_native(callback, "clearNotifications");
  }
}
// Location stuff

PushNotification.prototype.recordCurrentLocation = function (callback) {
  this.call_native(callback, "recordCurrentLocation");
}

module.exports = new PushNotification();
