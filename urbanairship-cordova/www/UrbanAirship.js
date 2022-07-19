/* Copyright Urban Airship and Contributors */

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
 * Constant for Feature NONE.
 */
const FEATURE_NONE = "FEATURE_NONE"
/**
 * Constant for InApp Automation Feature.
 */
const FEATURE_IN_APP_AUTOMATION = "FEATURE_IN_APP_AUTOMATION"
/**
 * Constant for Message Center Feature.
 */
const FEATURE_MESSAGE_CENTER = "FEATURE_MESSAGE_CENTER"
/**
 * Constant for Push Feature.
 */
const FEATURE_PUSH = "FEATURE_PUSH"
/**
 * Constant for Chat Feature.
 */
const FEATURE_CHAT = "FEATURE_CHAT"
/**
 * Constant for Analytics Feature.
 */
const FEATURE_ANALYTICS = "FEATURE_ANALYTICS"
/**
 * Constant for Tags and Attributes Feature.
 */
const FEATURE_TAGS_AND_ATTRIBUTES = "FEATURE_TAGS_AND_ATTRIBUTES"
/**
 * Constant for Contacts Feature.
 */
const FEATURE_CONTACTS = "FEATURE_CONTACTS"
/**
 * Constant for Location Feature.
 */
const FEATURE_LOCATION = "FEATURE_LOCATION"
/**
 * Constant for all Feature.
 */
const FEATURE_ALL = "FEATURE_ALL"

/**
 * Helper object to edit tag groups.
 *
 * Normally not created directly. Instead use [UrbanAirship.editNamedUserTagGroups]{@link module:UrbanAirship.editNamedUserTagGroups}
 * or [UrbanAirship.editChannelTagGroups]{@link module:UrbanAirship.editChannelTagGroups}.
 *
 * @class TagGroupEditor
 * @param nativeMethod The native method to call on apply.
 */
function TagGroupEditor(nativeMethod) {

    // Store the raw operations and let the SDK combine them
    var operations = []

    var editor = {}

    /**
     * Adds tags to a tag group.
     * @instance
     * @memberof TagGroupEditor
     * @function addTags
     *
     * @param {string} tagGroup The tag group.
     * @param {array<string>} tags Tags to add.
     * @return {TagGroupEditor} The tag group editor instance.
     */
    editor.addTags = function(tagGroup, tags) {
      argscheck.checkArgs('sa', "TagGroupEditor#addTags", arguments)
      var operation = { "operation": "add", "group": tagGroup, "tags": tags }
      operations.push(operation)
      return editor
    }

    /**
     * Removes a tag from the tag group.
     * @instance
     * @memberof TagGroupEditor
     * @function removeTags
     *
     * @param {string} tagGroup The tag group.
     * @param {array<string>} tags Tags to remove.
     * @return {TagGroupEditor} The tag group editor instance.
     */
    editor.removeTags = function(tagGroup, tags) {
      argscheck.checkArgs('sa', "TagGroupEditor#removeTags", arguments)
      var operation = { "operation": "remove", "group": tagGroup, "tags": tags }
      operations.push(operation)
      return editor
    }

    /**
     * Applies the tag changes.
     * @instance
     * @memberof TagGroupEditor
     * @function apply
     *
     * @param {function} [success] Success callback.
     * @param {function(message)} [failure] Failure callback.
     * @param {string} failure.message The failure message.
     * @return {TagGroupEditor} The tag group editor instance.
     */
    editor.apply = function(success, failure) {
      argscheck.checkArgs('FF', "TagGroupEditor#apply", arguments)
      callNative(success, failure, nativeMethod, [operations])
      operations = []
      return editor
    }

    return editor
}

