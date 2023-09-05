/* Copyright Urban Airship and Contributors */

export class AirshipPush {
        
    var cordova = require("cordova"),
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
    
    // Helper method to run an action
    function _runAction(actionName, actionValue, success, failure) {
        var successWrapper = function(result) {
            if (success) {
                success(result.value)
            }
        }
        
        callNative(successWrapper, failure, "runAction", [actionName, actionValue])
    }

    /**
     * @module AirshipPush
     */
    module.exports = {
        
        /**
         * Enables or disables user notifications.
         *
         * @param {boolean} enabled true to enable notifications, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setUserNotificationsEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setUserNotificationsEnabled', arguments)
        callNative(success, failure, "setUserNotificationsEnabled", [!!enabled])
    },
        
        /**
         * Checks if user notifications are enabled or not.
         *
         * @param {function(enabled)} success Success callback.
         * @param {boolean} success.enabled Flag indicating if user notifications is enabled or not.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    isUserNotificationsEnabled: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.isUserNotificationsEnabled', arguments)
        callNative(success, failure, "isUserNotificationsEnabled")
    },
        
        /**
         * Enables user notifications.
         *
         * @param {function} success Success callback.
         * @param {boolean} success.enabled Flag indicating if user notifications enablement was authorized or not.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    enableUserNotifications: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.enableUserNotifications', arguments)
        callNative(success, failure, "enableUserNotifications")
    },
        
        /**
         * Returns the last notification that launched the application.
         *
         * @param {Boolean} clear true to clear the notification.
         * @param {function(push)} success The function to call on success.
         * @param {object} success.push The push message object containing data associated with a push notification.
         * @param {string} success.push.message The push alert message.
         * @param {object} success.push.extras Any push extras.
         * @param {number} [success.push.notification_id] The Android notification ID.
         * @param {failureCallback} [failure] The function to call on failure.
         * @param {string} failure.message The error message.
         */
    getLaunchNotification: function(clear, success, failure) {
        argscheck.checkArgs('*fF', 'UAirship.getLaunchNotification', arguments)
        callNative(success, failure, "getLaunchNotification", [!!clear])
    },
        
        /**
         * Checks if quiet time is enabled or not.
         *
         * @param {function(enabled)} success Success callback.
         * @param {boolean} success.enabled Flag indicating if quiet time is enabled or not.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    isQuietTimeEnabled: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.isQuietTimeEnabled', arguments)
        callNative(success, failure, "isQuietTimeEnabled")
    },
        
        /**
         * Enables or disables quiet time.
         *
         * @param {Boolean} enabled true to enable quiet time, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setQuietTimeEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setQuietTimeEnabled', arguments)
        callNative(success, failure, "setQuietTimeEnabled", [!!enabled])
    },
        
        /**
         * Checks if the device is currently in quiet time.
         *
         * @param {function(inQuietTime)} success Success callback.
         * @param {boolean} success.inQuietTime Flag indicating if quiet time is currently in effect.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
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
         * @param {function(quietTime)} success The function to call on success.
         * @param {object} success.quietTime The quietTime object represents a timespan during
         *        which notifications should be silenced.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
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
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setQuietTime: function(startHour, startMinute, endHour, endMinute, success, failure) {
        argscheck.checkArgs('nnnnFF', 'UAirship.setQuietTime', arguments)
        callNative(success, failure, "setQuietTime", [startHour, startMinute, endHour, endMinute])
    },
        
        /**
         * Clears a notification by identifier.
         *
         * @param {string} identifier The notification identifier.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         */
    clearNotification: function(identifier, success, failure) {
        argscheck.checkArgs('sFF', 'UAirship.clearNotification', arguments)
        callNative(success, failure, "clearNotification", [identifier])
    },
        
        /**
         * Clears all notifications posted by the application.
         *
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    clearNotifications: function(success, failure) {
        argscheck.checkArgs('FF', 'UAirship.clearNotifications', arguments)
        callNative(success, failure, "clearNotifications")
    },
        
        /**
         * Gets currently active notifications.
         *
         * Note: On Android this functionality is only supported on Android M or higher.
         *
         * @param {function(messages)} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         */
    getActiveNotifications: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.getActiveNotifications', arguments)
        callNative(success, failure, "getActiveNotifications")
    },
        
