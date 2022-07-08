/* Copyright Urban Airship and Contributors */

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#import "AirshipMessageCenterLib.h"
#import "AirshipAutomationLib.h"
#else
@import AirshipKit;
#endif

/**
 * The Urban Airship Cordova plugin.
 */
@interface UAirshipPlugin : CDVPlugin

/**
 * Sets the Urban Airship config and attempts takeOff.
 *
 * Expected arguments: NSDictionary
 *
 * @param command The cordova command.
 */
- (void)takeOff:(CDVInvokedUrlCommand *)command;

/**
 * Sets the default behavior when the message center is launched from a push
 * notification. If set to false the message center must be manually launched.
 *
 * Expected arguments: Boolean
 *
 * @param command The cordova command.
 */
- (void)setAutoLaunchDefaultMessageCenter:(CDVInvokedUrlCommand *)command;

/**
 * Enables or disables user push notifications.
 *
 * Expected arguments: Boolean
 *
 * @param command The cordova command.
 */
- (void)setUserNotificationsEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Enables user push notifications.
 *
 * @param command The cordova command.
 */
- (void)enableUserNotifications:(CDVInvokedUrlCommand *)command;

/**
 * Checks if user push notifications are enabled or not.
 *
 * @param command The cordova command.
 */
- (void)isUserNotificationsEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Returns the last notification that launched the application.
 *
 * Expected arguments: Boolean - `YES` to clear the notification.
 *
 * @param command The cordova command.
 */
- (void)getLaunchNotification:(CDVInvokedUrlCommand *)command;

/**
 * Returns the last received deep link.
 *
 * Expected arguments: Boolean - `YES` to clear the deep link.
 *
 * @param command The cordova command.
 */
- (void)getDeepLink:(CDVInvokedUrlCommand *)command;

/**
 * Returns the channel ID.
 *
 * @param command The cordova command.
 */
- (void)getChannelID:(CDVInvokedUrlCommand *)command;

/**
 * Returns the tags as an array.
 *
 * @param command The cordova command.
 */
- (void)getTags:(CDVInvokedUrlCommand *)command;

/**
 * Sets the tags.
 *
 * Expected arguments: An array of Strings
 *
 * @param command The cordova command.
 */
- (void)setTags:(CDVInvokedUrlCommand *)command;

/**
 * Returns the current badge number.
 *
 * @param command The cordova command.
 */
- (void)getBadgeNumber:(CDVInvokedUrlCommand *)command;

/**
 * Enables or disables auto badge. Defaults to `NO`.
 *
 * Expected arguments: Boolean
 *
 * @param command The cordova command.
 */
- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Sets the badge number.
 *
 * Expected arguments: Number
 *
 * @param command The cordova command.
 */
- (void)setBadgeNumber:(CDVInvokedUrlCommand *)command;

/**
 * Clears the badge.
 *
 * @param command The cordova command.
 */
- (void)resetBadge:(CDVInvokedUrlCommand *)command;

/**
 * Sets the named user ID.
 *
 * Expected arguments: String
 *
 * @param command The cordova command.
 */
- (void)setNamedUser:(CDVInvokedUrlCommand *)command;

/**
 * Returns the named user ID.
 *
 * Expected arguments: String
 *
 * @param command The cordova command.
 */
- (void)getNamedUser:(CDVInvokedUrlCommand *)command;

/**
 * Enables or disables quiet time.
 *
 * Expected arguments: Boolean
 *
 * @param command The cordova command.
 */
- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Checks if quiet time is currently enabled.
 *
 * @param command The cordova command.
 */
- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Sets the quiet time.
 *
 * Expected arguments: Number - start hour, Number - start minute,
 * Number - end hour, Number - end minute
 *
 * @param command The cordova command.
 */
- (void)setQuietTime:(CDVInvokedUrlCommand *)command;

/**
 * Returns the quiet time as an object with the following:
 * "startHour": Number,
 * "startMinute": Number,
 * "endHour": Number,
 * "endMinute": Number
 *
 * @param command The cordova command.
 */
- (void)getQuietTime:(CDVInvokedUrlCommand *)command;

/**
 * Checks if the device is currently in quiet time.
 *
 * @param command The cordova command.
 */
- (void)isInQuietTime:(CDVInvokedUrlCommand *)command;

/**
 * Sets the user notification types. Defaults to all notification types.
 *
 * Expected arguments: Number - bitmask of the notification types
 *
 * @param command The cordova command.
 */
