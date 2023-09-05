/* Copyright Urban Airship and Contributors */

export class AirshipPreferenceCenter {
    
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
     * @module AirshipPreferenceCenter
     */
    module.exports = {
        
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
    }
        
    }
    
}
