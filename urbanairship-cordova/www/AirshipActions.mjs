/* Copyright Urban Airship and Contributors */

export class AirshipActions {
    
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
     * @module AirshipActions
     */
    module.exports = {
        
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
    }
        
    }
    
}
