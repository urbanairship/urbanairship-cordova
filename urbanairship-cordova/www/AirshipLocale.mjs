/* Copyright Urban Airship and Contributors */

export class AirshipLocale {
    
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
     * @module AirshipLocale
     */
    module.exports = {
        
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
    
}