        // iOS only
        
        /**
         * Enables or disables auto badge. Defaults to `NO`.
         *
         * @param {Boolean} enabled true to enable auto badge, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setAutobadgeEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setAutobadgeEnabled', arguments)
        callNative(success, failure, "setAutobadgeEnabled", [!!enabled])
    },
        
        /**
         * Sets the badge number.
         *
         * @param {Number} number specified badge to set.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setBadgeNumber: function(number, success, failure) {
        argscheck.checkArgs('nFF', 'UAirship.setBadgeNumber', arguments)
        callNative(success, failure, "setBadgeNumber", [number])
    },
        
        /**
         * Returns the current badge number.
         *
         * @param {function(badgeNumber)} success The function to call on success.
         * @param {int} success.badgeNumber The current application badge number.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    getBadgeNumber: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.getBadgeNumber', arguments)
        callNative(success, failure, "getBadgeNumber")
    },
        
        /**
         * Clears the badge.
         *
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    resetBadge: function(success, failure) {
        argscheck.checkArgs('FF', 'UAirship.resetBadge', arguments)
        callNative(success, failure, "resetBadge")
    },
        
        /**
         * Sets the iOS notification types. Specify the combination of
         * badges, sound and alerts that are desired.
         *
         * @param {notificationType} types specified notification types.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setNotificationTypes: function(types, success, failure) {
        argscheck.checkArgs('nFF', 'UAirship.setNotificationTypes', arguments)
        callNative(success, failure, "setNotificationTypes", [types])
    },
        
        /**
         * Sets the iOS presentation options. Specify the combination of
         * badges, sound and alerts that are desired.
         *
         * @param {presentationOptions} types specified presentation options.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setPresentationOptions: function(options, success, failure) {
        argscheck.checkArgs('nFF', 'UAirship.setPresentationOptions', arguments)
        callNative(success, failure, "setPresentationOptions", [options])
    },
        
        /**
         * Enables/Disables foreground notifications display on Android.
         *
         * @param {Boolean} enabled true to enable foreground notifications, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setAndroidForegroundNotificationsEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setAndroidForegroundNotificationsEnabled', arguments)
        callNative(success, failure, "setAndroidForegroundNotificationsEnabled", [!!enabled])
    },
        
        /**
         * Enum for notification types.
         * @readonly
         * @enum {number}
         */
    notificationType: {
    none: 0,
    badge: 1,
    sound: 2,
    alert: 4
    },
        
        /**
         * Enum for presentation options.
         * @readonly
         * @enum {number}
         */
    presentationOptions: {
    none: 0,
    badge: 1,
    sound: 2,
    alert: 4
    },
        
        // Android only
        
        /**
         * Checks if notification sound is enabled or not.
         *
         * @param {function(enabled)} success Success callback.
         * @param {boolean} success.enabled Flag indicating if sound is enabled or not.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    isSoundEnabled: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.isSoundEnabled', arguments)
        callNative(success, failure, "isSoundEnabled")
    },
        
        /**
         * Enables or disables notification sound.
         *
         * @param {Boolean} enabled true to enable sound, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setSoundEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setSoundEnabled', arguments)
        callNative(success, failure, "setSoundEnabled", [!!enabled])
    },
        
        /**
         * Checks if notification vibration is enabled or not.
         *
         * @param {function(enabled)} success Success callback.
         * @param {boolean} success.enabled Flag indicating if vibration is enabled or not.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    isVibrateEnabled: function(success, failure) {
        argscheck.checkArgs('fF', 'UAirship.isVibrateEnabled', arguments)
        callNative(success, failure, "isVibrateEnabled")
    },
        
        /**
         * Enables or disables notification vibration.
         *
         * @param {Boolean} enabled true to enable vibration, false to disable.
         * @param {function} [success] Success callback.
         * @param {function(message)} [failure] Failure callback.
         * @param {string} failure.message The error message.
         */
    setVibrateEnabled: function(enabled, success, failure) {
        argscheck.checkArgs('*FF', 'UAirship.setVibrateEnabled', arguments)
        callNative(success, failure, "setVibrateEnabled", [!!enabled])
    }
        
    }


}
