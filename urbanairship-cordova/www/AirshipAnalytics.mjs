/* Copyright Urban Airship and Contributors */

export class AirshipAnalytics {
    
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
     * @module AirshipAnalytics
     */
    module.exports = {
        
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
    }
        
    }
    
}
