# Cordova Plugin Changelog

## Version 17.1.0 - March 27, 2025

Minor release that updates the Android SDK to 19.4.0 and the iOS SDK to 19.1.1

### Changes
- Updated Android SDK to [19.4.0](https://github.com/urbanairship/android-library/releases/tag/19.4.0)
- Updated iOS SDK to [19.1.1](https://github.com/urbanairship/ios-library/releases/tag/19.1.1)

## Version 17.0.0 February 11, 2025
Major release that updates the Android Airship SDK to 19.1.0 and iOS Airship SDK to 19.0.3

### Changes
- Updated Android SDK to [19.1.0](https://github.com/urbanairship/android-library/releases/tag/19.1.0).
- Updated iOS SDK to [19.0.3](https://github.com/urbanairship/ios-library/releases/tag/19.0.3).
- iOS requires Xcode 16.2, iOS 15+, and cordova-ios 7.1.0+
- Android requires compileSdkVersion 35+, minSdkVersion 23+, and cordova-android 13.0+

## Version 16.0.0 July 3, 2024
Major release that requires Android Cordova 13.

### Changes
- Updated Airship Android SDK to 18.1.1
- Updated Airship iOS SDK to 18.5.0
- Added iOS logPrivacyLevel that can be set in the environments when calling takeOff

## Version 15.2.4 June 21, 2024
Patch release to fix a regression on iOS with In-App Automations, Scenes, and Surveys ignoring screen, version, and custom event triggers. Apps using those triggers that are on 15.2.3 should update.

### Changes
- Updated iOS SDK to 18.4.1
- Fixed regression with triggers

## Version 15.2.3 June 20, 2024
Patch release with several bug fixes.

### Changes
- Updated iOS SDK to 18.4.0.
- Fixed compatibility with cordova-android@13.

## Version 15.2.2 May 16, 2024
Patch release that updates to latest Airship SDKs.

### Changes
- Updated iOS SDK to 18.2.2

## Version 15.2.1 May 13, 2024
Patch release that updates to latest Airship SDKs and fixes issues with methods that take an optional string parameter on Android.

### Changes
- Updated iOS SDK to 18.2.0
- Updated Android SDK to 17.8.1
- Fixed `Airship.messageCenter.display(null)` and `Airship.analytics.trackScreen(null)` on Android

## Version 15.2.0 April 23, 2024
Minor release with several bug fixes.

### Changes
- Added `isForeground` to push received events to indicate the application state when the push was received.
- Fixed cordova-airship-hms plugin ID mismatch.
- Fixed Android background push received and background notification response events.
- Fixed null result handling on Android to be null instead of the OK status.
- Updated iOS SDK to 18.1.1

## Version 15.1.0 April 18, 2024
Minor release with several bug fixes.

### Changes
- Updated Airship Android SDK to 17.8.0
- Updated Airship iOS SDK to 18.1.0
- Fixed `Airship.push.ios.resetBadge` method binding
- Fixed `Airship.contact.getNamedUserId` method binding
- Fixed plugin not found issue if the app contains a space in the name


## Version 15.0.0  March 6, 2024
Major release with several breaking changes. Apps should use the migration guide to update [Migration Guide](https://github.com/urbanairship/urbanairship-cordova/blob/main/MIGRATION.md)

### Changes
- Requires cordova-android 12.0.0+, cordova-ios 7.0.0+, and Xcode 15.2+
- Updated to iOS SDK 17.8.0
- Updated to Android SDK 17.7.3
- Renamed package from `urbanairship-cordova` to `@ua/cordova-airship`
- Renamed package from `urbanairship-hms-cordova` to `@ua/cordova-airship-hms`
- Removed package from `urbanairship-accengage-cordova`
- Replaces `UAirship` with `Airship` as the root instance.
- Grouped functional apis under new components under the Airship instance: Airship.channel, Airship.push, Airship.push.ios, etc...
- Added types
- Added feature flag support
- Added new events for notification status


## Version 14.11.0 - January 11, 2024
Minor release that updates iOS SDK to 16.12.5 and removes an error message on Huawei devices that have the Play Store installed.

### Changes
- Updated iOS Airship SDK to 16.12.5.
- Removed an error message on Huawei devices that have the Play Store installed.

## Version 14.10.1 - June 23, 2023
Patch release that fixes a crash if takeOff is called with invalid config on Android.

### Changes
- Fix crash due to an invalid config exception on Android

## Version 14.10.0 - June 14, 2023
Minor release that updates the iOS SDK to 16.12.1 and the Android SDK to 16.11.1.

### Changes
- Updated iOS Airship SDK to 16.12.1
- Updated Android Airship SDK to 16.11.1
- Fixed issue with `Delete` button on the InboxMessageView not dismissing the view

## Version 14.9.1 - March 31, 2023
Patch release that fixing Contact update merging order, improves Scene/Survey accessibility and reporting.

### Changes
- Updated iOS Airship SDK to 16.11.3
- Updated Android Airship SDK to 16.9.1
- Fixed Contact update merge order, resolving a Preference Center bug that could lead to unexpected subscription states in some circumstances.
- Improved Scene/Survey accessibility and fixed a reporting bug related to form display events.

## Version 14.9.0 - March 21, 2023
Minor release that adds a new Android config flag `com.urbanairship.android.disable_user_notifications_on_system_opt_out` that will disable user notifications on Airship if not enabled at the system level during app start. Apps can set this to `always` to always do this check, or `once` to apply a one time disable on Airship. 

This new flag is useful for preventing a notification permission prompt if the app previously enabled Airship user notifications on plugin 14.2.0 or older on a Android 33+ device. Most apps should use `once` value in order for Airship to still be able to send user notifications if the end user ops back in through system settings instead of through the app without the App needing to enable user notification on Airship again.

### Changes
- Updated Android SDK to 16.9.0 (compileSdkVersion is now 33).
- Updated iOS SDK to 16.11.2.
- Added new config flag on Android to disable user notifications on startup.
- Fixed enableUserNotifications on Android to hand back the actual result of the prompt instead of always `true`.

## Version 14.7.0 - January 18, 2023
Minor release adding support for styling message center. 

### Changes
- Add support for `messageCenterStyleConfig` from takeOff and `"com.urbanairship.message.center.style.file"` from config.xml.

## Version 14.6.0 - December 7, 2022
Minor release updating Android SDK to 16.8.0 and iOS SDK to 16.10.6. 

### Changes
- Updated Android SDK to 16.8.0.
- Updated iOS SDK to 16.10.6.

## Version 14.5.0 - October 10, 2022
Minor release updating Android SDK to 16.7.5 and iOS SDK to 16.9.4. 

### Changes
- Updated Android SDK to 16.7.5.
- Updated iOS SDK to 16.9.4.

## Version 14.4.0 - September 21, 2022
Minor release updating Android SDK to 16.7.4 and iOS SDK to 16.9.3. 

### Changes
- Updated Android SDK to 16.7.4.
- Updated iOS SDK to 16.9.3.

## Version 14.3.0 - July 19, 2022
Minor release updating Android SDK to 16.6.1 and iOS SDK to 16.8.0. 

### Changes
- Updated Android SDK to 16.6.1.
- Updated iOS SDK to 16.8.0.
- Adds locale override methods: setCurrentLocale, getCurrentLocale and resetLocale.

## Version 14.2.0 - May 23, 2022
Minor release updating Android SDK to 16.5.0 and iOS SDK to 16.7.0 and fixing setting named user to null. 

### Changes
- Updated Android SDK to 16.5.0.
- Updated iOS SDK to 16.7.0.
- Fixed setting named user to null.
- Added actions to the push events.
- Added set method to suppress notifications on Android in the foreground.

## Version 14.1.0 - May 4, 2022
Minor release that updates Airship Android SDK to 16.4.0, and iOS SDK to 16.6.0. These SDK releases fix several issues with Scenes and Surveys. Apps using Scenes & Surveys should update.

### Changes
- Added support for randomizing Survey responses.
- Added subscription list action.
- Added firebase app name to config.
- Updated localizations. All strings within the SDK are now localized in 48 different languages.
- Improved accessibility with OOTB Message Center UI.
- In-App rules will now attempt to refresh before displaying. This change should reduce the chances of showing out of data or cancelled in-app automations, scenes, or surveys when background refresh is disabled.
- Fixed reporting issue with a single page Scene.
- Fixed rendering issues for Scenes & Surveys.
- Fixed deep links that contain invalid characters by encoding those deep links.
- Fixed crash on Android 8 with Scenes & Surveys.
- Fixed Survey attribute storage on Android.
- Fixed silent push events on Android.


## Version 14.0.0 - March 10, 2022
Major release that changes the config returned by `getPreferenceCenterConfig`.

### Changes
- Updated Android SDK to 16.3.3
- Removed setting targetSdkVersion
- Fixed editing subscription lists on iOS
- Fixed `setUseCustomPreferenceCenterUi` on Android
- Added new `enableUserNotifications` that returns a boolean result

## Version 13.3.0 - February 25, 2022
Minor release that updates to latest Airship SDKs and adds support for Preference Center.

### Changes
- Updated iOS SDK to 16.4.0 and Android SDK to 16.3.1
- Added preference center methods: `setUseCustomPreferenceCenterUi`, `getPreferenceCenterConfig`, `editChannelSubscriptionLists`, `editContactSubscriptionLists`, `getChannelSubscriptionLists`, `getContactSubscriptionLists`
- Added `urbanairship.open_preference_center` event

## Version 13.2.0 - February 1, 2022
Minor release that updates the Airship SDKs to 16.2.0

### Changes
- Update iOS & Android Airship SDK to 16.2.0

## Version 13.1.1 - January 11, 2022
Patch release that updates to latest Airship SDKs and fixes an opt-in issue
with iOS when enable_push_onlaunch flag is false/not set.

### Changes
- Updated iOS SDK to 16.1.2
- Updated Android SDK to 16.1.1
- Fixed `enable_push_onlaunch` flag handling on iOS


## Version 13.1.0 - December 2, 2021

Minor release updating iOS SDK to 16.1.1 and Android SDK to 16.1.0

### Changes
- Updated iOS SDK to 16.1.1
- Updated Android SDK to 16.1.0

## Version 13.0.1 - November 5, 2021

Patch release that fixes preferences resetting on iOS when upgrading to 13.0.0. This update will restore old preferences that have not been modified new plugin.

**Apps that have migrated to version 13.0.0 should update. Apps currently on version 12.2.0 and below should only migrate to 13.0.1 to avoid a bug in version 13.0.0.**

### Changes
- Updated iOS SDK to 16.0.2

## Version 13.0.0 - October 26, 2021

**Due to a bug that mishandles persisted SDK settings, apps that are migrating from plugin 12.2.0 or older should avoid this version and instead use 13.0.1 or newer.**

Major release for Airship Android SDK 16.0.0 and iOS SDK 16.0.1.

### Changes
- Updated compile and target sdk version to 31.
- Updated Java source and target compatibility versions to 1.8.
- Xcode 13 is now required.
- Added Privacy Manager methods `enableFeature`, `disableFeature`, `setEnabledFeatures`, `getEnabledFeatures` and `isFeatureEnabled` that replace `getDataCollectionEnabled`, `setDataCollectionEnabled`, `getPushTokenRegistrationEnabled` and `setPushTokenRegistrationEnabled`.
- Support for OOTB Preference Center

See the [Android Migration Guide](https://github.com/urbanairship/android-library/blob/main/documentation/migration/migration-guide-15-16.md) for further details.

## Version 12.2.0 - April 29, 2021

Minor release updating iOS and Android SDK versions to 14.3.1 and 14.3.0, respectively.

### Changes
- Updated iOS SDK to 14.3.1
- Updated Android SDK to 14.3.0
- Added support for HMS with our [HMS module](https://github.com/urbanairship/urbanairship-cordova/tree/main/urbanairship-hms-cordova)

## Version 12.1.1 - February 2, 2021

Patch release updating iOS SDK version 14.2.2.

### Changes
- Fixed attributes updates when the named user has invalid URL characters.
- Fixed accessing UIApplication state on a background queue warning.
- Initial channel creation will wait up to 10 seconds for device token registration.

For more details, see the [iOS CHANGELOG](https://github.com/urbanairship/ios-library/blob/14.2.2/CHANGELOG.md).

## Version 12.1.0 - December 31, 2020

Minor release that adds support for In-App Automation message limits and segments.

### Changes
- Updated iOS SDK to 14.2.1
- Updated Android SDK to 14.1.1


## Version 12.0.1 - September 30, 2020

Patch release updating iOS and Android SDK versions to 14.1.2 and 14.0.1, respectively.

### iOS
- Fixes a crash related to sending In-App Messages through push notifications.
- Fixes a crash in the Airship and AirshipAutomation XCFramework.
For more details, see the [iOS CHANGELOG](https://github.com/urbanairship/ios-library/blob/14.1.2/CHANGELOG.md).

### Android
- Fixes an exception caused by too many alarms being scheduled for in-app automation message intervals on Samsung devices.
For more details, see the [Android CHANGELOG](https://github.com/urbanairship/android-library/blob/14.0.1/CHANGELOG.md).

## Version 12.0.0 - September 17, 2020

Major release for Airship Android SDK 14.0 and iOS SDK 14.1.

### Changes
- Xcode 12 is now required.
- Requires Cordova iOS 6.1.0+, Cordova Android 9.0.0+.
- Fixed conflict with play services with Cordova Android 9.0.0.

## Version 11.0.1 - August 17, 2020
Patch release updating iOS and Android SDK versions to 13.5.4 and 13.3.2, respectively.

### iOS
- Xcode 12 and iOS 14 support.
- In App Automation bugfixes.
- Addresses [Dynamic Type](https://developer.apple.com/documentation/uikit/uifont/scaling_fonts_automatically) build warnings and Message Center Inbox UI issues.
For more details, see the [iOS CHANGELOG](https://github.com/urbanairship/ios-library/blob/13.5.4/CHANGELOG.md).

### Android
- Fixes In-App Automation version triggers to only fire on app updates instead of new installs.
- Fixes ADM registration exceptions that occur on first run and text alignment issues with In-App Automation.
For more details, see the [Android CHANGELOG](https://github.com/urbanairship/android-library/blob/13.3.2/CHANGELOG.md).

## Version 11.0.0 - June 3, 2020
Major release adding support for named user and date attributes.

### Changes
- Added support for named user attributes.
- Added support for date attributes.
- Updated iOS SDK to 13.3.2
- Updated Android SDK to 13.1.2

## Version 10.1.2 - May 5, 2020
Patch release updating to the latest Airship SDKs and addressing issues with YouTube video support and channel registration on iOS.

### Changes
- Updated iOS SDK to 13.3.0
- Updated Android SDK to 13.1.0
- Fixed YouTube video support in Message Center and HTML In-app messages.
- Fixed channel registration to occur every APNs registration change.

## Version 10.1.1 - March 23, 2020
Patch addressing a regression in iOS SDK 13.1.0 causing channel tag loss
when upgrading from iOS SDK versions prior to 13.0.1. Apps upgrading from plugin
9.0.1 or below should avoid plugin versions 10.1.0 in favor of version 10.1.1.

- Updated iOS SDK to 13.1.1

## Version 10.1.0 - February 24, 2020
Minor release that adds support for number attributes, new data collection flags and screen tracking.

### Changes
- Updated iOS SDK to 13.1.0.
- Updated Android SDK to 12.2.0.
- Added number attributes support for iOS and Android
- Added data collection controls for iOS and Android
- Added screen tracking for iOS and Android

## Version 10.0.0 - January 15, 2020
Major release adding support for channel attributes, which allow
key value pairs to be associated with the application's Airship channel
for segmentation purposes.

Custom channel attributes are currently a beta feature. If you wish to
participate in the beta program, please complete our
[signup form](https://www.airship.com/lp/sign-up-now-to-participate-in-the-advanced-segmentation-beta-program/).

### Changes
- Updated iOS SDK to 13.0.4.
- Updated Android SDK to 12.1.0. Plugin now requires modern Jetpack libraries (AndroidX).
- Removed overlay inbox message feature.

## Version 9.0.1 - December 6, 2019
Patch release to fix a bug affecting loss of tags on iOS during app
migration to plugin 9.0.0. This patch release fixes the bug
by combining previous tags with tags that have been set since
the update to 9.0.0. Applications using 9.0.0 should update.

### Changes
- Updated iOS SDK to 12.1.2

## Version 9.0.0 - October 16, 2019

- Updated iOS Airship SDK to 12.0.0
- Updated iOS minimum deployment target to 11.0
- Fixed overlayInboxMessage crash on iOS

## Version 8.1.0 - September 23, 2019

- Updated Android Airship SDK to 10.1.3
- Updated iOS Airship SDK to 11.1.2
- Added support for EU cloud site
- Updated jsdoc to 3.6.3

## Version 8.0.0 - July 10, 2019
- Updated Android SDK to 10.0.1
- Updated iOS SDK to 11.1.0
- Added support for creating new Android notification channels and setting the default notification channel ID in Android
- Using Airship location now requires adding airship-location-cordova plugin
- Plugin now requires cordova >= 9.0.0, cordova-ios >= 5.0.1 and cocoapods >= 1.7.3

## Version 7.6.0 - June 5, 2019
- Added support for app defined notification buttons/categories.
- Fixed push events not being sent on iOS if a notification was delivered with a deep link.

## Version 7.5.4 - April 19, 2019
- Added a gradle property `uaSkipApplyGoogleServicesPlugin` that will disable applying
the `GoogleServicesPlugin` if set to `true`. This option should only be used if another
plugin also applies the `GoogleServicesPlugin` to avoid build errors.
- Updated Airship iOS SDK to 10.2.2

## Version 7.5.3 - March 14, 2019
Fixed a security issue within Android Urban Airship SDK, that could allow trusted
URL redirects in certain edge cases. All applications that are using
urbanairship-cordova version 7.0.0 - 7.5.2 on Android should update as soon as possible.
For more details, please email security@urbanairship.com.

## Version 7.5.2 - March 1, 2019
- Make Android compatible with Java 1.6

## Version 7.5.1 - February 4, 2019
- Fix Android crash when receiving a push when push is disabled.

## Version 7.5.0 - January 23, 2019
- Added ShowInbox event
- Fixed issue with installing directly from our Githup repo
- Fixed Android build for new version of Cordova (8.1.2)
- Fixed build failure in UAirshipPlugin.java
- Updated Android SDK to 9.7.0
- Updated iOS SDK to 10.0.4

## Version 7.4.0 - December 21, 2018
- Added APIs to manage active notifications
- Fixed issue with calling takeOff from JS

## Version 7.3.4 - November 20, 2018
- Updated Android SDK to 9.5.6

## Version 7.3.3 - November 14, 2018
- Fixed build issue with 7.3.2
- Updated Android SDK to 9.5.5

## Version 7.3.2 - November 13, 2018
- Fixed package issue with 7.3.1
- Updated Android SDK to 9.5.4
- Updated iOS SDK to 10.0.3

## Version 7.3.1 - October 9, 2018
- Updated Android plugin to be compatible with older versions of Cordova Android.

## Version 7.3.0 - September 25, 2018
- Updated iOS SDK to 10.0.0

## Version 7.2.1 - September 20, 2018
- Updated iOS SDK to 9.4.0
- Updated Android SDK to 9.5.2

## Version 7.2.0 - July 26, 2018
- Updated iOS SDK to 9.3.3
- Updated Android SDK to 9.4.1
- Added setter for foreground presentation options
- Enabled takeOff from the JS interface for iOS and Android

## Version 7.1.1 - June 15, 2018
- Updated Android SDK to 9.3.1

## Version 7.1.0 - June 13, 2018
- Updated iOS SDK to 9.2.1
- Updated Android SDK to 9.3.0
- Added support for FCM on Android
- Added support for loading custom notification categories
- Notification event payloads now contain title and subtitle
- iOS notification event payloads now contain a top level APS object with raw notification data
- iOS background notifications now generate events
- Plugin now requires Cordova 7.0. Earlier versions may work, but integration steps and
  documentation assume Cordova 7.0 or higher moving forward.

## Version 7.0.1 - March 8, 2018
- Updated iOS SDK to 9.0.3.
- Updated Android SDK to 9.0.2.
- Fixed build failures by excluding Android libraries version 28 until it is released.

## Version 7.0.0 - February 14, 2018
- Updated iOS SDK to 9.0.2.
- Updated Android SDK to 9.0.1.

## Version 6.9.3 - February 1, 2018
- Updated Android SDK to 8.9.7.
- Updated to be compatible with Cordova Android 7.0.0. Due to an
  [issue in the android resource processing (AAPT2)](https://issuetracker.google.com/issues/69347762),
  the GCM/FCM sender ID either needs to be prefixed with `sender:` or you can disable AAPT2 with
  [cordova-disable-aapt2](https://github.com/runtrizapps/cordova-android-disable-aapt2).
- Added notification response info to the notification open event.
- Added registration token to the channel registration event.
- Fixed iOS 11 main thread warnings.

## Version 6.9.2 - November 20, 2017
- Updated Android SDK to 8.9.5.
- Updated iOS SDK to 8.6.3.
- Fixed build Android plugin build issue with AndroidManifest.

## Version 6.9.1 - August 22, 2017
- Updated iOS Urban Airship SDK to 8.5.3.
- Fixed missing symbols for StoreKit and WebKit when building for iOS.

## Version 6.9.0 - August 10, 2017
- Updated iOS Urban Airship SDK to 8.5.2
- Updated Android Urban Airship SDK to 8.8.2
- Added event `notification_opt_in_status` that will fire when authorization status changes.

## Version 6.8.0 - June 21, 2017
- Updated Android Urban Airship SDK to 8.6.0

## Version 6.7.1 - May 23, 2017
 - Updated iOS Urban Airship SDK to 8.3.3
 - Updated Android Urban Airship SDK to 8.4.2

## Version 6.7.0 - May 15, 2017
 - Updated iOS Urban Airship SDK to 8.3.2
 - Updated Android Urban Airship SDK to 8.4.1
 - ‘Alias’ functionality is now deprecated in the plugin. It will be removed in a future version of the plugin. Please use 'Named User'.
 - iOS: Use static library until dynamic framework build issues with Cordova and Cocoapods have been resolved.
 - Android: fix landing page issues

## Version 6.6.0 - Feb 3, 2017
 - Updated iOS Urban Airship SDK to 8.0.5
 - Added support for Message Center, Inbox Message and Overlay Inbox Message dismissal in iOS and Android

## Version 6.5.0 - Jan 30, 2017
 - Update Android Urban Airship SDK to use the latest version of play services.
 - Updated Android Urban Airship SDK to 8.2.5

## Version 6.4.0 - Jan 6, 2017
 - Added support for configuring logging levels.

## Version 6.3.0 - Oct 31, 2016
 - Updated min iOS engine to 4.3.0
 - Included Urban Airship SDK using a podspec instead of a static lib
 - Added apns-environment entitlements to the plugin.xml
 - Updated Urban Airship iOS SDK to 8.0.3

## Version 6.2.0 - Oct 7, 2016
 - Added support for iOS foreground presentation options
 - Added method `addCustomEvent` to add custom events
 - Added event `notification_open` that will fire when a notification is opened

## Version 6.1.0 - Oct 4, 2016
 - Updated iOS Urban Airship SDK to 8.0.2
 - Added support for enabling/disabling the "displayASAPEnabled" property for in-app messages

## Version 6.0.1 - Sept 21, 2016
 - Fixed getLaunchNotification and push events for iOS
 - Fixed iOS deep link not accepting non url deep links
 - Removed overriding the Android minSDK to 16

## Version 6.0.0 - Sept 19, 2016
 - Updated Android Urban Airship SDK to 8.0.1
 - Updated iOS Urban Airship SDK to 8.0.1 (requires Xcode 8)
 - Added support for setting large notification icon in Android

## Version 5.4.0 - Sept 1, 2016
 - Updated iOS Urban Airship SDK to 7.3.0
 - Updated Android Urban Airship SDK to 7.3.0
 - All events are now fired after the 'deviceready' event
 - Added document event 'urbanairship.deep_link' when a new deep link is received
 - Added `isAppNotificationsEnabled` to check if the user opted in to notifications for the device
 - Added `setAssociatedIdentifier` to set device identifiers for Urban Airship Connect

## Version 5.3.1 - July 18, 2016
 - Update Android Urban Airship SDK to use latest version of play services.

## Version 5.3.0 - June 22, 2016
 - Updated Android Urban Airship SDK to 7.2.0

## Version 5.2.0 - June 2, 2016
 - Updated iOS Urban Airship SDK to 7.2.0
 - Updated Android Urban Airship SDK to 7.1.5

## Version 5.1.4 - May 20, 2016
 - Fixed bug where Message Center UI is displayed on a background thread

## Version 5.1.3 - May 12, 2016
 - Updated Android Urban Airship SDK to 7.1.3
 - Updated iOS Urban Airship SDK to 7.1.2

## Version 5.1.2 - April 28, 2016
 - Updated Android Urban Airship SDK to 7.1.2

## Version 5.1.1 - April 26, 2016
 - Updated Android Urban Airship SDK to 7.1.1

## Version 5.1.0 - April 21, 2016
 - Updated Android and iOS Urban Airship SDK to 7.1.0
 - Added deep linking support.
 - Added flag to disable auto launching the Message Center when the corresponding push is opened.

## Version 5.0.1 - Mar 17, 2016
 - Updated Android maven URL to https

## Version 5.0.0 - Mar 7, 2016
 - Updated Android Urban Airship SDK to 7.0.3
 - All functions now take an optional failure callback. The method runAction now
   returns just the result's value in the success callback and the result's error
   in the failure callback.
 - Added methods to access and modify the inbox listing.
 - Generated JSDocs are now available - http://docs.urbanairship.com/reference/libraries/urbanairship-cordova/latest/

## Version 4.1.1 - Feb 12, 2016
 - Updated iOS Urban Airship SDK to 7.0.2

## Version 4.1.0 - Feb 8, 2016
 - Updated iOS Urban Airship SDK to 7.0.1
 - Updated Android Urban Airship SDK to 7.0.1
 - Added Message Center support
 - Added config to set the Android notification sound
 - Removed Cordova minimum version restriction
 - Removed Cordova iOS minimum version restriction

## Version 4.0.1 - Nov 19, 2015
 - Fixed enabling location for Android below API 23.

## Version 4.0.0 - Nov 18, 2015
 - Updated to Cordova 5.4.0, Cordova-iOS 3.9.0, Cordova-Android 4.1.0
 - Updated iOS Urban Airship SDK to 6.4.0 (bitcode) compatible with Xcode 7
 - Updated Android Urban Airship SDK to 6.4.1
 - Request Android location permissions for API 23+

## Version 3.6.0 - Oct 29, 2015
 - Updated iOS Urban Airship SDK to 6.4.0 (non-bitcode) compatible with Xcode 6.4
 - Updated Android Urban Airship SDK to 6.4.0

## Version 3.5.0 - Oct 2, 2015
 - Updated iOS Urban Airship SDK to 6.3.0 (non-bitcode) compatible with Xcode 6.4
 - Updated Android Urban Airship SDK to 6.3.0

## Version 3.4.1 - Sept 18, 2015
 - Updated iOS Urban Airship SDK to 6.2.2 (non-bitcode) compatible with Xcode 6.4
 - The urbanairship.registration event on iOS now returns the device token along with the channel ID.

## Version 3.4.0 - Aug 24, 2015
 - Updated iOS Urban Airship SDK to 6.2.0
 - Updated Android Urban Airship SDK to 6.2.2

## Version 3.3.3 - Aug 3, 2015
 - Updated iOS Urban Airship SDK to 6.1.4
 - Fixed AndroidRuntimeException when handling any user recoverable Google Play services errors.

## Version 3.3.2 - July 30, 2015
 - Updated iOS Urban Airship SDK to 6.1.3
 - Updated Android Urban Airship SDK to 6.1.3

## Version 3.3.1 - July 14, 2015
 - Use the CordovaLandingPageView in the landing page activity.

## Version 3.3.0 - July 13, 2015
 - Themed the Android landing page to match iOS.
 - Fixed Android security exception when applications try to open a database with 'window.openDatabase' after
   launching the application with a landing page.

## Version 3.2.2 - July 10, 2015
 - Updated iOS Urban Airship SDK to 6.1.2

## Version 3.2.1 - July 7, 2015
 - Updated iOS Urban Airship SDK to 6.1.1
 - Updated Android Urban Airship SDK to 6.1.2

## Version 3.2.0 - June 25, 2015
 - Updated iOS Urban Airship SDK to 6.1.0
 - Updated Android Urban Airship SDK to 6.1.1
 - Added tag group support

## Version 3.1.4 - May 26, 2015
 - Updated Android Urban Airship SDK to 6.0.2
 - Added ability to disable analytics through the config

## Version 3.1.3 - April 29, 2015
 - Updated iOS Urban Airship SDK to 6.0.2

## Version 3.1.2 - April 23, 2015
 - Fixed iOS build error

## Version 3.1.1 - April 22, 2015
 - Renamed plugin ID to urbanairship-cordova to better match the IDs for NPM

## Version 3.1.0 - April 22, 2015
 - Updated Android plugin to support cordova-android 4.0.0

## Version 3.0.0 - April 14, 2015
 - Updated Android and iOS Urban Airship SDK to 6.0.1
 - Removed the cordova device plugin dependency
 - Renamed the plugin ID to "com.urbanairship.cordova" from "com.urbanairship.phonegap.PushNotification"
 - Renamed the window accessor to "UAirship" from "PushNotification"
 - Added method to get the channel ID on both Android and iOS
 - Updated the API to remove any enable/disable methods
 - Updated getTags to return an array of tags instead of an object
 - Support for running Urban Airship actions
 - Support for disabling/enabling analytics
 - Support for associating and disassociating a channel to a named user
 - iOS: Added method to get current badge number (iOS only)
 - iOS: Added config option to not clear badge on launch (iOS only)
 - Android: Added config to set the notification icon and accent color
 - Android: Plugin now depends on "android.support.v4" and "com.google.play.services"

## Version 2.8.1 - February 12, 2015
 - Revert min sdk version back to 3.4.0

## Version 2.8.0 - February 11, 2015
 - Removed extra manual installation steps by providing plugin hooks.
 - Updated the minimum cordova version to 4.0.0.

## Version 2.7.3 - January 28, 2015
- Updated Urban Airship Android Library to 5.1.5

## Version 2.7.2 - December 31, 2014
- Updated Urban Airship Android Library to 5.1.4

## Version 2.7.1 - December 15, 2014
- Updated Urban Airship Android Library to 5.1.3

## Version 2.7.0 - December 8, 2014
- Updated Urban Airship iOS Library to 5.1.0
- Updated Urban Airship Android Library to 5.1.2

## Version 2.6.0 - November 5, 2014
- Updated Urban Airship Android Library to 5.1.0

## Version 2.5.1 - November 3, 2014
- Removed writeJavascript calls for compatibility with Cordova 4.0

## Version 2.5.0 - September 22, 2014
- Updated Urban Airship iOS Library to 5.0.0

## Version 2.4.0 - April 23, 2014
- Updated for Cordova 3.4.0+
- Updated Urban Airship iOS Library to 4.0.0
- Updated Urban Airship Android Library to 4.0.1,
CHANGELOG: http://docs.urbanairship.com/reference/libraries/android/latest/android_changelog.txt

## Version 2.3.3 - April 18, 2014
- Added PushNotification.clearNotifications() to clear all notifications for the application on Android.

## Version 2.3.2 - March 10, 2014
- Updated Urban Airship iOS Library to 3.0.3, which fixed detectProvisioningMode behavior when
inProduction is not set.

## Version 2.3.1 - February 25, 2014
- Updated Urban Airship Android Library to 3.3.1,
CHANGELOG: http://docs.urbanairship.com/reference/libraries/android/latest/android_changelog.txt
- Fixes crash with Android background location

## Version 2.3.0 - January 15, 2014
- Updated Urban Airship Android Library to 3.3.0,
CHANGELOG: http://docs.urbanairship.com/reference/libraries/android/latest/android_changelog.txt
- Add config to enable push on launch.  When push is not enabled on launch,
it will be enabled once enablePush is called from javascript.

## Version 2.2.2 - January 2, 2014
- Start iOS location provider automatically when location is enabled

## Version 2.2.1 - December 30, 2013
- Fix iOS location bug

## Version 2.2.0 - November 18, 2013
- Use DOM events for incoming push and registration events
- Use js-module for including the PushNotification.js file
- Updated and fixed issues in the provided example

## Version 2.1.1 - November 18, 2013
- Update device plugin dependency id
- Add log statement to iOS push plugin on initialize

## Version 2.1.0 - September 20, 2013
- Update Urban Airship iOS Library to 3.0.0, CHANGELOG: https://github.com/urbanairship/ios-library/blob/3.0.0/CHANGELOG
- Requires Xcode 5 now

## Version 2.0.1 - August 8, 2013
- Update Urban Airship iOS Library to 2.0.1 that fixed a bug in UAHTTPConnectionOperation
causing intermittent crashes during push registration.

## Version 2.0.0 - July 30, 2013
- Updated for PhoneGap 3.0.0

## Version 1.0.8 - July 23, 2013
- Update iOS plugin to latest UAirship release (2.0.0)
- Fix: Fix possible crash with setQuietTime on iOS

## Version 1.0.7 - June 12, 2013
- Fix: setAlias on Android to actually set the alias.
- Fix: setQuietTime on iOS now sets the correct time.

## Version 1.0.6 - April 22, 2013
- Updated to Cordova 2.6
- Sample apps load plugin at startup

## Version 1.0.5 - March 8, 2013
- Moved the plugin intent receiver into the plugin package (Android)
- Properly handle the Cordova 2.3 changes to the iOS platform value

## Version 1.0.4 - February 19, 2013
- Updated plugin to be compatible with version 2.0.4 of
the Urban Airship Android library

## Version 1.0.3 - December 7, 2012
- FIXED: getIncoming now only returns incoming push data once
- Improved Intent flags in Android sample application when launching
  from a notification
- Sample applications now log push data to the console instead of
  displaying alert dialogs

## Version 1.0.2 - November 16, 2012
- Updated to PhoneGap 2.2.0
- FIXED: App delegate surrogate handling was causing edge case issues
  with iOS 6 and certain plugin combinations
- FIXED: setting an alias to an empty string results in subsequent
  registration failures
- Changed default value of UAPushEnabled to NO in NSUserDefaults on app launch.
  This only affects the iOS plugin using v1.3.5 of libUAirship.a or greater.
  If you have used previous versions of the plugin, you should update your
  app in order to reflect this change, in order to see the same behavior.

## Version 1.0.1 - August 23, 2012
- FIXED: Addressed crash in the Android plugin caused by Cordova library bug when push or
  registration events occur before the web view is initialized.
- FIXED: Addressed JSON bug resulting in invalid push extras.
- Removed Urban Airship library from iOS and Android sample projects. See the documentation at
  https://docs.urbanairship.com/display/DOCS/Client%3A+PhoneGap for more information
