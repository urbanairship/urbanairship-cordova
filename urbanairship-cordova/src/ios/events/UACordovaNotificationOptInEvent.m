/* Copyright Urban Airship and Contributors */

#import "UACordovaNotificationOptInEvent.h"

NSString *const EventNotificationOptInStatus = @"urbanairship.notification_opt_in_status";

NSString *const UACordovaNotificationOptInEventAlertKey = @"alert";
NSString *const UACordovaNotificationOptInEventBadgeKey = @"badge";
NSString *const UACordovaNotificationOptInEventSoundKey = @"sound";
NSString *const UACordovaNotificationOptInEventCarPlayKey = @"carPlay";
NSString *const UACordovaNotificationOptInEventLockScreenKey = @"lockScreen";
NSString *const UACordovaNotificationOptInEventNotificationCenterKey = @"notificationCenter";

@implementation UACordovaNotificationOptInEvent

+ (instancetype)eventWithAuthorizedSettings:(UAAuthorizedNotificationSettings)authorizedSettings {
    return [[UACordovaNotificationOptInEvent alloc] initWithAuthorizedSettings:authorizedSettings];
}

- (instancetype)initWithAuthorizedSettings:(UAAuthorizedNotificationSettings)authorizedSettings {
    self = [super init];

    if (self) {
        self.type = EventNotificationOptInStatus;
        self.data = [self eventDataForAuthorizedSettings:authorizedSettings];
    }

    return self;
}

- (NSDictionary *)eventDataForAuthorizedSettings:(UAAuthorizedNotificationSettings)authorizedSettings {
    BOOL optedIn = NO;

    BOOL alertBool = NO;
    BOOL badgeBool = NO;
    BOOL soundBool = NO;
    BOOL carPlayBool = NO;
    BOOL lockScreenBool = NO;
    BOOL notificationCenterBool = NO;

    if (authorizedSettings & UAAuthorizedNotificationSettingsAlert) {
        alertBool = YES;
    }

    if (authorizedSettings & UAAuthorizedNotificationSettingsBadge) {
        badgeBool = YES;
    }

    if (authorizedSettings & UAAuthorizedNotificationSettingsSound) {
        soundBool = YES;
    }

    if (authorizedSettings & UAAuthorizedNotificationSettingsCarPlay) {
        carPlayBool = YES;
    }

    if (authorizedSettings & UAAuthorizedNotificationSettingsLockScreen) {
        lockScreenBool = YES;
    }

    if (authorizedSettings & UAAuthorizedNotificationSettingsNotificationCenter) {
        notificationCenterBool = YES;
    }

    optedIn = authorizedSettings != UAAuthorizedNotificationSettingsNone;

    NSDictionary *eventBody = @{  @"optIn": @(optedIn),
                                  @"authorizedNotificationSettings" : @{
                                          UACordovaNotificationOptInEventAlertKey : @(alertBool),
                                          UACordovaNotificationOptInEventBadgeKey : @(badgeBool),
                                          UACordovaNotificationOptInEventSoundKey : @(soundBool),
                                          UACordovaNotificationOptInEventCarPlayKey : @(carPlayBool),
                                          UACordovaNotificationOptInEventLockScreenKey : @(lockScreenBool),
                                          UACordovaNotificationOptInEventNotificationCenterKey : @(notificationCenterBool)
                                  }};

    return eventBody;
}

@end
