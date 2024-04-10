/* Copyright Airship and Contributors */

var cordova = require("cordova"),
    exec = require("cordova/exec"),
    argscheck = require('cordova/argscheck'),
    airship = {
        contact: {},
        channel: {},
        analytics: {},
        locale: {},
        messageCenter: {},
        featureFlagManager: {},
        preferenceCenter: {},
        push: {
            ios: {},
            android: {}
        },
        privacyManager: {},
        actions: {},
        inApp: {}
    }

// Argcheck values:
// * : allow anything, 
// f : function
// a : array
// d : date
// n : number
// s : string
// o : object
// lowercase = required, uppercase = optional

function perform(name, args, success, failure) {
    exec(success, failure, "AirshipCordova", "perform", [name, args])
}

var callbackId = 0
function registerListener(name, callback) {
    var isCancelled = false
    let subCallbackId = callbackId
    callbackId += 1

    exec(function (event) {
        if (!isCancelled) {
            callback(event)
        }
    }, {}, "AirshipCordova", "addListener", [name, subCallbackId])

    let subscription = {}
    subscription.cancel = function () {
        isCancelled = true
        exec({}, {}, "AirshipCordova", "removeListener", [name, subCallbackId])
    }
    return subscription
}

function TagEditor(methodPrefix, nativeMethod) {
    var operations = []
    var editor = {}

    editor.addTags = function (tags) {
        argscheck.checkArgs('a', methodPrefix + "#addTags", arguments)
        var operation = { "operationType": "add", "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.removeTags = function (tags) {
        argscheck.checkArgs('a', methodPrefix + "#removeTags", arguments)
        var operation = { "operationType": "remove", "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.apply = function (success, failure) {
        argscheck.checkArgs('FF', methodPrefix + "#apply", arguments)
        perform(nativeMethod, operations, success, failure)
        operations = []
        return editor
    }

    return editor
}

function TagGroupEditor(methodPrefix, nativeMethod) {
    var operations = []
    var editor = {}

    editor.addTags = function (tagGroup, tags) {
        argscheck.checkArgs('sa', methodPrefix + "#addTags", arguments)
        var operation = { "operationType": "add", "group": tagGroup, "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.removeTags = function (tagGroup, tags) {
        argscheck.checkArgs('sa', methodPrefix + "#removeTags", arguments)
        var operation = { "operationType": "remove", "group": tagGroup, "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.setTags = function (tagGroup, tags) {
        argscheck.checkArgs('sa', methodPrefix + "#setTags", arguments)
        var operation = { "operationType": "set", "group": tagGroup, "tags": tags }
        operations.push(operation)
        return editor
    }

    editor.apply = function (success, failure) {
        argscheck.checkArgs('FF', methodPrefix + "#apply", arguments)
        perform(nativeMethod, operations, success, failure)
        operations = []
        return editor
    }

    return editor
}

function ScopedSubscriptionListEditor(methodPrefix, nativeMethod) {
    var operations = []
    var editor = {}

    editor.subscribe = function (listId, scope) {
        argscheck.checkArgs('ss', methodPrefix + "#subscribe", arguments)
        var operation = { "action": "subscribe", "listId": listId, "scope": scope }
        operations.push(operation)
        return editor
    }

    editor.unsubscribe = function (listId, scope) {
        argscheck.checkArgs('ss', methodPrefix + "#unsubscribe", arguments)
        var operation = { "action": "unsubscribe", "listId": listId, "scope": scope }
        operations.push(operation)
        return editor
    }

    editor.apply = function (success, failure) {
        argscheck.checkArgs('FF', methodPrefix + "#apply", arguments)
        perform(nativeMethod, operations, success, failure)
        operations = []
        return editor
    }

    return editor
}

function SubscriptionListEditor(methodPrefix, nativeMethod) {
    // Store the raw operations and let the SDK combine them
    var operations = []
    var editor = {}

    editor.subscribe = function (listId) {
        argscheck.checkArgs('s', methodPrefix + "#subscribe", arguments)
        var operation = { "action": "subscribe", "listId": listId }
        operations.push(operation)
        return editor
    }

    editor.unsubscribe = function (listId) {
        argscheck.checkArgs('s', methodPrefix + "#unsubscribe", arguments)
        var operation = { "action": "unsubscribe", "listId": listId }
        operations.push(operation)
        return editor
    }

    editor.apply = function (success, failure) {
        argscheck.checkArgs('FF', methodPrefix + "#apply", arguments)
        perform(nativeMethod, operations, success, failure)
        operations = []
        return editor
    }

    return editor
}

function AttributesEditor(methodPrefix, nativeMethod) {
    var operations = [];
    var editor = {};

    editor.setAttribute = function (name, value) {
        argscheck.checkArgs('s*', methodPrefix + "#setAttribute", arguments)

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
            throw ("Unsupported attribute type: " + typeof value)
        }

        operations.push(operation)

        return editor
    }

    editor.removeAttribute = function (name) {
        argscheck.checkArgs('s', methodPrefix + "#removeAttribute", arguments)
        var operation = { "action": "remove", "key": name }
        operations.push(operation)
        return editor
    }

    editor.apply = function (success, failure) {
        argscheck.checkArgs('FF', methodPrefix + "#apply", arguments)
        perform(nativeMethod, operations, success, failure)
        operations = []
        return editor
    }

    return editor
}


airship.takeOff = function (config, success, failure) {
    argscheck.checkArgs("*FF", "Airship.takeOff", arguments);
    perform("takeOff", config, success, failure)
}

airship.isFlying = function (success, failure) {
    argscheck.checkArgs("fF", "Airship.isFlying", arguments);
    perform("isFlying", null, success, failure)
}

airship.onDeepLink = function (callback) {
    argscheck.checkArgs('F', 'Airship.onDeepLink', arguments)
    return registerListener("airship.event.deep_link_received", callback)
}

// Channel

airship.channel.getChannelId = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.channel.getChannelId', arguments)
    perform("channel#getChannelId", null, success, failure)
}

airship.channel.getSubscriptionLists = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.channel.getSubscriptionLists', arguments)
    perform("channel#getSubscriptionLists", null, success, failure)
}

airship.channel.getTags = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.channel.getTags', arguments)
    perform("channel#getTags", null, success, failure)
}