/**
* Helper object to subscribe/unsubscribe to/from a list.
*
* Normally not created directly. Instead use [UrbanAirship.editSubscriptionLists]{@link module:UrbanAirship.editSubscriptionLists}.
*
* @class ChannelSubscriptionListEditor
* @param nativeMethod The native method to call on apply.
*/
function ChannelSubscriptionListEditor(nativeMethod) {

    // Store the raw operations and let the SDK combine them
    var operations = []

    var editor = {}

    /**
     * Subscribes to a list.
     * @instance
     * @memberof ChannelSubscriptionListEditor
     * @function subscribe
     *
     * @param {subscriptionListID} subscriptionListID The subscription list identifier.
     * @return {ChannelSubscriptionListEditor} The subscription list editor instance.
     */
    editor.subscribe = function(subscriptionListID) {
        argscheck.checkArgs('s', "ChannelSubscriptionListEditor#subscribe", arguments)
        var operation = { "operation": "subscribe", "listId": subscriptionListID}
        operations.push(operation)
        return editor
    }

    /**
     * Unsubscribes from a list.
     * @instance
     * @memberof ChannelSubscriptionListEditor
     * @function unsubscribe
     *
     * @param {subscriptionListID} subscriptionListID The subscription list identifier.
     * @return {ChannelSubscriptionListEditor} The subscription list editor instance.
     */
    editor.unsubscribe = function(subscriptionListID) {
        argscheck.checkArgs('s', "ChannelSubscriptionListEditor#unsubscribe", arguments)
        var operation = { "operation": "unsubscribe", "listId": subscriptionListID}
        operations.push(operation)
        return editor
    }

    /**
     * Applies subscription list changes.
     * @instance
     * @memberof ChannelSubscriptionListEditor
     * @function apply
     *
     * @param {function} [success] Success callback.
     * @param {function(message)} [failure] Failure callback.
     * @param {string} failure.message The failure message.
     * @return {ChannelSubscriptionListEditor} The subscription List editor instance.
     */
    editor.apply = function(success, failure) {
        argscheck.checkArgs('FF', "ChannelSubscriptionListEditor#apply", arguments)
        callNative(success, failure, nativeMethod, [operations])
        operations = []
        return editor
    }

    return editor
}

/**
* Helper object to subscribe/unsubscribe to/from a list.
*
* Normally not created directly. Instead use [UrbanAirship.editContactSubscriptionLists]{@link module:UrbanAirship.editContactSubscriptionLists}.
*
* @class ContactSubscriptionListEditor
* @param nativeMethod The native method to call on apply.
*/
function ContactSubscriptionListEditor(nativeMethod) {

    // Store the raw operations and let the SDK combine them
    var operations = []

    var editor = {}

    /**
     * Subscribes to a contact list.
     * @instance
     * @memberof ContactSubscriptionListEditor
     * @function subscribe
     *
     * @param {subscriptionListID} subscriptionListID The subscription list identifier.
     * @param {contactScope} contactScope Defines the channel types that the change applies to.
     * @return {ContactSubscriptionListEditor} The subscription list editor instance.
     */
    editor.subscribe = function(contactSubscriptionListID, contactScope) {
        argscheck.checkArgs('ss', "ContactSubscriptionListEditor#subscribe", arguments)
        var operation = { "operation": "subscribe", "listId": contactSubscriptionListID, "scope": contactScope}
        operations.push(operation)
        return editor
    }

    /**
     * Unsubscribes from a contact list.
     * @instance
     * @memberof ContactSubscriptionListEditor
     * @function unsubscribe
     *
     * @param {subscriptionListID} subscriptionListID The subscription list identifier.
     * @param {contactScope} contactScope Defines the channel types that the change applies to.
     * @return {ContactSubscriptionListEditor} The subscription list editor instance.
     */
    editor.unsubscribe = function(contactSubscriptionListID, contactScope) {
        argscheck.checkArgs('ss', "ContactSubscriptionListEditor#unsubscribe", arguments)
        var operation = { "operation": "unsubscribe", "listId": contactSubscriptionListID, "scope": contactScope}
        operations.push(operation)
        return editor
    }

    /**
     * Applies subscription list changes.
     * @instance
     * @memberof ContactSubscriptionListEditor
     * @function apply
     *
     * @param {function} [success] Success callback.
     * @param {function(message)} [failure] Failure callback.
     * @param {string} failure.message The failure message.
     * @return {ContactSubscriptionListEditor} The subscription List editor instance.
     */
    editor.apply = function(success, failure) {
        argscheck.checkArgs('FF', "ContactSubscriptionListEditor#apply", arguments)
        callNative(success, failure, nativeMethod, [operations])
        operations = []
        return editor
    }

    return editor
}

/**
 * Helper object to edit attributes groups.
 *
 * Normally not created directly. Instead use [UrbanAirship.editChannelAttributes]{@link module:UrbanAirship.editChannelAttributes}.
 *
 * @class AttributeEditor
 * @param nativeMethod The native method to call on apply.
 */
