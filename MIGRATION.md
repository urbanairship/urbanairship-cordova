# Migration Guide

## 14.x to 15.x

### Requirements

- cordova-android: 12.x
- cordova-ios: 7.x
- Xcode: 15.2+

### Config.xml Changes

In config.xml, you must set the min deployment target for iOS to 14 and enable swift support:

```
<!-- Deployment target must be >= iOS 14  -->
<preference name="deployment-target" value="14.0" />

<!-- Must be 5.0 -->
<preference name="SwiftVersion" value="5.0" />
```

For Android to enable FCM, enable the Google Services plugin:
```
<preference name="AndroidGradlePluginGoogleServicesEnabled" value="true" />
```


### Package Changes

The npm packages are now published under new names:

| 14.x                           | 15.x                    | Notes                                                                                            |
|--------------------------------|-------------------------|--------------------------------------------------------------------------------------------------|
| urbanairship-cordova           | @ua/cordova-airship     | The plugin id is `cordova-airship`.                                                              |
| urbanairship-cordova-hms       | @ua/cordova-airhsip-hms | The plugin id is `cordova-airship-hms`.                                                          |
| urbanairship-accengage-cordova | removed                 | Package is no longer needed. It was only needed during the transition from Accengage to Airship. |


### API

The public API has been rewritten. Most methods have a one off replacement. See the table below for the method mapping.