airship.channel.editTags = function () {
    return new TagEditor('Airship.channel.editTags', 'channel#editTags')
}

airship.channel.editTagGroups = function () {
    return new TagGroupEditor('Airship.channel.editTagGroups', 'channel#editTagGroups')
}

airship.channel.editAttributes = function () {
    return new AttributesEditor('Airship.channel.editAttributes', 'channel#editAttributes')
}

airship.channel.editSubscriptionLists = function () {
    return new SubscriptionListEditor('Airship.channel.editSubscriptionLists', 'channel#editSubscriptionLists')
}

airship.channel.onChannelCreated = function (callback) {
    argscheck.checkArgs('F', 'Airship.channel.channel_created', arguments)
    return registerListener("airship.event.channel_created", callback)
}

airship.channel.enableChannelCreation = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.channel.enableChannelCreation', arguments)
    perform("channel#enableChannelCreation", null, success, failure)
}

// Contact

airship.contact.getNamedUserId = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.contact.getNamedUserId', arguments)
    perform("contact#getNamedUserId", null, success, failure)
}

airship.contact.identify = function (namedUserId, success, failure) {
    argscheck.checkArgs('SFF', 'Airship.contact.identify', arguments)
    perform("contact#identify", namedUserId, success, failure)
}

airship.contact.reset = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.contact.reset', arguments)
    perform("contact#reset", null, success, failure)
}

airship.contact.notifyRemoteLogin = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.contact.notifyRemoteLogin', arguments)
    perform("contact#notifyRemoteLogin", null, success, failure)
}

airship.contact.getSubscriptionLists = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.contact.getSubscriptionLists', arguments)
    perform("contact#getSubscriptionLists", null, success, failure)
}

airship.contact.editTagGroups = function () {
    return new TagGroupEditor('Airship.contact.editTagGroups', 'contact#editTagGroups')
}

airship.contact.editAttributes = function () {
    return new AttributesEditor('Airship.contact.editAttributes', 'contact#editAttributes')
}

airship.contact.editSubscriptionLists = function () {
    return new ScopedSubscriptionListEditor('Airship.contact.editSubscriptionLists', 'contact#editSubscriptionLists')
}

