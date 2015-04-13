/*
 Copyright 2009-2015 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

  /**
   * Enables or disables user notifications.
   *
   * @param {Boolean} enabled true to enable notifications, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setUserNotificationsEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setUserNotificationsEnabled', arguments)
    callNative(callback, "setUserNotificationsEnabled", [!!enabled])
  },

  /**
   * Checks if user notifications are enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isUserNotificationsEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isUserNotificationsEnabled', arguments)
    callNative(callback, "isUserNotificationsEnabled")
  },

  /**
   * Returns the channel ID.
   *
   * @param {Function} callback The function to call on completion.
   */
  getChannelID: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getChannelID', arguments)
    callNative(callback, "getChannelID")
  },

  /**
   * Returns the last notification that launched the application.
   *
   * @param {Boolean} clear true to clear the notification.
   * @param {Function} callback The function to call on completion.
   */
  getLaunchNotification: function(clear, callback) {
    argscheck.checkArgs('*f', 'UAirship.getLaunchNotification', arguments)
    callNative(callback, "getLaunchNotification", [!!clear])
  },

  /**
   * Returns the tags as an array.
   *
   * @param {Function} callback The function to call on completion.
   */
  getTags: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getTags', arguments);
    callNative(callback, "getTags")
  },

  /**
   * Sets the tags.
   *
   * @param {Array} tags an array of strings.
   * @param {Function} callback The function to call on completion.
   */
  setTags: function(tags, callback) {
    argscheck.checkArgs('aF', 'UAirship.setTags', arguments);
    callNative(callback, "setTags", [tags])
  },

  /**
   * Returns the alias.
   *
   * @param {Function} callback The function to call on completion.
   */
  getAlias: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getAlias', arguments)
    callNative(callback, "getAlias")
  },

  /**
   * Sets the alias.
   *
   * @param {String} alias string
   * @param {Function} callback The function to call on completion.
   */
  setAlias: function(alias, callback) {
    argscheck.checkArgs('sF', 'UAirship.setAlias', arguments)
    callNative(callback, "setAlias", [alias])
  },

  /**
   * Checks if quiet time is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isQuietTimeEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isQuietTimeEnabled', arguments)
    callNative(callback, "isQuietTimeEnabled")
  },

  /**
   * Enables or disables quiet time.
   *
   * @param {Boolean} enabled true to enable quiet time, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setQuietTimeEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setQuietTimeEnabled', arguments)
    callNative(callback, "setQuietTimeEnabled", [!!enabled])
  },

  /**
   * Checks if the device is currently in quiet time.
   *
   * @param {Function} callback The function to call on completion.
   */
  isInQuietTime: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isInQuietTime', arguments)
    callNative(callback, "isInQuietTime")
  },

  /**
   * Returns the quiet time as an object with the following:
   * "startHour": Number,
   * "startMinute": Number,
   * "endHour": Number,
   * "endMinute": Number
   *
   * @param {Function} callback The function to call on completion.
   */
  getQuietTime: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getQuietTime', arguments)
    callNative(callback, "getQuietTime")
  },

  /**
   * Sets the quiet time.
   *
   * @param {Number} startHour for quiet time.
   * @param {Number} startMinute for quiet time.
   * @param {Number} endHour for quiet time.
   * @param {Number} endMinute for quiet time.
   * @param {Function} callback The function to call on completion.
   */
  setQuietTime: function(startHour, startMinute, endHour, endMinute, callback) {
    argscheck.checkArgs('nnnnF', 'UAirship.setQuietTime', arguments)
    callNative(callback, "setQuietTime", [startHour, startMinute, endHour, endMinute])
  },

  /**
   * Enables or disables analytics.
   *
   * Disabling analytics will delete any locally stored events
   * and prevent any events from uploading. Features that depend on analytics being
   * enabled may not work properly if it's disabled (reports, region triggers,
   * location segmentation, push to local time).
   *
   * @param {Boolean} enabled true to enable analytics, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setAnalyticsEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setAnalyticsEnabled', arguments)
    callNative(callback, "setAnalyticsEnabled", [!!enabled])
  },

  /**
   * Checks if analytics is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isAnalyticsEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isAnalyticsEnabled', arguments)
    callNative(callback, "isAnalyticsEnabled")
  },

  /**
   * Returns the named user ID.
   *
   * @param {Function} callback The function to call on completion.
   */
  getNamedUser: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getNamedUser', arguments)
    callNative(callback, "getNamedUser")
  },

  /**
   * Sets the named user ID.
   *
   * @param {String} namedUser identifier string.
   * @param {Function} callback The function to call on completion.
   */
  setNamedUser: function(namedUser, callback) {
    argscheck.checkArgs('sF', 'UAirship.setNamedUser', arguments)
    callNative(callback, "setNamedUser", [namedUser])
  },

  /**
   * Runs an Urban Airship action. An object will be returned with
   * the following:
   * "error": String
   * "value": *
   *
   * @param {String} actionName action as a string.
   * @param {*} actionValue
   * @param {Function} callback The function to call on completion.
   */
  runAction: function(actionName, actionValue, callback) {
    argscheck.checkArgs('s*F', 'UAirship.runAction', arguments)
    callNative(callback, "runAction", [actionName, actionValue])
  },

  // Location

  /**
   * Enables or disables Urban Airship location services.
   *
   * @param {Boolean} enabled true to enable location, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setLocationEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setLocationEnabled', arguments)
    callNative(callback, "setLocationEnabled", [!!enabled])
  },

  /**
   * Checks if location is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isLocationEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isLocationEnabled', arguments)
    callNative(callback, "isLocationEnabled")
  },

  /**
   * Enables or disables background location.
   *
   * @param {Boolean} enabled true to enable background location, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setBackgroundLocationEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setBackgroundLocationEnabled', arguments)
    callNative(callback, "setBackgroundLocationEnabled", [!!enabled])
  },

  /**
   * Checks if background location is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isBackgroundLocationEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isBackgroundLocationEnabled', arguments)
    callNative(callback, "isBackgroundLocationEnabled")
  },

  /**
   * Records the current location.
   *
   * @param {Function} callback The function to call on completion.
   */
  recordCurrentLocation: function(callback) {
    argscheck.checkArgs('F', 'UAirship.recordCurrentLocation', arguments)
    callNative(callback, "recordCurrentLocation")
  },

  // iOS only

  /**
   * Enables or disables auto badge. Defaults to `NO`.
   *
   * @param {Boolean} enabled true to enable auto badge, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setAutobadgeEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setAutobadgeEnabled', arguments)
    callNative(callback, "setAutobadgeEnabled", [!!enabled])
  },

  /**
   * Sets the badge number.
   *
   * @param {Number} number specified badge to set.
   * @param {Function} callback The function to call on completion.
   */
  setBadgeNumber: function(number, callback) {
    argscheck.checkArgs('nF', 'UAirship.setBadgeNumber', arguments)
    callNative(callback, "setBadgeNumber", [number])
  },

  /**
   * Returns the current badge number.
   *
   * @param {Function} callback The function to call on completion.
   */
  getBadgeNumber: function(callback) {
    argscheck.checkArgs('f', 'UAirship.getBadgeNumber', arguments)
    callNative(callback, "getBadgeNumber", [number])
  },

  /**
   * Clears the badge.
   *
   * @param {Function} callback The function to call on completion.
   */
  resetBadge: function(callback) {
    argscheck.checkArgs('F', 'UAirship.resetBadge', arguments)
    callNative(callback, "resetBadge")
  },

  /**
   * Sets the iOS notification types. Specify the combination of
   * badges, sound and alerts are desired.
   *
   * @param {Number} types specified notification types.
   * @param {Function} callback The function to call on completion.
   */
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

  /**
   * Clears all notifications posted by the application.
   *
   * @param {Function} callback The function to call on completion.
   */
  clearNotifications: function(callback) {
    argscheck.checkArgs('F', 'UAirship.clearNotifications', arguments)
    callNative(callback, "clearNotifications")
  },

  /**
   * Checks if notification sound is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isSoundEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isSoundEnabled', arguments)
    callNative(callback, "isSoundEnabled")
  },

  /**
   * Enables or disables notification sound.
   *
   * @param {Boolean} enabled true to enable sound, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setSoundEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setSoundEnabled', arguments)
    callNative(callback, "setSoundEnabled", [!!enabled])
  },

  /**
   * Checks if notification vibration is enabled or not.
   *
   * @param {Function} callback The function to call on completion.
   */
  isVibrateEnabled: function(callback) {
    argscheck.checkArgs('f', 'UAirship.isVibrateEnabled', arguments)
    callNative(callback, "isVibrateEnabled")
  },

  /**
   * Enables or disables notification vibration.
   *
   * @param {Boolean} enabled true to enable vibration, false to disable.
   * @param {Function} callback The function to call on completion.
   */
  setVibrateEnabled: function(enabled, callback) {
    argscheck.checkArgs('*F', 'UAirship.setVibrateEnabled', arguments)
    callNative(callback, "setVibrateEnabled", [!!enabled])
  }
}

module.exports = plugin