- (void)setNotificationTypes:(CDVInvokedUrlCommand *)command;

/**
 * Sets notification presentation options.
 *
 * Expected arguments: Number - bitmask of the notification options
 *
 * @param command The cordova command.
 */
- (void)setPresentationOptions:(CDVInvokedUrlCommand *)command;

/**
 * Enables or disables analytics.
 *
 * Disabling analytics will delete any locally stored events
 * and prevent any events from uploading. Features that depend on analytics being
 * enabled may not work properly if it's disabled (reports, region triggers,
 * location segmentation, push to local time).
 *
 * Expected arguments: Boolean
 *
 * @param command The cordova command.
 */
- (void)setAnalyticsEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Sets associated custom identifiers for use with the Connect data stream.
 *
 * Previous identifiers will be replaced by the new identifiers each time setAssociateIdentifier is called. It is a set operation.
 *
 * Expected arguments: An array of strings containing the identifier and key.
 *
 * @param command The cordova command.
 */
- (void)setAssociatedIdentifier:(CDVInvokedUrlCommand *)command;

/**
 * Checks if analytics is enabled or not.
 *
 * @param command The cordova command.
 */
- (void)isAnalyticsEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Runs an Urban Airship action.
 *
 * Expected arguments: String - action name, * - the action value
 *
 * @param command The cordova command.
 */
- (void)runAction:(CDVInvokedUrlCommand *)command;

/**
 * Edits the named user tag groups.
 *
 * Expected arguments: An array of objects that contain:
 * "operation": String, either "add" or "remove",
 * "group": String, the tag group,
 * "tags": Array of tags
 *
 * @param command The cordova command.
 */
- (void)editNamedUserTagGroups:(CDVInvokedUrlCommand *)command;

/**
 * Edits the channel tag groups.
 *
 * Expected arguments: An array of objects that contain:
 * "operation": String, either "add" or "remove",
 * "group": String, the tag group,
 * "tags": Array of tags
 *
 * @param command The cordova command.
 */
- (void)editChannelTagGroups:(CDVInvokedUrlCommand *)command;

/**
 * Edits the channel attributes.
 *
 * Expected arguments: An array of objects that contain:
 * "action": String, either `remove` or `set`
 * "key": String, the attribute name.
 * "value": String, the attribute value.
 *
 * @param command The cordova command.
 */
- (void)editChannelAttributes:(CDVInvokedUrlCommand *)command;

/**
 * Edits the named user attributes.
 *
 * Expected arguments: An array of objects that contain:
 * "action": String, either `remove` or `set`
 * "key": String, the attribute name.
 * "value": String, the attribute value.
 *
 * @param command The cordova command.
 */
- (void)editNamedUserAttributes:(CDVInvokedUrlCommand *)command;

/**
 * Registers a listener for events.
 *
 * @param command The cordova command.
 */
- (void)registerListener:(CDVInvokedUrlCommand *)command;

/**
 * Display the given message without animation.
 *
 * @param command The cordova command.
 */
- (void)displayMessageCenter:(CDVInvokedUrlCommand *)command;

/**
 * Dismiss the message center.
 *
 * @param command The cordova command.
 */
- (void)dismissMessageCenter:(CDVInvokedUrlCommand *)command;

/**
 * Gets the inbox listing.
 *
 * @param command The cordova command.
 */
- (void)getInboxMessages:(CDVInvokedUrlCommand *)command;

/**
 * Marks an inbox message read.
 *
 * Expected arguments: String - message ID.
 *
 * @param command The cordova command.
 */
- (void)markInboxMessageRead:(CDVInvokedUrlCommand *)command;

/**
 * Deletes an inbox message.
 *
 * Expected arguments: String - message ID.
 *
 * @param command The cordova command.
 */
- (void)deleteInboxMessage:(CDVInvokedUrlCommand *)command;

/**
 * Displays an inbox message.
 *
 * Expected arguments: String - message ID.
 *
 * @param command The cordova command.
 */
- (void)displayInboxMessage:(CDVInvokedUrlCommand *)command;

/**
 * Dismiss an inbox message.
 *
 * @param command The cordova command.
 */
- (void)dismissInboxMessage:(CDVInvokedUrlCommand *)command;

/**
 * Refreshes the inbox.
 *
 * @param command The cordova command.
 */
- (void)refreshInbox:(CDVInvokedUrlCommand *)command;