// Push

airship.push.enableUserNotifications = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.enableUserNotifications', arguments)
    perform("push#enableUserNotifications", null, success, failure)
}

airship.push.isUserNotificationsEnabled = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.isUserNotificationsEnabled', arguments)
    perform("push#isUserNotificationsEnabled", null, success, failure)
}

airship.push.setUserNotificationsEnabled = function (enabled, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.setUserNotificationsEnabled', arguments)
    perform("push#setUserNotificationsEnabled", !!enabled, success, failure)
}

airship.push.getNotificationStatus = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.getNotificationStatus', arguments)
    perform("push#getNotificationStatus", null, success, failure)
}

airship.push.getPushToken = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.getPushToken', arguments)
    perform("push#getPushToken", null, success, failure)
}

airship.push.clearNotifications = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.push.clearNotifications', arguments)
    perform("push#clearNotifications", null, success, failure)
}

airship.push.clearNotification = function (id, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.push.clearNotification', arguments)
    perform("push#clearNotification", id, success, failure)
}

airship.push.getActiveNotifications = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.getActiveNotifications', arguments)
    perform("push#getActiveNotifications", null, success, failure)
}

airship.push.onNotificationStatusChanged = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.onNotificationStatusChanged', arguments)
    return registerListener("airship.event.notification_status_changed", callback)
}

airship.push.onPushTokenReceived = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.onPushTokenReceived', arguments)
    return registerListener("airship.event.push_token_received", callback)
}

airship.push.onPushReceived = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.onPushReceived', arguments)
    return registerListener("airship.event.push_received", callback)
}

airship.push.onNotificationResponse = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.onNotificationResponse', arguments)
    return registerListener("airship.event.notification_response", callback)
}

airship.push.onNotificationReceived = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.onNotificationReceived', arguments)
    // return registerListener("airship.event.<TODO>>", callback)
}

// Push Android

airship.push.android.setForegroundNotificationsEnabled = function (enabled, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.android.setForegroundNotificationsEnabled', arguments)
    perform("push#android#setForegroundNotificationsEnabled", !!enabled, success, failure)
}

airship.push.android.isForegroundNotificationsEnabled = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.android.isForegroundNotificationsEnabled', arguments)
    perform("push#android#isForegroundNotificationsEnabled", null, success, failure)
}

airship.push.android.isNotificationChannelEnabled = function (channel, success, failure) {
    argscheck.checkArgs('sfF', 'Airship.push.android.isNotificationChannelEnabled', arguments)
    perform("push#android#isNotificationChannelEnabled", channel, success, failure)
}

airship.push.android.setNotificationConfig = function (config, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.android.isNotificationChannelEnabled', arguments)
    perform("push#android#setNotificationConfig", config, success, failure)
}

// Push iOS

airship.push.ios.setQuietTimeEnabled = function(enabled, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.setQuietTimeEnabled', arguments)
    perform("push#ios#setQuietTimeEnabled", !!enabled, success, failure)
}

airship.push.ios.isQuietTimeEnabled = function(success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.isQuietTimeEnabled', arguments)
    perform("push#ios#isQuietTimeEnabled", null, success, failure)
}

airship.push.ios.setQuietTime =function(quietTime, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.setQuietTime', arguments)
    perform("push#ios#setQuietTime", quietTime, success, failure)
}

airship.push.ios.getQuietTime = function(success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.getQuietTime', arguments)
    perform("push#ios#getQuietTime", null, success, failure)
}

airship.push.ios.isAutobadgeEnabled = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.isAutobadgeEnabled', arguments)
    perform("push#ios#isAutobadgeEnabled", null, success, failure)
}

airship.push.ios.setAutobadgeEnabled = function (enabled, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.setAutobadgeEnabled', arguments)
    perform("push#ios#setAutobadgeEnabled", !!enabled, success, failure)
}

airship.push.ios.setForegroundPresentationOptions = function (options, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.setForegroundPresentationOptions', arguments)
    perform("push#ios#setForegroundPresentationOptions", options, success, failure)
}

airship.push.ios.setNotificationOptions = function (options, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.setNotificationOptions', arguments)
    perform("push#ios#setNotificationOptions", options, success, failure)
}

