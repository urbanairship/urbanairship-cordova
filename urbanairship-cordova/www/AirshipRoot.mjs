/* Copyright Urban Airship and Contributors */

import { AirshipActions } from './AirshipActions';
import { AirshipAnalytics } from './AirshipAnalytics';
import { AirshipChannel } from './AirshipChannel';
import { AirshipContact } from './AirshipContact';
import { AirshipInApp } from './AirshipInApp';
import { AirshipLocale } from './AirshipLocale';
import { AirshipMessageCenter } from './AirshipMessageCenter';
import { AirshipPreferenceCenter } from './AirshipPreferenceCenter';
import { AirshipPrivacyManager } from './AirshipPrivacyManager';
import { AirshipPush } from './AirshipPush';
//import { AirshipConfig, EventTypeMap, EventType } from './types';
//import { Subscription, UAEventEmitter } from './UAEventEmitter';

export class AirshipRoot {

    public readonly actions: AirshipActions;
    public readonly analytics: AirshipAnalytics;
    public readonly channel: AirshipChannel;
    public readonly contact: AirshipContact;
    public readonly inApp: AirshipInApp;
    public readonly locale: AirshipLocale;
    public readonly messageCenter: AirshipMessageCenter;
    public readonly preferenceCenter: AirshipPreferenceCenter;
    public readonly privacyManager: AirshipPrivacyManager;
    public readonly push: AirshipPush;

      constructor(private readonly module: any) {
          this.actions = new AirshipActions(module);
          this.analytics = new AirshipAnalytics(module);
          this.channel = new AirshipChannel(module);
          this.contact = new AirshipContact(module);
          this.inApp = new AirshipInApp(module);
          this.locale = new AirshipLocale(module);
          this.messageCenter = new AirshipMessageCenter(module);
          this.preferenceCenter = new AirshipPreferenceCenter(module);
          this.privacyManager = new AirshipPrivacyManager(module);
          this.push = new AirshipPush(module);
          this.eventEmitter = new UAEventEmitter(module);
        }

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
    
    
    function bindDocumentEvent() {
        callNative(function(e) {
            console.log("Firing document event: " + e.eventType)
            cordova.fireDocumentEvent(e.eventType, e.eventData)
        }, null, "registerListener")
    }
    
   // document.addEventListener("deviceready", bindDocumentEvent, false)
    

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
         * @param {string}  config.messageCenterStyleConfig The message center style config file. By default it's "messageCenterStyleConfig"
         * @param {object}  config.development The Urban Airship development config.
         * @param {string}  config.development.appKey The development appKey.
         * @param {string}  config.development.appSecret The development appSecret.
         * @param {object}  config.production The Urban Airship production config.
         * @param {string}  config.production.appKey The production appKey.
         * @param {string}  config.production.appSecret The production appSecret.
         */
    public takeOff: function(config, success, failure) {
        argscheck.checkArgs("*FF", "UAirship.takeOff", arguments);
        callNative(success, failure, "takeOff", [config]);
    }
        
        /**
         * Returns the last received deep link.
         *
         * @param {Boolean} clear true to clear the deep link.
         * @param {function(push)} success The function to call on success.
         * @param {string} success.deepLink The deep link.
         * @param {failureCallback} [failure] The function to call on failure.
         * @param {string} failure.message The error message.
         */
    public getDeepLink: function(clear, success, failure) {
        argscheck.checkArgs('*fF', 'UAirship.getDeepLink', arguments)
        callNative(success, failure, "getDeepLink", [!!clear])
    }
        

}
