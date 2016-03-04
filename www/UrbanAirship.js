/*
 Copyright 2009-2016 Urban Airship Inc. All rights reserved.

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
function callNative(success, failure, name, args) {
  args = args || []
  exec(success, failure, "UAirship", name, args)
}

// Helper method to edit tag groups
function tagGroupEditor(cordovaMethod, nativeMethod) {
    // Store the raw operations and let the SDK combine them
    var operations = []
    var editor = {}

    editor.addTags = function(tagGroup, tags) {
        argscheck.checkArgs('sa', cordovaMethod + ".addTags", arguments)
        var operation = { "operation": "add", "group": tagGroup, "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.removeTags = function(tagGroup, tags) {
        argscheck.checkArgs('sa', cordovaMethod + ".removeTags", arguments)
        var operation = { "operation": "remove", "group": tagGroup, "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.apply = function(success, failure) {
        argscheck.checkArgs('FF', cordovaMethod + ".apply", arguments)
        callNative(success, failure, nativeMethod, [operations])
        operations = []
        return editor
    }

    return editor
}

// Listen for channel registration updates
callNative(function(registration) {
  console.log("Firing document event for registration update.")
  cordova.fireDocumentEvent("urbanairship.registration", registration)
}, null, "registerChannelListener")

// Listen for incoming push notifications
callNative(function(push) {
  console.log("Firing document event for push event.")
  cordova.fireDocumentEvent("urbanairship.push", push)
}, null, "registerPushListener")

// Listen for inbox updates
callNative(function() {
  console.log("Firing document event for inbox update.")
  cordova.fireDocumentEvent("urbanairship.inbox_updated")
}, null, "registerInboxListener")


var plugin = {

  /**
   * Enables or disables user notifications.
   *
   * @param {Boolean} enabled true to enable notifications, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setUserNotificationsEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setUserNotificationsEnabled', arguments)
    callNative(success, failure, "setUserNotificationsEnabled", [!!enabled])
  },

  /**
   * Checks if user notifications are enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isUserNotificationsEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isUserNotificationsEnabled', arguments)
    callNative(success, failure, "isUserNotificationsEnabled")
  },

  /**
   * Returns the channel ID.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getChannelID: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getChannelID', arguments)
    callNative(success, failure, "getChannelID")
  },

  /**
   * Returns the last notification that launched the application.
   *
   * @param {Boolean} clear true to clear the notification.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getLaunchNotification: function(clear, success, failure) {
    argscheck.checkArgs('*fF', 'UAirship.getLaunchNotification', arguments)
    callNative(success, failure, "getLaunchNotification", [!!clear])
  },

  /**
   * Returns the tags as an array.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getTags: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getTags', arguments);
    callNative(success, failure, "getTags")
  },

  /**
   * Sets the tags.
   *
   * @param {Array} tags an array of strings.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setTags: function(tags, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.setTags', arguments);
    callNative(success, failure, "setTags", [tags])
  },

  /**
   * Returns the alias.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getAlias: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getAlias', arguments)
    callNative(success, failure, "getAlias")
  },

  /**
   * Sets the alias.
   *
   * @param {String} alias string
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setAlias: function(alias, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.setAlias', arguments)
    callNative(success, failure, "setAlias", [alias])
  },

  /**
   * Checks if quiet time is enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isQuietTimeEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isQuietTimeEnabled', arguments)
    callNative(success, failure, "isQuietTimeEnabled")
  },

  /**
   * Enables or disables quiet time.
   *
   * @param {Boolean} enabled true to enable quiet time, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setQuietTimeEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setQuietTimeEnabled', arguments)
    callNative(success, failure, "setQuietTimeEnabled", [!!enabled])
  },

  /**
   * Checks if the device is currently in quiet time.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isInQuietTime: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isInQuietTime', arguments)
    callNative(success, failure, "isInQuietTime")
  },

  /**
   * Returns the quiet time as an object with the following:
   * "startHour": Number,
   * "startMinute": Number,
   * "endHour": Number,
   * "endMinute": Number
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getQuietTime: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getQuietTime', arguments)
    callNative(success, failure, "getQuietTime")
  },

  /**
   * Sets the quiet time.
   *
   * @param {Number} startHour for quiet time.
   * @param {Number} startMinute for quiet time.
   * @param {Number} endHour for quiet time.
   * @param {Number} endMinute for quiet time.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setQuietTime: function(startHour, startMinute, endHour, endMinute, success, failure) {
    argscheck.checkArgs('nnnnFF', 'UAirship.setQuietTime', arguments)
    callNative(success, failure, "setQuietTime", [startHour, startMinute, endHour, endMinute])
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
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setAnalyticsEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setAnalyticsEnabled', arguments)
    callNative(success, failure, "setAnalyticsEnabled", [!!enabled])
  },

  /**
   * Checks if analytics is enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isAnalyticsEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isAnalyticsEnabled', arguments)
    callNative(success, failure, "isAnalyticsEnabled")
  },

  /**
   * Returns the named user ID.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getNamedUser: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getNamedUser', arguments)
    callNative(success, failure, "getNamedUser")
  },

  /**
   * Sets the named user ID.
   *
   * @param {String} namedUser identifier string.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setNamedUser: function(namedUser, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.setNamedUser', arguments)
    callNative(success, failure, "setNamedUser", [namedUser])
  },

  /**
   * Runs an Urban Airship action.
   *
   * @param {String} actionName action as a string.
   * @param {*} actionValue
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  runAction: function(actionName, actionValue, success, failure) {
    argscheck.checkArgs('s*FF', 'UAirship.runAction', arguments)

    var successWrapper = function(result) {
      if (success) {
        success(result.value)
      }
    }

    callNative(successWrapper, failure, "runAction", [actionName, actionValue])
  },

  /**
   * Creates an editor to modify the named user tag groups.
   *
   * @return A tag group editor.
   */
  editNamedUserTagGroups: function() {
    return tagGroupEditor('UAirship.editNamedUserTagGroups', 'editNamedUserTagGroups')
  },

  /**
   * Creates an editor to modify the channel tag groups.
   *
   * @return A tag group editor.
   */
  editChannelTagGroups: function() {
    return tagGroupEditor('UAirship.editTagGroups', 'editChannelTagGroups')
  },

  // Location

  /**
   * Enables or disables Urban Airship location services.
   *
   * @param {Boolean} enabled true to enable location, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setLocationEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setLocationEnabled', arguments)
    callNative(success, failure, "setLocationEnabled", [!!enabled])
  },

  /**
   * Checks if location is enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isLocationEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isLocationEnabled', arguments)
    callNative(success, failure, "isLocationEnabled")
  },

  /**
   * Enables or disables background location.
   *
   * @param {Boolean} enabled true to enable background location, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setBackgroundLocationEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setBackgroundLocationEnabled', arguments)
    callNative(success, failure, "setBackgroundLocationEnabled", [!!enabled])
  },

  /**
   * Checks if background location is enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isBackgroundLocationEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isBackgroundLocationEnabled', arguments)
    callNative(success, failure, "isBackgroundLocationEnabled")
  },

  /**
   * Records the current location.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  recordCurrentLocation: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.recordCurrentLocation', arguments)
    callNative(success, failure, "recordCurrentLocation")
  },

  /**
   * Displays the message center.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  displayMessageCenter: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.displayMessageCenter', arguments)
    callNative(success, failure, "displayMessageCenter")
  },

  /**
   * Gets the array of inbox messages. Each message will have the following properties:
   * "id": string - The messages ID. Needed to display, mark as read, or delete the message.
   * "title": string - The message title.
   * "sentDate": number - The message sent date in milliseconds.
   * "listIconUrl": string, optional - The icon url for the message.
   * "isRead": boolean - The unread/read status of the message.
   * "extras": object - String to String map of any message extras.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getInboxMessages: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getInboxMessages', arguments)
    callNative(success, failure, "getInboxMessages")
  },

  /**
   * Marks an inbox message read.
   *
   * @param {String} messageId The ID of the message to mark as read.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  markInboxMessageRead: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.markInboxMessageRead', arguments)
    callNative(success, failure, 'markInboxMessageRead', [messageId])
  },

  /**
   * Deletes an inbox message.
   *
   * @param {String} messageId The ID of the message to delete.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  deleteInboxMessage: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.deleteInboxMessage', arguments)
    callNative(success, failure, 'deleteInboxMessage', [messageId])
  },

  /**
   * Displays the inbox message using a full screen view.
   *
   * @param {String} messageId The ID of the message to display.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  displayInboxMessage: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.displayInboxMessage', arguments)
    callNative(success, failure, 'displayInboxMessage', [messageId])
  },

  /**
   * Displays the inbox message using an overlay display.
   *
   * @param {String} messageId The ID of the message to display.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  overlayInboxMessage: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.overlayInboxMessage', arguments)
    callNative(success, failure, 'overlayInboxMessage', [messageId])
  },

  // iOS only

  /**
   * Enables or disables auto badge. Defaults to `NO`.
   *
   * @param {Boolean} enabled true to enable auto badge, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setAutobadgeEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setAutobadgeEnabled', arguments)
    callNative(success, failure, "setAutobadgeEnabled", [!!enabled])
  },

  /**
   * Sets the badge number.
   *
   * @param {Number} number specified badge to set.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setBadgeNumber: function(number, success, failure) {
    argscheck.checkArgs('nFF', 'UAirship.setBadgeNumber', arguments)
    callNative(success, failure, "setBadgeNumber", [number])
  },

  /**
   * Returns the current badge number.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  getBadgeNumber: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getBadgeNumber', arguments)
    callNative(success, failure, "getBadgeNumber")
  },

  /**
   * Clears the badge.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  resetBadge: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.resetBadge', arguments)
    callNative(success, failure, "resetBadge")
  },

  /**
   * Sets the iOS notification types. Specify the combination of
   * badges, sound and alerts are desired.
   *
   * @param {Number} types specified notification types.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setNotificationTypes: function(types, success, failure) {
    argscheck.checkArgs('nFF', 'UAirship.setNotificationTypes', arguments)
    callNative(success, failure, "setNotificationTypes", [types])
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
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  clearNotifications: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.clearNotifications', arguments)
    callNative(success, failure, "clearNotifications")
  },

  /**
   * Checks if notification sound is enabled or not.
   *
   */
  isSoundEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isSoundEnabled', arguments)
    callNative(success, failure, "isSoundEnabled")
  },

  /**
   * Enables or disables notification sound.
   *
   * @param {Boolean} enabled true to enable sound, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setSoundEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setSoundEnabled', arguments)
    callNative(success, failure, "setSoundEnabled", [!!enabled])
  },

  /**
   * Checks if notification vibration is enabled or not.
   *
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  isVibrateEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isVibrateEnabled', arguments)
    callNative(success, failure, "isVibrateEnabled")
  },

  /**
   * Enables or disables notification vibration.
   *
   * @param {Boolean} enabled true to enable vibration, false to disable.
   * @param {Function} success The function to call on success.
   * @param {Function} failure The function to call on failure.
   */
  setVibrateEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setVibrateEnabled', arguments)
    callNative(success, failure, "setVibrateEnabled", [!!enabled])
  }
}

module.exports = plugin