airship.push.ios.setBadgeNumber = function (badge, success, failure) {
    argscheck.checkArgs('nFF', 'Airship.push.ios.setBadgeNumber', arguments)
    perform("push#ios#setBadgeNumber", badge, success, failure)
}

airship.push.ios.getBadgeNumber = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.getBadgeNumber', arguments)
    perform("push#ios#getBadgeNumber", null, success, failure)
}

airship.push.ios.getAuthorizedNotificationSettings = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.getAuthorizedNotificationSettings', arguments)
    perform("push#ios#getAuthorizedNotificationSettings", null, success, failure)
}

airship.push.ios.getAuthorizedNotificationStatus = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.ios.getAuthorizedNotificationStatus', arguments)
    perform("push#ios#getAuthorizedNotificationStatus", null, success, failure)
}

airship.push.ios.resetBadge = function (success, failure) {
    argscheck.checkArgs('*FF', 'Airship.push.ios.resetBadge', arguments)
    perform("push#ios#resetBadge", null, success, failure)
}

airship.push.ios.onAuthorizedSettingsChanged = function (callback) {
    argscheck.checkArgs('F', 'Airship.push.ios.onAuthorizedSettingsChanged', arguments)
    return registerListener("airship.event.ios_authorized_notification_settings_changed", callback)
}


// Privacy Manager

airship.privacyManager.isFeaturesEnabled = function (features, success, failure) {
    argscheck.checkArgs('afF', 'Airship.push.isFeaturesEnabled', arguments)
    perform("privacyManager#isFeaturesEnabled", features, success, failure)
}

airship.privacyManager.setEnabledFeatures = function (features, success, failure) {
    argscheck.checkArgs('aFF', 'Airship.push.setEnabledFeatures', arguments)
    perform("privacyManager#setEnabledFeatures", features, success, failure)
}

airship.privacyManager.enableFeatures = function (features, success, failure) {
    argscheck.checkArgs('aFF', 'Airship.push.enableFeatures', arguments)
    perform("privacyManager#enableFeatures", features, success, failure)
}

airship.privacyManager.disableFeatures = function (features, success, failure) {
    argscheck.checkArgs('aFF', 'Airship.push.disableFeatures', arguments)
    perform("privacyManager#disableFeatures", features, success, failure)
}

airship.privacyManager.getEnabledFeatures = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.push.getEnabledFeatures', arguments)
    perform("privacyManager#getEnabledFeatures", null, success, failure)
}

// Message Center

airship.messageCenter.getUnreadCount = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.messageCenter.getUnreadCount', arguments)
    perform("messageCenter#getUnreadCount", null, success, failure)
}

airship.messageCenter.getMessages = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.messageCenter.getMessages', arguments)
    perform("messageCenter#getMessages", null, success, failure)
}

airship.messageCenter.markMessageRead = function (messageId, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.messageCenter.markMessageRead', arguments)
    perform("messageCenter#markMessageRead", messageId, success, failure)
}

airship.messageCenter.deleteMessage = function (messageId, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.messageCenter.deleteMessage', arguments)
    perform("messageCenter#deleteMessage", messageId, success, failure)
}

airship.messageCenter.dismiss = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.messageCenter.dismiss', arguments)
    perform("messageCenter#dismiss", null, success, failure)
}

airship.messageCenter.display = function (messageId, success, failure) {
    argscheck.checkArgs('SFF', 'Airship.messageCenter.display', arguments)
    perform("messageCenter#display", messageId, success, failure)
}

airship.messageCenter.showMessageView = function (messageId, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.messageCenter.showMessageView', arguments)
    perform("messageCenter#showMessageView", messageId, success, failure)
}

airship.messageCenter.refreshMessages = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.messageCenter.refreshMessages', arguments)
    perform("messageCenter#refreshMessages", null, success, failure)
}

airship.messageCenter.setAutoLaunchDefaultMessageCenter = function (autoLaunch, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.messageCenter.setAutoLaunchDefaultMessageCenter', arguments)
    perform("messageCenter#setAutoLaunchDefaultMessageCenter", !!autoLaunch, success, failure)
}