| 14.x                                              | 15.x                                                          | Notes                                                                                                                  |
|---------------------------------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| UAirship.takeOff                                  | Airship.takeOff                                               | The option `messageCenterStyleConfig` moved to AirshipConfig.ios.messageCenterStyleConfig.                             |
| UAirship.setAndroidNotificationConfig             | Airship.push.android.setNotificationConfig                    | You can now also android notification config during takeOff                                                            |
| UAirship.setAutoLaunchDefaultMessageCenter        | Airship.messageCenter.setAutoLaunchDefaultMessageCenter       |                                                                                                                        |
| UAirship.displayMessageCenter                     | Airship.messageCenter.display                                 | If `setAutoLaunchDefaultMessageCenter` is enabled, this will show a message center UI. If no, it will generate events. |
| UAirship.dismissMessageCenter                     | Airship.messageCenter.dismiss                                 |                                                                                                                        |
| UAirship.dismissInboxMessage                      | Airship.messageCenter.dismiss                                 |                                                                                                                        |
| UAirship.getInboxMessages                         | Airship.messageCenter.getMessages                             |                                                                                                                        |
| UAirship.markInboxMessageRead                     | Airship.messageCenter.markMessageRead                         |                                                                                                                        |
| UAirship.deleteInboxMessage                       | Airship.messageCenter.deleteMessage                           |                                                                                                                        |
| UAirship.displayInboxMessage                      | Airship.messageCenter.showMessageView                         |                                                                                                                        |
| UAirship.refreshInbox                             | Airship.messageCenter.refreshMessages                         |                                                                                                                        |
| UAirship.setUserNotificationsEnabled              | Airship.push.setUserNotificationsEnabled                      |                                                                                                                        |
| UAirship.isUserNotificationsEnabled               | Airship.push.isUserNotificationsEnabled                       |                                                                                                                        |
| UAirship.enableUserNotifications                  | Airship.push.enableUserNotifications                          |                                                                                                                        |
| UAirship.isAppNotificationsEnabled                | Airship.push.getNotificationStatus                            | Use the flag `areNotificationsAllowed` on the status object                                                            |
| UAirship.isQuietTimeEnabled                       | Airship.push.isQuietTimeEnabled                               |                                                                                                                        |
| UAirship.setQuietTimeEnabled                      | Airship.push.setQuietTimeEnabled                              |                                                                                                                        |
| UAirship.isInQuietTime                            | No replacement                                                | If needed please file a github issue with usage.                                                                       |
| UAirship.setQuietTime                             | Airship.push.setQuietTime                                     | API now takes an object with startHour, endHour, startMinute, endMinute                                                |
| UAirship.getQuietTime                             | Airship.push.getQuietTime                                     |                                                                                                                        |
| UAirship.clearNotification                        | Airship.push.clearNotification                                |                                                                                                                        |
| UAirship.clearNotifications                       | Airship.push.clearNotifications                               |                                                                                                                        |
| UAirship.getActiveNotifications                   | Airship.push.getActiveNotifications                           |                                                                                                                        |
| UAirship.setAutobadgeEnabled                      | Airship.push.ios.setAutobadgeEnabled                          |                                                                                                                        |
| UAirship.setBadgeNumber                           | Airship.push.ios.setBadgeNumber                               |                                                                                                                        |
| UAirship.setBadgeNumber                           | Airship.push.ios.setBadgeNumber                               |                                                                                                                        |
| UAirship.getBadgeNumber                           | Airship.push.ios.getBadgeNumber                               |                                                                                                                        |
| UAirship.resetBadge                               | Airship.push.ios.resetBadge                                   |                                                                                                                        |
| UAirship.setNotificationTypes                     | Airship.push.ios.setNotificationOptions                       |                                                                                                                        |
| UAirship.setPresentationOptions                   | Airship.push.ios.setForegroundPresentationOptions             |                                                                                                                        |
| UAirship.setAndroidForegroundNotificationsEnabled | Airship.push.android.setForegroundNotificationsEnabled        |                                                                                                                        |
| UAirship.isSoundEnabled                           | No replacement                                                | Use notification categories/channel instead                                                                            |
| UAirship.setSoundEnabled                          | No replacement                                                | Use notification categories/channel instead                                                                            |
| UAirship.isVibrateEnabled                         | No replacement                                                | Use notification categories/channel instead                                                                            |
| UAirship.setVibrateEnabled                        | No replacement                                                | Use notification categories/channel instead                                                                            |
| UAirship.setAnalyticsEnabled                      | Airship.privacyManager.enable/disable                         | Enable/disable "analytics" flag on privacy manager                                                                     |
| UAirship.isAnalyticsEnabled                       | Airship.privacyManager.isFeaturesEnabled("analytics")         |                                                                                                                        |
| UAirship.setAssociatedIdentifier                  | Airship.analytics.setAssociatedIdentifier                     |                                                                                                                        |
| UAirship.addCustomEvent                           | Airship.analytics.addCustomEvent                              | The field `name` is now `eventName`, `value` is `eventValue`, `properties` can be any valid json object.               |
| UAirship.trackScreen                              | Airship.analytics.trackScreen                                 |                                                                                                                        |
| UAirship.getChannelID                             | Airship.channel.getChannelId                                  |                                                                                                                        |
| UAirship.getTags                                  | Airship.channel.getTags                                       |                                                                                                                        |
| UAirship.setTags                                  | Airship.channel.editTags                                      | Use the editor to add and remove tags                                                                                  |
| UAirship.editChannelTagGroups                     | Airship.channel.editTagGroups                                 |                                                                                                                        |
| UAirship.editChannelAttributes                    | Airship.channel.editAttributes                                |                                                                                                                        |
| UAirship.editChannelSubscriptionLists             | Airship.channel.editSubscriptionLists                         |                                                                                                                        |
| UAirship.getChannelSubscriptionLists              | Airship.channel.getSubscriptionLists                          |                                                                                                                        |
| UAirship.getAlias                                 | Airship.contact.getNamedUserId                                |                                                                                                                        |
| UAirship.setAlias                                 | Airship.contact.identify                                      |                                                                                                                        |
| UAirship.getNamedUser                             | Airship.contact.getNamedUserId                                |                                                                                                                        |
| UAirship.setNamedUser                             | Airship.contact.identify/reset                                | Use identify to set the named user, reset to clear it                                                                  |
| UAirship.editNamedUserTagGroups                   | Airship.contact.editTagGroups                                 |                                                                                                                        |
| UAirship.editNamedUserAttributes                  | Airship.contact.editAttributes                                |                                                                                                                        |
| UAirship.editContactSubscriptionLists             | Airship.contact.editSubscriptionLists                         |                                                                                                                        |
| UAirship.getContactSubscriptionLists              | Airship.contact.getSubscriptionLists                          |                                                                                                                        |
| UAirship.getLaunchNotification                    | No replacement                                                | Use the Airship.push.onNotificationResponse listener                                                                   |
| UAirship.getDeepLink                              | No replacement                                                | Use the Airship.onDeepLink listener                                                                                    |
| UAirship.runAction                                | Airship.actions.run                                           |                                                                                                                        |
| UAirship.enableFeature                            | Airship.privacyManager.enableFeature                          | Feature constants, see below for more info.                                                                            |
| UAirship.disableFeature                           | Airship.privacyManager.disableFeature                         | Feature constants, see below for more info.                                                                            |
| UAirship.setEnabledFeatures                       | Airship.privacyManager.setEnabledFeatures                     | Feature constants, see below for more info.                                                                            |
| UAirship.getEnabledFeatures                       | Airship.privacyManager.getEnabledFeatures                     | Feature constants, see below for more info.                                                                            |
| UAirship.isFeatureEnabled                         | Airship.privacyManager.isFeatureEnabled                       | Feature constants, see below for more info.                                                                            |
| UAirship.openPreferenceCenter                     | Airship.preferenceCenter.display                              |                                                                                                                        |
| UAirship.getPreferenceCenterConfig                | Airship.preferenceCenter.getConfig                            |                                                                                                                        |
| UAirship.setUseCustomPreferenceCenterUi           | Airship.preferenceCenter.setAutoLaunchDefaultPreferenceCenter |                                                                                                                        |
| UAirship.setCurrentLocale                         | Airship.locale.setLocaleOverride                              |                                                                                                                        |
| UAirship.getCurrentLocale                         | Airship.locale.getLocale                                      |                                                                                                                        |
| UAirship.clearLocale                              | Airship.locale.clearLocaleOverride                            |                                                                                                                        |
| UAirship.reattach                                 | No replacement				                                | Events are no longer sent on the document. See events for replacements                                                 |