function AttributesEditor(nativeMethod) {
  var operations = []
  var editor = {}

  /**
   * Sets an attribute.
   * @instance
   * @memberof AttributesEditor
   * @function setAttribute
   *
   * @param {string} name The attribute name.
   * @param {string|number|Date} value The attribute's value.
   * @return {AttributesEditor} The attribute editor instance.
   */
    editor.setAttribute = function(name, value) {
        argscheck.checkArgs('s*', "AttributesEditor#setAttribute", arguments)

        var operation = { "action": "set", "value": value, "key": name }

        if (typeof value === "string") {
            operation["type"] = "string"
        } else if (typeof value === "number") {
            operation["type"] = "number"
        } else if (typeof value === "boolean") {
            // No boolean attribute type. Convert value to string.
            operation["type"] = "string"
            operation["value"] = value.toString();
        } else if (value instanceof Date) {
            // JavaScript's date type doesn't pass through the JS to native bridge. Dates are instead serialized as milliseconds since epoch.
            operation["type"] = "date"
            operation["value"] = value.getTime()
        } else {
            throw("Unsupported attribute type: " + typeof value)
        }

        operations.push(operation)

        return editor
  }

  /**
   * Removes an attribute.
   * @instance
   * @memberof AttributesEditor
   * @function removeAttribute
   *
   * @param {string} name The attribute's name.
   * @return {AttributesEditor} The attribute editor instance.
   */
  editor.removeAttribute = function(name) {
    argscheck.checkArgs('s', "AttributesEditor#removeAttribute", arguments)
    var operation = { "action": "remove", "key": name }
    operations.push(operation)
    return editor
  }

  /**
   * Applies the attribute changes.
   * @instance
   * @memberof AttributesEditor
   * @function apply
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The failure message.
   * @return {TagGroupEditor} The tag group editor instance.
   */
  editor.apply = function(success, failure) {
    argscheck.checkArgs('FF', "AttributesEditor#apply", arguments)
    callNative(success, failure, nativeMethod, [operations])
    operations = []
    return editor
  }

  return editor
}

function bindDocumentEvent() {
    callNative(function(e) {
      console.log("Firing document event: " + e.eventType)
      cordova.fireDocumentEvent(e.eventType, e.eventData)
    }, null, "registerListener")
}

document.addEventListener("deviceready", bindDocumentEvent, false)

/**
 * @module UrbanAirship
 */