/**
 * Checks if app notifications are enabled or not.
 *
 * @param command The cordova command.
 */
- (void)isAppNotificationsEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Gets the currently active notifications.
 *
 * @param command The cordova command.
 */
- (void)getActiveNotifications:(CDVInvokedUrlCommand *)command;

/**
 * Clears notifications by identifier.
 *
 * Expected arguments: String - notification identifier.
 *
 * @param command The cordova command.
 */
- (void)clearNotification:(CDVInvokedUrlCommand *)command;

/**
 * Clears all notifications.
 *
 * @param command The cordova command.
 */
- (void)clearNotifications:(CDVInvokedUrlCommand *)command;

/**
 * Enables features, adding them to the set of currently enabled features.
 *
 * Expected arguments: NSArray - the features.
 *
 * @param command The cordova command.
 */
- (void)enableFeature:(CDVInvokedUrlCommand *)command;

/**
 * Disables features, removing them from the set of currently enabled features.
 *
 * Expected arguments: NSArray - the features.
 *
 * @param command The cordova command.
 */
- (void)disableFeature:(CDVInvokedUrlCommand *)command;

/**
 * Sets the current enabled features, replacing any currently enabled features with the given set.
 *
 * Expected arguments: NSArray - the features.
 *
 * @param command The cordova command.
 */
- (void)setEnabledFeatures:(CDVInvokedUrlCommand *)command;

/**
 * Gets the current enabled features.
 *
 * @param command The cordova command.
 */
- (void)getEnabledFeatures:(CDVInvokedUrlCommand *)command;

/**
 * Checks if all of the given features are enabled.
 *
 * Expected arguments: NSArray - the features.
 *
 * @param command The cordova command.
 */
- (void)isFeatureEnabled:(CDVInvokedUrlCommand *)command;

/**
 * Opens the Preference Center with the given preferenceCenterId.
 *
 * Expected arguments: String - the preference center id.
 *
 * @param command The cordova command.
 */
- (void)openPreferenceCenter:(CDVInvokedUrlCommand *)command;


/**
 * Gets the configuration of the Preference Center with the given Id trough a callback method.
 *
 * Expected arguments: String - the preference center Id.
 *
 * @param command The cordova command.
 */
- (void)getPreferenceCenterConfig:(CDVInvokedUrlCommand *)command;

/**
 * Set to true of override the preference center UI
 *
 * Expected arguments: An array of objects that contain:
 * "preferenceCenterId": the preference center Id.
 * "userCustomUi": Boolean: true to use your custom preference center otherwise set to false.
 *
 * @param command The cordova command.
 */
- (void)setUseCustomPreferenceCenterUi:(CDVInvokedUrlCommand *)command;

/**
 * Edits channel subscription lists.
 *
 * Expected arguments: An array of objects that contain:
 * "operation": String, either `subscribe` or `unsubscribe`
 * "listId": String, the listID.
 *
 * @param command The cordova command.
 */
- (void)editChannelSubscriptionLists:(CDVInvokedUrlCommand *)command;

/**
 * Edits contact subscription lists.
 *
 * Expected arguments: An array of objects that contain:
 * "operation": String, either `subscribe` or `unsubscribe`
 * "listId": String, the listID.
 * "scope": Defines the channel types that the change applies to
 *
 * @param command The cordova command.
 */
- (void)editContactSubscriptionLists:(CDVInvokedUrlCommand *)command;

/**
 * Returns the current set of subscription lists for the current channel,
 * optionally applying pending subscription list changes that will be applied during the next channel update.
 *
 * @param command The cordova command.
 */
- (void)getChannelSubscriptionLists:(CDVInvokedUrlCommand *)command;

/**
 * Returns the current set of subscription lists for the current contact,
 * optionally applying pending subscription list changes that will be applied during the next contact update.
 *
 * @param command The cordova command.
 */
- (void)getContactSubscriptionLists:(CDVInvokedUrlCommand *)command;

/**
 * Returns the locale currently used by Airship.
 * @param command The cordova command.
 */
- (void)getCurrentLocale:(CDVInvokedUrlCommand *)command;

/**
 * Overrides the locale.
 * @param command The cordova command.
 */
- (void)setCurrentLocale:(CDVInvokedUrlCommand *)command;

/**
 * Resets the current locale.
 *
 * @param command The cordova command.
 */
- (void)clearLocale:(CDVInvokedUrlCommand *)command ;

@end
