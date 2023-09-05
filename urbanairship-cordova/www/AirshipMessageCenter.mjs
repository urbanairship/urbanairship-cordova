/* Copyright Urban Airship and Contributors */

export class AirshipMessageCenter {
    
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
     * @module AirshipMessageCenter
     */
    module.exports = {
        
        
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
    }
        
    }
    
}
