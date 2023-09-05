/* Copyright Urban Airship and Contributors */

export class AirshipChannel {
    
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
     * @module AirshipChannel
     */
    module.exports = {
        
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
    }
        
    }
    
}
