/* Copyright Urban Airship and Contributors */

export class AirshipContact {
    
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
     * @module AirshipContact
     */
    module.exports = {
        
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
         * Creates an editor to modify the named user tag groups.
         *
         * @return {TagGroupEditor} A tag group editor instance.
         */
    editNamedUserTagGroups: function() {
        return new TagGroupEditor('editNamedUserTagGroups')
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
    }
        
    }
    
}