### Privacy Manager Flags

The flag constants are no longer all uppercase and some options have been removed. The 15.x flags are:

```
/**
 * Enum of authorized Features.
 */
export enum Feature {
    InAppAutomation = 'in_app_automation',
    MessageCenter = 'message_center',
    Push = 'push',
    Analytics = 'analytics',
    TagsAndAttributes = 'tags_and_attributes',
    Contacts = 'contacts',
}
```

### Events

Events are no longer sent as document events. Events are also now queued up until a listener is added, so there is no longer a need to return `launchNotification` or `deepLink` since its now possible for the app to receive those when ready.

| 14.x                                                                           | 15.x                                          | Notes                                           |
|--------------------------------------------------------------------------------|-----------------------------------------------|-------------------------------------------------|
| document.addEventListener("urbanairship.deep_link", callback)                  | Airship.onDeepLink(callback)                  |                                                 |
| document.addEventListener("urbanairship.registration", callback)               | Airship.channel.onChannelCreated(callback)    | For channel ID. Use `event.channelId`           |
| document.addEventListener("urbanairship.registration", callback)               | Airship.push.onPushTokenReceived(callback)    | For push token.  Use `event.pushToken`          |
| document.addEventListener("urbanairship.push", callback)                       | Airship.push.onPushReceived(callback)         | The event has changed, see below for more info. |
| document.addEventListener("urbanairship.notification_opened", callback)        | Airship.push.onNotificationReceived(callback) | The event has changed, see below for more info. |
| document.addEventListener("urbanairship.notification_opt_in_status", callback) | Airship.onNotificationStatusChanged(callback) | The event has changed, see below for more info. |
| document.addEventListener("urbanairship.inbox_updated", callback)              | Airship.messageCenter.onUpdated(callback)     |                                                 |
| document.addEventListener("urbanairship.show_inbox", callback)                 | Airship.messageCenter.onDisplay(callback)     |                                                 |
| document.addEventListener("urbanairship.open_preference_center", callback)     | Airship.preferenceCenter.onDisplay(callback)  |                                                 |

#### Push Received

The push payload is now grouped under the `pushPayload` field. The old `message` field has been renamed to `alert` (`pushPayload.alert`). 

#### Push Response

The push payload is now grouped under the `pushPayload` field. The old `message` field has been renamed to `alert` (`pushPayload.alert`). The field `actionID` has been renamed to `actionId`.


#### Notification Opt In Status Event

Notification status event now provides an object with several flags that you can use to determine the exact reason why the device is opted out:

```
/**
 * Push notification status.
 */
export interface PushNotificationStatus {
    /**
     * If user notifications are enabled on [Airship.push].
     */
    isUserNotificationsEnabled: boolean;

    /**
     * If notifications are allowed at the system level for the application.
     */
    areNotificationsAllowed: boolean;

    /**
     * If the push feature is enabled on [Airship.privacyManager].
     */
    isPushPrivacyFeatureEnabled: boolean;

    /*
     * If push registration was able to generate a token.
     */
    isPushTokenRegistered: boolean;

    /*
     * If Airship is able to send and display a push notification.
     */
    isOptedIn: boolean;

    /*
     * Checks for isUserNotificationsEnabled, areNotificationsAllowed, and isPushPrivacyFeatureEnabled. If this flag
     * is true but `isOptedIn` is false, that means push token was not able to be registered. 
     */
    isUserOptedIn: boolean;
}

/**
 * Event fired when the notification status changes.
 */
export interface PushNotificationStatusChangedEvent {
    /**
     * The push notification status.
     */
    status: PushNotificationStatus
}
```