airship.messageCenter.onUpdated = function (callback) {
    argscheck.checkArgs('F', 'Airship.messageCenter.onUpdated', arguments)
    return registerListener("airship.event.message_center_updated", callback)
}

airship.messageCenter.onDisplay = function (callback) {
    argscheck.checkArgs('F', 'Airship.messageCenter.onDisplay', arguments)
    return registerListener("airship.event.display_message_center", callback)
}


// Preference Center

airship.preferenceCenter.display = function (preferenceCenterId, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.preferenceCenter.display', arguments)
    perform("preferenceCenter#display", preferenceCenterId, success, failure)
}

airship.preferenceCenter.getConfig = function (preferenceCenterId, success, failure) {
    argscheck.checkArgs('sfF', 'Airship.preferenceCenter.getConfig', arguments)
    perform("preferenceCenter#getConfig", preferenceCenterId, success, failure)
}

airship.preferenceCenter.setAutoLaunchDefaultPreferenceCenter = function (preferenceCenterId, autoLaunch, success, failure) {
    argscheck.checkArgs('s*FF', 'Airship.preferenceCenter.setAutoLaunchDefaultPreferenceCenter', arguments)
    perform("preferenceCenter#setAutoLaunchDefaultPreferenceCenter", [preferenceCenterId, !!autoLaunch], success, failure)
}

airship.preferenceCenter.onDisplay = function (callback) {
    argscheck.checkArgs('F', 'Airship.preferenceCenter.onDisplay', arguments)
    return registerListener("airship.event.display_preference_center", callback)
}

// Analytics

airship.analytics.trackScreen = function (screen, success, failure) {
    argscheck.checkArgs('SFF', 'Airship.analytics.trackScreen', arguments)
    perform("analytics#trackScreen", screen, success, failure)
}

airship.analytics.associateIdentifier = function (key, value, success, failure) {
    argscheck.checkArgs('sSFF', 'Airship.analytics.associateIdentifier', arguments)
    perform("analytics#associateIdentifier", [key, value], success, failure)
}

airship.analytics.addCustomEvent = function (event, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.analytics.addCustomEvent', arguments)
    perform("analytics#addCustomEvent", event, success, failure)
}

// Actions

airship.actions.run = function (name, value, success, failure) {
    argscheck.checkArgs('s*FF', 'Airship.actions.run', arguments)
    perform("actions#run", [name, value], success, failure)
}

/// Feature Flags

airship.featureFlagManager.flag = function (name, success, failure) {
    argscheck.checkArgs('sfF', 'Airship.featureFlagManager.flag', arguments)
    perform("featureFlagManager#flag", name, success, failure)
}

airship.featureFlagManager.trackInteraction = function (flag, success, failure) {
    argscheck.checkArgs('*FF', 'Airship.featureFlagManager.trackInteraction', arguments)
    perform("featureFlagManager#trackInteraction", flag, success, failure)
}

/// In App

airship.inApp.setPaused = function (paused, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.inApp.setPaused', arguments)
    perform("inApp#setPaused", !!paused, success, failure)
}


airship.inApp.isPaused = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.inApp.isPaused', arguments)
    perform("inApp#isPaused", null, success, failure)
}

airship.inApp.setDisplayInterval = function (interval, success, failure) {
    argscheck.checkArgs('nFF', 'Airship.inApp.setDisplayInterval', arguments)
    perform("inApp#setDisplayInterval", interval, success, failure)
}

airship.privacyManager.getDisplayInterval = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.inApp.getDisplayInterval', arguments)
    perform("inApp#getDisplayInterval", null, success, failure)
}

/// Locale

airship.locale.setLocaleOverride = function (locale, success, failure) {
    argscheck.checkArgs('sFF', 'Airship.inApp.setLocaleOverride', arguments)
    perform("locale#setLocaleOverride", locale, success, failure)
}


airship.locale.clearLocaleOverride = function (success, failure) {
    argscheck.checkArgs('FF', 'Airship.inApp.clearLocaleOverride', arguments)
    perform("locale#clearLocaleOverride", null, success, failure)
}

airship.locale.getLocale = function (success, failure) {
    argscheck.checkArgs('fF', 'Airship.locale.getLocale', arguments)
    perform("locale#getLocale", null, success, failure)
}

module.exports = airship;