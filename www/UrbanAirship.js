
var cardova = require("cordova"),
    exec = require("cordova/exec"),
    argscheck = require('cordova/argscheck')


// Argcheck values:
// * : allow anything,
// f : function
// a : array
// d : date
// n : number
// s : string
// o : object
// lowercase = required, uppercase = optional

// Helper method to call into the native plugin
function callNative(callback, name, args) {
  args = args || []

  var failure = function(e) {
      console.log("Javascript Callback Error: " + e)
  }

  exec(callback, failure, "UAirship", name, args)
}

// Listen for channel registration updates
callNative(function(registration) {
  console.log("Firing document event for registration update.")
  cordova.fireDocumentEvent("urbanairship.registration", registration)
}, "registerChannelListener")

// Listen for incoming push notifications
callNative(function(push) {
  console.log("Firing document event for push event.")
  cordova.fireDocumentEvent("urbanairship.push", push)
}, "registerPushListener")


var plugin = {

  setUserNotificationsEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setUserNotificationsEnabled', arguments)
    callNative(callback, "setUserNotificationsEnabled", [!!enabled])
  },

  isUserNotificationsEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isUserNotificationsEnabled', arguments)
    callNative(callback, "isUserNotificationsEnabled")
  },

  getChannelID: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getChannelID', arguments)
    callNative(callback, "getChannelID")
  },

  getLaunchNotification: function(clear, callback) {
    argscheck.checkArgs('*f', 'UAirship.getLaunchNotification', arguments)
    callNative(callback, "getLaunchNotification", [!!clear])
  },

  getTags: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getTags', arguments);
    callNative(callback, "getTags")
  },

  setTags: function(tags, callback) {
    argscheck.checkArgs('aF', 'UAirship.setTags', arguments);
    callNative(callback, "setTags", [tags])
  },

  getAlias: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getAlias', arguments)
    callNative(callback, "getAlias")
  },

  setAlias: function(alias, callback) {
    argscheck.checkArgs('sF', 'UAirship.setAlias', arguments)
    callNative(callback, "setAlias", [alias])
  },

  isQuietTimeEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isQuietTimeEnabled', arguments)
    callNative(callback, "isQuietTimeEnabled")
  },

  setQuietTimeEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setQuietTimeEnabled', arguments)
    callNative(callback, "setQuietTimeEnabled", [!!enabled])
  },

  isInQuietTime: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isInQuietTime', arguments)
    callNative(callback, "isInQuietTime")
  },

  getQuietTime: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getQuietTime', arguments)
    callNative(callback, "getQuietTime")
  },

  setQuietTime: function(startHour, startMinute, endHour, endMinute, callback) {
    argscheck.checkArgs('nnnnF', 'UAirship.setQuietTime', arguments)
    callNative(callback, "setQuietTime", [startHour, startMinute, endHour, endMinute])
  },

  setAnalyticsEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setAnalyticsEnabled', arguments)
    callNative(callback, "setAnalyticsEnabled", [!!enabled])
  },

  isAnalyticsEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isAnalyticsEnabled', arguments)
    callNative(callback, "isAnalyticsEnabled")
  },

  getNamedUser: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getNamedUser', arguments)
    callNative(callback, "getNamedUser")
  },

  setNamedUser: function(namedUser, callback) {
    argscheck.checkArgs('sF', 'UAirship.setNamedUser', arguments)
    callNative(callback, "setNamedUser", [namedUser])
  },

  runAction: function(actionName, actionValue, callback) {
    argscheck.checkArgs('s*F', 'UAirship.runAction', arguments)
    callNative(callback, "runAction", [actionName, actionValue])
  },

  // Location

  setLocationEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setLocationEnabled', arguments)
    callNative(callback, "setLocationEnabled", [!!enabled])
  },

  isLocationEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isLocationEnabled', arguments)
    callNative(callback, "isLocationEnabled")
  },

  setBackgroundLocationEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setBackgroundLocationEnabled', arguments)
    callNative(callback, "setBackgroundLocationEnabled", [!!enabled])
  },

  isBackgroundLocationEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isBackgroundLocationEnabled', arguments)
    callNative(callback, "isBackgroundLocationEnabled")
  },

  recordCurrentLocation: function(callback) {
    argscheck.checkArgs('F', 'UAirship.recordCurrentLocation', arguments)
    callNative(callback, "recordCurrentLocation")
  },

  // iOS only

  setAutobadgeEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setAutobadgeEnabled', arguments)
    callNative(callback, "setAutobadgeEnabled", [!!enabled])
  },

  setBadgeNumber: function(number, callback) {
    argscheck.checkArgs('nF', 'UAirship.setBadgeNumber', arguments)
    callNative(callback, "setBadgeNumber", [number])
  },

  getBadgeNumber: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getBadgeNumber', arguments)
    callNative(callback, "getBadgeNumber", [number])
  },

  resetBadge: function(callback) {
    argscheck.checkArgs('F', 'UAirship.resetBadge', arguments)
    callNative(callback, "resetBadge")
  },

  setNotificationTypes: function(types, callback) {
    argscheck.checkArgs('nF', 'UAirship.setNotificationTypes', arguments)
    callNative(callback, "setNotificationTypes", [types])
  },

  notificationType: {
    none: 0,
    badge: 1,
    sound: 2,
    alert: 4
  },


  // Android only

  clearNotifications: function(callback) {
    argscheck.checkArgs('F', 'UAirship.clearNotifications', arguments)
    callNative(callback, "clearNotifications")
  },

  isSoundEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isSoundEnabled', arguments)
    callNative(callback, "isSoundEnabled")
  },

  setSoundEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setSoundEnabled', arguments)
    callNative(callback, "setSoundEnabled", [!!enabled])
  },

  isVibrateEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isVibrateEnabled', arguments)
    callNative(callback, "isVibrateEnabled")
  },

  setVibrateEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setVibrateEnabled', arguments)
    callNative(callback, "setVibrateEnabled", [!!enabled])
  }
}

module.exports = plugin
