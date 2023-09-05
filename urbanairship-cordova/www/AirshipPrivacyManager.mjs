/* Copyright Urban Airship and Contributors */

export class AirshipPrivacyManager {
    
    
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
     * @module AirshipPrivacyManager
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
    }
        
    }
    
}
