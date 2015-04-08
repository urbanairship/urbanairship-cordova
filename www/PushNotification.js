
var cardova = require("cordova"),
    exec = require("cordova/exec")

// Helper method to call into the native plugin
function callNative(callback, name, args) {
  args = args || []

  var failure = function(e) {
      console.log("Javascript Callback Error: " + e)
  }

  exec(callback, failure, "PushNotificationPlugin", name, args)
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

  enablePush: function(callback) {
    callNative(callback, "enablePush");
  },

  disablePush: function(callback) {
    callNative(callback, "disablePush")
  },

  isPushEnabled: function(callback) {
    callNative(callback, "isPushEnabled")
  },

  getChannelID: function(callback) {
    callNative(callback, "getChannelID")
  },

  getIncoming: function(callback) {
    callNative(callback, "getIncoming")
  },

  getTags: function(callback) {
    callNative(callback, "getTags")
  },

  setTags: function(tags, callback) {
    callNative(callback, "setTags", [tags])
  },

  getAlias: function(callback) {
    callNative(callback, "getAlias")
  },

  setAlias: function(alias, callback) {
    callNative(callback, "setAlias", [alias])
  },

  isQuietTimeEnabled: function(callback) {
    callNative(callback, "isQuietTimeEnabled")
  },

  setQuietTimeEnabled: function(bool, callback) {
    callNative(callback, "setQuietTimeEnabled", [bool])
  },

  isInQuietTime: function(callback) {
    callNative(callback, "isInQuietTime")
  },

  getQuietTime: function(callback) {
    callNative(callback, "getQuietTime")
  },

  setQuietTime: function(startHour, startMinute, endHour, endMinute, callback) {
    callNative(callback, "setQuietTime", [startHour, startMinute, endHour, endMinute])
  },

  setAnalyticsEnabled: function(enabled, callback) {
    callNative(callback, "setAnalyticsEnabled", [enabled])
  },

  isAnalyticsEnabled: function(callback) {
    callNative(callback, "isAnalyticsEnabled")
  },

  getNamedUser: function(callback) {
    callNative(callback, "getNamedUser")
  },

  setNamedUser: function(namedUser, callback) {
    callNative(callback, "setNamedUser", [namedUser])
  },

  // Location

  enableLocation: function(callback) {
    callNative(callback, "enableLocation")
  },

  disableLocation: function(callback) {
    callNative(callback, "disableLocation")
  },

  isLocationEnabled: function(callback) {
    callNative(callback, "isLocationEnabled")
  },

  enableBackgroundLocation: function(callback) {
    callNative(callback, "enableBackgroundLocation")
  },

  disableBackgroundLocation: function(callback) {
    callNative(callback, "disableBackgroundLocation")
  },

  isBackgroundLocationEnabled: function(callback) {
    callNative(callback, "isBackgroundLocationEnabled")
  },

  recordCurrentLocation: function(callback) {
    callNative(callback, "recordCurrentLocation")
  },

  // iOS only

  setAutobadgeEnabled: function(enabled, callback) {
    callNative(callback, "setAutobadgeEnabled", [enabled])
  },

  setBadgeNumber: function(number, callback) {
    callNative(callback, "setBadgeNumber", [number])
  },

  getBadgeNumber: function(callback) {
    callNative(callback, "getBadgeNumber", [number])
  },

  resetBadge: function(callback) {
    callNative(callback, "resetBadge")
  },

  registerForNotificationTypes: function(types, callback) {
    callNative(callback, "registerForNotificationTypes", [types])
  },

  notificationType: {
    none: 0,
    badge: 1,
    sound: 2,
    alert: 4
  },


  // Android only

  clearNotifications: function(callback) {
    callNative(callback, "clearNotifications")
  },

  isSoundEnabled: function(callback) {
    callNative(callback, "isSoundEnabled")
  },

  isVibrateEnabled: function(callback) {
    callNative(callback, "isVibrateEnabled")
  },

  setSoundEnabled: function(bool, callback) {
    callNative(callback, "setSoundEnabled", [bool])
  },

  setVibrateEnabled: function(bool, callback) {
    callNative(callback, "setVibrateEnabled", [bool])
  }
}

module.exports = plugin