module.exports = {

  FEATURE_NONE: FEATURE_NONE,
  FEATURE_IN_APP_AUTOMATION: FEATURE_IN_APP_AUTOMATION,
  FEATURE_MESSAGE_CENTER: FEATURE_MESSAGE_CENTER,
  FEATURE_PUSH: FEATURE_PUSH,
  FEATURE_CHAT: FEATURE_CHAT,
  FEATURE_ANALYTICS: FEATURE_ANALYTICS,
  FEATURE_TAGS_AND_ATTRIBUTES: FEATURE_TAGS_AND_ATTRIBUTES,
  FEATURE_CONTACTS: FEATURE_CONTACTS,
  FEATURE_LOCATION: FEATURE_LOCATION,
  FEATURE_ALL: FEATURE_ALL,

  /**
   * Event fired when a new deep link is received.
   *
   * @event deep_link
   * @type {object}
   * @param {string} [deepLink] The deep link.
   */

  /**
   * Event fired when a channel registration occurs.
   *
   * @event registration
   * @type {object}
   * @param {string} [channelID] The channel ID.
   * @param {string} [registrationToken] The deviceToken on iOS, and the FCM/ADM token on Android.
   * @param {string} [error] Error message if an error occurred.
   */

  /**
   * Event fired when the inbox is updated.
   *
   * @event inbox_updated
   */

  /**
   * Event fired when the inbox needs to be displayed. This event is only emitted if auto
   * launch message center is disabled.
   *
   * @event show_inbox
   * @type {object}
   * @param {string} [messageId] The optional message ID.
   */

  /**
   * Event fired when a push is received.
   *
   * @event push
   * @type {object}
   * @param {string} message The push alert message.
   * @param {string} title The push title.
   * @param {string} subtitle The push subtitle.
   * @param {object} extras Any push extras.
   * @param {object} aps The raw aps dictionary (iOS only)
   * @param {number} [notification_id] The Android notification ID. Deprecated in favor of notificationId.
   * @param {string} [notificationId] The notification ID.
   */

  /**
   * Event fired when notification opened.
   *
   * @event notification_opened
   * @type {object}
   * @param {string} message The push alert message.
   * @param {object} extras Any push extras.
   * @param {number} [notification_id] The Android notification ID. Deprecated in favor of notificationId.
   * @param {string} [notificationId] The notification ID.
   * @param {string} [actionID] The ID of the notification action button if available.
   * @param {boolean} isForeground Will always be true if the user taps the main notification. Otherwise its defined by the notification action button.
   */

  /**
   * Event fired when the user notification opt-in status changes.
   *
   * @event notification_opt_in_status
   * @type {object}
   * @param {boolean} optIn If the user is opted in or not to user notifications.
   * @param {object} [authorizedNotificationSettings] iOS only. A map of authorized settings.
   * @param {boolean} authorizedNotificationSettings.alert If alerts are authorized.
   * @param {boolean} authorizedNotificationSettings.sound If sounds are authorized.
   * @param {boolean} authorizedNotificationSettings.badge If badges are authorized.
   * @param {boolean} authorizedNotificationSettings.carPlay If car play is authorized.
   * @param {boolean} authorizedNotificationSettings.lockScreen If the lock screen is authorized.
   * @param {boolean} authorizedNotificationSettings.notificationCenter If the notification center is authorized.
   */

  /**
   * Re-attaches document event listeners in this webview
   */
  reattach: bindDocumentEvent,

  /**
   * Initailizes Urban Airship.
   *
   * The plugin will automatically call takeOff during the next app init in
   * order to properly handle incoming push. If takeOff is called multiple times
   * in a session, or if the config is different than the previous sesssion, the
   * new config will not be used until the next app start.
   *
   * @param {object}  config The Urban Airship config.
   * @param {string}  config.site Sets the cloud site, must be either EU or US.
   * @param {object}  config.development The Urban Airship development config.
   * @param {string}  config.development.appKey The development appKey.
   * @param {string}  config.development.appSecret The development appSecret.
   * @param {object}  config.production The Urban Airship production config.
   * @param {string}  config.production.appKey The production appKey.
   * @param {string}  config.production.appSecret The production appSecret.
   */
  takeOff: function(config, success, failure) {
    argscheck.checkArgs("*FF", "UAirship.takeOff", arguments);
    callNative(success, failure, "takeOff", [config]);
  },

  /**
   * Sets the Android notification config. Values not set will fallback to any values set in the config.xml.
   *
   * @param {object}  config The notification config.
   * @param {string}  [config.icon] The name of the drawable resource to use as the notification icon.
   * @param {string}  [config.largeIcon] The name of the drawable resource to use as the notification large icon.
   * @param {string}  [config.accentColor] The notification accent color. Format is #AARRGGBB.
   */
  setAndroidNotificationConfig: function(config, success, failure) {
    argscheck.checkArgs("*FF", "UAirship.setAndroidNotificationConfig", arguments);
    callNative(success, failure, "setAndroidNotificationConfig", [config]);
  },

  /**
   * Sets the default behavior when the message center is launched from a push
   * notification. If set to false the message center must be manually launched.
   *
   * @param {boolean} enabled true to automatically launch the default message center, false to disable.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setAutoLaunchDefaultMessageCenter: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setAutoLaunchDefaultMessageCenter', arguments)
    callNative(success, failure, "setAutoLaunchDefaultMessageCenter", [!!enabled]);
  },

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
   * Checks if app notifications are enabled or not. Its possible to have `userNotificationsEnabled`
   * but app notifications being disabled if the user opted out of notifications.
   *
   * @param {function(enabled)} success Success callback.
   * @param {boolean} success.enabled Flag indicating if app notifications is enabled or not.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  isAppNotificationsEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isAppNotificationsEnabled', arguments)
    callNative(success, failure, "isAppNotificationsEnabled")
  },

  /**
   * Returns the channel ID.
   *
   * @param {function(ID)} success The function to call on success.
   * @param {string} success.ID The channel ID string
   * @param {failureCallback} [failure] The function to call on failure.
   * @param {string} failure.message The error message.
   */
  getChannelID: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getChannelID', arguments)
    callNative(success, failure, "getChannelID")
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
   * Returns the last received deep link.
   *
   * @param {Boolean} clear true to clear the deep link.
   * @param {function(push)} success The function to call on success.
   * @param {string} success.deepLink The deep link.
   * @param {failureCallback} [failure] The function to call on failure.
   * @param {string} failure.message The error message.
   */
  getDeepLink: function(clear, success, failure) {
    argscheck.checkArgs('*fF', 'UAirship.getDeepLink', arguments)
    callNative(success, failure, "getDeepLink", [!!clear])
  },

  /**
   * Returns the tags as an array.
   *
   * @param {function(tags)} success The function to call on success.
   * @param {array} success.tags The tags as an array.
   * @param {failureCallback} [failure] The function to call on failure.
   * @param {string} failure.message The error message.
   */
  getTags: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getTags', arguments);
    callNative(success, failure, "getTags")
  },

  /**
   * Sets the tags.
   *
   * @param {Array} tags an array of strings.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setTags: function(tags, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.setTags', arguments);
    callNative(success, failure, "setTags", [tags])
  },

  /**
   * Returns the alias.
   *
   * @deprecated Deprecated since 6.7.0 - to be removed in a future version of the plugin - please use getNamedUser
   *
   * @param {function(currentAlias)} success The function to call on success.
   * @param {string} success.currentAlias The alias as a string.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  getAlias: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getAlias', arguments)
    callNative(success, failure, "getAlias")
  },

  /**
   * Sets the alias.
   *
   * @deprecated Deprecated since 6.7.0  - to be removed in a future version of the plugin - please use setNamedUser
   *
   * @param {String} alias string
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setAlias: function(alias, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.setAlias', arguments)
    callNative(success, failure, "setAlias", [alias])
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
   * Enables or disables analytics.
   *
   * Disabling analytics will delete any locally stored events
   * and prevent any events from uploading. Features that depend on analytics being
   * enabled may not work properly if it's disabled (reports, region triggers,
   * location segmentation, push to local time).
   *
   * @param {Boolean} enabled true to enable analytics, false to disable.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setAnalyticsEnabled: function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'UAirship.setAnalyticsEnabled', arguments)
    callNative(success, failure, "setAnalyticsEnabled", [!!enabled])
  },

  /**
   * Checks if analytics is enabled or not.
   *
   * @param {function(enabled)} success Success callback.
   * @param {boolean} success.enabled Flag indicating if analytics is enabled or not.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  isAnalyticsEnabled: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.isAnalyticsEnabled', arguments)
    callNative(success, failure, "isAnalyticsEnabled")
  },

  /**
   * Returns the named user ID.
   *
   * @param {function(namedUser)} success The function to call on success.
   * @param {string} success.namedUser The named user ID as a string.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  getNamedUser: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getNamedUser', arguments)
    callNative(success, failure, "getNamedUser")
  },

  /**
   * Sets the named user ID.
   *
   * @param {String} namedUser identifier string.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setNamedUser: function(namedUser, success, failure) {
    argscheck.checkArgs('SFF', 'UAirship.setNamedUser', arguments)
    callNative(success, failure, "setNamedUser", [namedUser])
  },

  /**
   * Runs an Urban Airship action.
   *
   * @param {String} actionName action as a string.
   * @param {*} actionValue
   * @param {function(result)} [success] The function to call on success.
   * @param {object} success.result The result's value.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  runAction: function(actionName, actionValue, success, failure) {
    argscheck.checkArgs('s*FF', 'UAirship.runAction', arguments)
    _runAction(actionName, actionValue, success, failure)
  },

  /**
   * Creates an editor to modify the named user tag groups.
   *
   * @return {TagGroupEditor} A tag group editor instance.
   */
  editNamedUserTagGroups: function() {
    return new TagGroupEditor('editNamedUserTagGroups')
  },

  /**
   * Creates an editor to modify the channel tag groups.
   *
   * @return {TagGroupEditor} A tag group editor instance.
   */
  editChannelTagGroups: function() {
    return new TagGroupEditor('editChannelTagGroups')
  },

  /**
   * Creates an editor to modify the channel attributes.
   *
   * @return {AttributesEditor} An attributes editor instance.
   */
  editChannelAttributes: function() {
    return new AttributesEditor('editChannelAttributes')
  },

  /**
   * Creates an editor to modify the channel subscription lists.
   *
   * @return {ChannelSubscriptionListEditor} A subscription list editor instance.
   */
  editChannelSubscriptionLists: function() {
    return new ChannelSubscriptionListEditor('editChannelSubscriptionLists')
  },

  /**
   * Creates an editor to modify the contact subscription lists.
   *
   * @return {ContacttSubscriptionListEditor} A subscription list editor instance.
   */
  editContactSubscriptionLists: function() {
    return new ContactSubscriptionListEditor('editContactSubscriptionLists')
  },

  /**
   * Creates an editor to modify the named user attributes.
   *
   * @return {AttributesEditor} An attributes editor instance.
   */
  editNamedUserAttributes: function() {
    return new AttributesEditor('editNamedUserAttributes')
  },

  /**
   * Sets an associated identifier for the Connect data stream.
   *
   * @param {string} Custom key for identifier.
   * @param {string} The identifier value.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setAssociatedIdentifier: function(key, identifier, success, failure) {
    argscheck.checkArgs('ssFF', 'UAirship.setAssociatedIdentifier', arguments)
    callNative(success, failure, "setAssociatedIdentifier", [key, identifier])
  },

  /**
   * Displays the message center.
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  displayMessageCenter: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.displayMessageCenter', arguments)
    callNative(success, failure, "displayMessageCenter")
  },

  /**
   * Dismiss the message center.
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  dismissMessageCenter: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.dismissMessageCenter', arguments)
    callNative(success, failure, "dismissMessageCenter")
  },

  /**
   * Dismiss the inbox message.
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  dismissInboxMessage: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.dismissInboxMessage', arguments)
    callNative(success, failure, "dismissInboxMessage")
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
   * @param {function(messages)} success The function to call on success.
   * @param {array} success.messages The array of inbox messages.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  getInboxMessages: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getInboxMessages', arguments)
    callNative(success, failure, "getInboxMessages")
  },

  /**
   * Marks an inbox message read.
   *
   * @param {String} messageId The ID of the message to mark as read.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  markInboxMessageRead: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.markInboxMessageRead', arguments)
    callNative(success, failure, 'markInboxMessageRead', [messageId])
  },

  /**
   * Deletes an inbox message.
   *
   * @param {String} messageId The ID of the message to delete.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  deleteInboxMessage: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.deleteInboxMessage', arguments)
    callNative(success, failure, 'deleteInboxMessage', [messageId])
  },

  /**
   * Displays the inbox message using a full screen view.
   *
   * @param {String} messageId The ID of the message to display.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  displayInboxMessage: function(messageId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.displayInboxMessage', arguments)
    callNative(success, failure, 'displayInboxMessage', [messageId])
  },

  /**
   * Forces the inbox to refresh. This is normally not needed as the inbox
   * will automatically refresh on foreground or when a push arrives thats
   * associated with a message, but it can be useful when providing a refresh
   * button for the message listing.
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  refreshInbox: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.refreshInbox', arguments)
    callNative(success, failure, 'refreshInbox')
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
  },

  /**
   * Adds a custom event.
   *
   * @param {object} event The custom event object.
   * @param {string} event.name The event's name.
   * @param {number} [event.value] The event's value.
   * @param {string} [event.transactionId] The event's transaction ID.
   * @param {object} [event.properties] The event's properties. Only numbers, booleans, strings, and array of strings are supported.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  addCustomEvent: function(event, success, failure) {
    argscheck.checkArgs('oFF', 'UAirship.addCustomEvent', arguments)

    var actionArg = {
      event_name: event.name,
      event_value: event.value,
      transaction_id: event.transactionId,
      properties: event.properties
    }

    _runAction("add_custom_event_action", actionArg, success, failure)
  },

  /**
   * Initiates screen tracking for a specific app screen, must be called once per tracked screen.
   *
   * @param {string} screen The screen's string identifier.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  trackScreen: function(screen, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.trackScreen', arguments)
    callNative(success, failure, "trackScreen", [screen])
  },

  /**
   * Enables features, adding them to the set of currently enabled features.
   *
   * @param {array<string>} features The features to enable.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  enableFeature: function(features, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.enableFeature', arguments)
    callNative(success, failure, "enableFeature", [features])
  },

  /**
   * Disables features, removing them from the set of currently enabled features.
   *
   * @param {array<string>} features The features to disable.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  disableFeature: function(features, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.disableFeature', arguments)
    callNative(success, failure, "disableFeature", [features])
  },

  /**
   * Sets the current enabled features, replacing any currently enabled features with the given set.
   *
   * @param {array<string>} features The features to set as enabled.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  setEnabledFeatures: function(features, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.setEnabledFeatures', arguments)
    callNative(success, failure, "setEnabledFeatures", [features])
  },

  /**
   * Gets the current enabled features.
   *
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  getEnabledFeatures: function(success, failure) {
    argscheck.checkArgs('FF', 'UAirship.getEnabledFeatures', arguments)
    callNative(success, failure, "getEnabledFeatures")
  },

  /**
   * Checks if all of the given features are enabled.
   *
   * @param {array<string>} features The features to check.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  isFeatureEnabled: function(features, success, failure) {
    argscheck.checkArgs('aFF', 'UAirship.isFeatureEnabled', arguments)
    callNative(success, failure, "isFeatureEnabled", [features])
  },

  /**
   * Opens the Preference Center with the given preferenceCenterId.
   *
   * @param {string} preferenceCenterId The preference center ID.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
  openPreferenceCenter: function(preferenceCenterId, success, failure) {
    argscheck.checkArgs('sFF', 'UAirship.openPreferenceCenter', arguments)
    callNative(success, failure, "openPreferenceCenter", [preferenceCenterId])
  },

  /**
   * Returns the configuration of the Preference Center with the given ID trough a callback method.
   *
   * @param {string} preferenceCenterId The preference center ID.
   * @param {function} [success] Success callback.
   * @param {function(message)} [failure] Failure callback.
   * @param {string} failure.message The error message.
   */
   getPreferenceCenterConfig: function(preferenceCenterId, success, failure) {
     argscheck.checkArgs('sFF', 'UAirship.getPreferenceCenterConfig', arguments)
     callNative(success, failure, "getPreferenceCenterConfig", [preferenceCenterId])
  },

   /**
    * Returns the current set of subscription lists for the current channel,
    * optionally applying pending subscription list changes that will be applied during the next channel update.
    * An empty set indicates that this contact is not subscribed to any lists.
    *
    * @param {function} [success] Success callback.
    * @param {string} failure.message The error message.
    */
  getChannelSubscriptionLists: function(success, failure) {
    argscheck.checkArgs('fF', 'UAirship.getChannelSubscriptionLists', arguments)
    callNative(success, failure, "getChannelSubscriptionLists")
  },

  /**
   * Returns the current set of subscription lists for the current contact,
   * optionally applying pending subscription list changes that will be applied during the next contact update.
   * An empty set indicates that this contact is not subscribed to any lists.
   *
   * @param {function} [success] Success callback.
   * @param {string} failure.message The error message.
   */
   getContactSubscriptionLists: function(success, failure) {
     argscheck.checkArgs('fF', 'UAirship.getContactSubscriptionLists', arguments)
     callNative(success, failure, "getContactSubscriptionLists")
   },

   /**
    * Sets the use custom preference center.
    * 
    * @param {string} preferenceCenterId The preference center ID.
    * @param {boolean} useCustomUi The preference center use custom UI.
    */
    setUseCustomPreferenceCenterUi: function(preferenceCenterId, useCustomUi, success, failure) {
      argscheck.checkArgs("s*FF", "UAirship.setUseCustomPreferenceCenterUi", arguments)
      callNative(success, failure, "setUseCustomPreferenceCenterUi", [preferenceCenterId, useCustomUi])
    },

    /**
      * Overriding the locale.
      *
      * @param {string} localeIdentifier The locale identifier.
      */
     setCurrentLocale: function(localeIdentifier, success, failure) {
         argscheck.checkArgs("sFF", "UAirship.setCurrentLocale", arguments)
         callNative(success, failure, "setCurrentLocale", [localeIdentifier])
     },

     /**
      * Getting the locale currently used by Airship.
      *
      * @param {function} [success] Success callback.
      * @param {string} failure.message The error message.
      */
     getCurrentLocale: function(success, failure) {
         argscheck.checkArgs('fF', 'UAirship.getCurrentLocale', arguments)
         callNative(success, failure, "getCurrentLocale")
     },

     /**
      * Resets the current locale.
      *
      * @param {function} [success] Success callback.
      * @param {function(message)} [failure] Failure callback.
      * @param {string} failure.message The error message.
      */
     clearLocale: function(success, failure) {
         argscheck.checkArgs('FF', 'UAirship.clearLocale', arguments)
         callNative(success, failure, "clearLocale")
     }

}
