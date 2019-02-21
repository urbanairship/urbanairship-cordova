/* Copyright Urban Airship and Contributors */

#import "UACordovaPluginManager.h"
#import "AirshipLib.h"

// Config keys
NSString *const ProductionAppKeyConfigKey = @"com.urbanairship.production_app_key";
NSString *const ProductionAppSecretConfigKey = @"com.urbanairship.production_app_secret";
NSString *const DevelopmentAppKeyConfigKey = @"com.urbanairship.development_app_key";
NSString *const DevelopmentAppSecretConfigKey = @"com.urbanairship.development_app_secret";
NSString *const ProductionLogLevelKey = @"com.urbanairship.production_log_level";
NSString *const DevelopmentLogLevelKey = @"com.urbanairship.development_log_level";
NSString *const ProductionConfigKey = @"com.urbanairship.in_production";
NSString *const EnablePushOnLaunchConfigKey = @"com.urbanairship.enable_push_onlaunch";
NSString *const ClearBadgeOnLaunchConfigKey = @"com.urbanairship.clear_badge_onlaunch";
NSString *const EnableAnalyticsConfigKey = @"com.urbanairship.enable_analytics";
NSString *const AutoLaunchMessageCenterKey = @"com.urbanairship.auto_launch_message_center";
NSString *const NotificationPresentationAlertKey = @"com.urbanairship.ios_foreground_notification_presentation_alert";
NSString *const NotificationPresentationBadgeKey = @"com.urbanairship.ios_foreground_notification_presentation_badge";
NSString *const NotificationPresentationSoundKey = @"com.urbanairship.ios_foreground_notification_presentation_sound";

NSString *const AuthorizedNotificationSettingsAlertKey = @"alert";
NSString *const AuthorizedNotificationSettingsBadgeKey = @"badge";
NSString *const AuthorizedNotificationSettingsSoundKey = @"sound";
NSString *const AuthorizedNotificationSettingsCarPlayKey = @"carPlay";
NSString *const AuthorizedNotificationSettingsLockScreenKey = @"lockScreen";
NSString *const AuthorizedNotificationSettingsNotificationCenterKey = @"notificationCenter";

// Events
NSString *const EventPushReceived = @"urbanairship.push";
NSString *const EventNotificationOpened = @"urbanairship.notification_opened";
NSString *const EventNotificationOptInStatus = @"urbanairship.notification_opt_in_status";

NSString *const EventShowInbox = @"urbanairship.show_inbox";
NSString *const EventInboxUpdated = @"urbanairship.inbox_updated";
NSString *const EventRegistration = @"urbanairship.registration";
NSString *const EventDeepLink = @"urbanairship.deep_link";

@interface UACordovaPluginManager() <UARegistrationDelegate, UAPushNotificationDelegate, UAInboxDelegate, UADeepLinkDelegate>
@property (nonatomic, strong) NSDictionary *defaultConfig;
@property (nonatomic, strong) NSMutableDictionary *pendingEvents;
@property (nonatomic, assign) BOOL isAirshipReady;

@end
@implementation UACordovaPluginManager

- (void)dealloc {
    [UAirship push].pushNotificationDelegate = nil;
    [UAirship push].registrationDelegate = nil;
    [UAirship inbox].delegate = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (instancetype)initWithDefaultConfig:(NSDictionary *)defaultConfig {
    self = [super init];

    if (self) {
        self.defaultConfig = defaultConfig;
        self.pendingEvents = [NSMutableDictionary dictionary];
    }

    return self;
}

+ (instancetype)pluginManagerWithDefaultConfig:(NSDictionary *)defaultConfig {
    return [[UACordovaPluginManager alloc] initWithDefaultConfig:defaultConfig];
}

- (void)attemptTakeOff {
    if (self.isAirshipReady) {
        return;
    }

    UAConfig *config = [self createAirshipConfig];
    if (![config validate]) {
        return;
    }

    [UAirship takeOff:config];

    [UAirship push].userPushNotificationsEnabledByDefault = [[self configValueForKey:EnablePushOnLaunchConfigKey] boolValue];

    if ([[self configValueForKey:ClearBadgeOnLaunchConfigKey] boolValue]) {
        [[UAirship push] resetBadge];
    }

    [UAirship push].pushNotificationDelegate = self;
    [UAirship push].registrationDelegate = self;
    [UAirship inbox].delegate = self;
    [UAirship shared].deepLinkDelegate = self;

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(inboxUpdated)
                                                 name:UAInboxMessageListUpdatedNotification
                                               object:nil];

    self.isAirshipReady = YES;
}

- (UAConfig *)createAirshipConfig {
    UAConfig *airshipConfig = [UAConfig config];
    airshipConfig.productionAppKey = [self configValueForKey:ProductionAppKeyConfigKey];
    airshipConfig.productionAppSecret = [self configValueForKey:ProductionAppSecretConfigKey];
    airshipConfig.developmentAppKey = [self configValueForKey:DevelopmentAppKeyConfigKey];
    airshipConfig.developmentAppSecret = [self configValueForKey:DevelopmentAppSecretConfigKey];

    if ([self configValueForKey:ProductionConfigKey] != nil) {
        airshipConfig.inProduction = [[self configValueForKey:ProductionConfigKey] boolValue];
    }

    airshipConfig.developmentLogLevel = [self parseLogLevel:[self configValueForKey:DevelopmentLogLevelKey]
                                            defaultLogLevel:UALogLevelDebug];

    airshipConfig.productionLogLevel = [self parseLogLevel:[self configValueForKey:ProductionLogLevelKey]
                                           defaultLogLevel:UALogLevelError];

    if ([self configValueForKey:EnableAnalyticsConfigKey] != nil) {
        airshipConfig.analyticsEnabled = [[self configValueForKey:EnableAnalyticsConfigKey] boolValue];
    }

    return airshipConfig;
}

- (id)configValueForKey:(NSString *)key {
    id value = [[NSUserDefaults standardUserDefaults] objectForKey:key];
    if (value != nil) {
        return value;
    }

    return self.defaultConfig[key];
}

- (BOOL)autoLaunchMessageCenter {
    if ([self configValueForKey:AutoLaunchMessageCenterKey] == nil) {
        return YES;
    }

    return [[self configValueForKey:AutoLaunchMessageCenterKey] boolValue];
}

- (void)setAutoLaunchMessageCenter:(BOOL)autoLaunchMessageCenter {
    [[NSUserDefaults standardUserDefaults] setValue:@(autoLaunchMessageCenter) forKey:AutoLaunchMessageCenterKey];
}

- (void)setProductionAppKey:(NSString *)appKey appSecret:(NSString *)appSecret {
    [[NSUserDefaults standardUserDefaults] setValue:appKey forKey:ProductionAppKeyConfigKey];
    [[NSUserDefaults standardUserDefaults] setValue:appSecret forKey:ProductionAppSecretConfigKey];
}

- (void)setDevelopmentAppKey:(NSString *)appKey appSecret:(NSString *)appSecret {
    [[NSUserDefaults standardUserDefaults] setValue:appKey forKey:DevelopmentAppKeyConfigKey];
    [[NSUserDefaults standardUserDefaults] setValue:appSecret forKey:DevelopmentAppSecretConfigKey];
}

- (void)setPresentationOptions:(NSUInteger)options {
    [[NSUserDefaults standardUserDefaults] setValue:@(options & UNNotificationPresentationOptionAlert) forKey:NotificationPresentationAlertKey];
    [[NSUserDefaults standardUserDefaults] setValue:@(options & UNNotificationPresentationOptionBadge) forKey:NotificationPresentationBadgeKey];
    [[NSUserDefaults standardUserDefaults] setValue:@(options & UNNotificationPresentationOptionSound) forKey:NotificationPresentationSoundKey];
}

-(NSInteger)parseLogLevel:(id)logLevel defaultLogLevel:(UALogLevel)defaultValue  {
    if (![logLevel isKindOfClass:[NSString class]] || ![logLevel length]) {
        return defaultValue;
    }

    NSString *normalizedLogLevel = [[logLevel stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] lowercaseString];

    if ([normalizedLogLevel isEqualToString:@"verbose"]) {
        return UALogLevelTrace;
    } else if ([normalizedLogLevel isEqualToString:@"debug"]) {
        return UALogLevelDebug;
    } else if ([normalizedLogLevel isEqualToString:@"info"]) {
        return UALogLevelInfo;
    } else if ([normalizedLogLevel isEqualToString:@"warning"]) {
        return UALogLevelWarn;
    } else if ([normalizedLogLevel isEqualToString:@"error"]) {
        return UALogLevelError;
    } else if ([normalizedLogLevel isEqualToString:@"none"]) {
        return UALogLevelNone;
    }

    return defaultValue;
}


#pragma mark UAInboxDelegate

- (void)showMessageForID:(NSString *)messageID {
    if (self.autoLaunchMessageCenter) {
        [[UAirship messageCenter] displayMessageForID:messageID];
    } else {
        [self fireEvent:EventShowInbox data:@{@"messageId":messageID}];
    }
}

- (void)showInbox {
    if (self.autoLaunchMessageCenter) {
        [[UAirship messageCenter] display];
    } else {
        [self fireEvent:EventShowInbox data:@{}];
    }
}

- (void)inboxUpdated {
    UA_LDEBUG(@"Inbox updated");
    [self fireEvent:EventInboxUpdated data:@{}];
}

#pragma mark UAPushNotificationDelegate

-(void)receivedForegroundNotification:(UANotificationContent *)notificationContent completionHandler:(void (^)(void))completionHandler {
    UA_LDEBUG(@"Received a notification while the app was already in the foreground %@", notificationContent);

    id event = [self pushEventFromNotification:notificationContent];
    [self fireEvent:EventPushReceived data:event];

    completionHandler();
}

- (void)receivedBackgroundNotification:(UANotificationContent *)notificationContent
                     completionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {

    UA_LDEBUG(@"Received a background notification %@", notificationContent);
    id event = [self pushEventFromNotification:notificationContent];

    [self fireEvent:EventPushReceived data:event];
    completionHandler(UIBackgroundFetchResultNoData);
}

-(void)receivedNotificationResponse:(UANotificationResponse *)notificationResponse completionHandler:(void (^)(void))completionHandler {
    UA_LDEBUG(@"The application was launched or resumed from a notification %@", notificationResponse);
    NSDictionary *pushEvent = [self pushEventFromNotification:notificationResponse.notificationContent];
    NSMutableDictionary *event = [NSMutableDictionary dictionaryWithDictionary:pushEvent];

    if ([notificationResponse.actionIdentifier isEqualToString:UANotificationDefaultActionIdentifier]) {
        [event setValue:@(YES) forKey:@"isForeground"];
    } else {
        UANotificationAction *notificationAction = [self notificationActionForCategory:notificationResponse.notificationContent.categoryIdentifier
                                                                      actionIdentifier:notificationResponse.actionIdentifier];

        BOOL isForeground = notificationAction.options & UNNotificationActionOptionForeground;
        [event setValue:@(isForeground) forKey:@"isForeground"];
        [event setValue:notificationResponse.actionIdentifier forKey:@"actionID"];
    }

    self.lastReceivedNotificationResponse = event;
    [self fireEvent:EventNotificationOpened data:event];
    completionHandler();
}

- (UNNotificationPresentationOptions)presentationOptionsForNotification:(UNNotification *)notification NS_AVAILABLE_IOS(10.0) {
    UNNotificationPresentationOptions options = UNNotificationPresentationOptionNone;

    if ([[self configValueForKey:NotificationPresentationAlertKey] boolValue]) {
        options = options | UNNotificationPresentationOptionAlert;
    }

    if ([[self configValueForKey:NotificationPresentationBadgeKey] boolValue]) {
        options = options | UNNotificationPresentationOptionBadge;
    }

    if ([[self configValueForKey:NotificationPresentationSoundKey] boolValue]) {
        options = options | UNNotificationPresentationOptionSound;
    }

    return options;
}

#pragma mark UADeepLinkDelegate
-(void)receivedDeepLink:(NSURL *_Nonnull)url completionHandler:(void (^_Nonnull)(void))completionHandler {
    self.lastReceivedDeepLink = [url absoluteString];
    NSDictionary *data = @{ @"deepLink":[url absoluteString]};
    [self fireEvent:EventDeepLink data:data];
}

#pragma mark UARegistrationDelegate

- (void)registrationSucceededForChannelID:(NSString *)channelID deviceToken:(NSString *)deviceToken {
    UA_LINFO(@"Channel registration successful %@.", channelID);

    NSDictionary *data;
    if (deviceToken) {
        data = @{ @"channelID":channelID, @"deviceToken":deviceToken, @"registrationToken":deviceToken };
    } else {
        data = @{ @"channelID":channelID };
    }

    [self fireEvent:EventRegistration data:data];
}

- (void)registrationFailed {
    UA_LINFO(@"Channel registration failed.");
    [self fireEvent:EventRegistration data:@{ @"error": @"Registration failed." }];
}

- (void)notificationAuthorizedSettingsDidChange:(UAAuthorizedNotificationSettings)authorizedSettings {
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
                                  // Deprecated payload for backwards compatibility
                                  @"notificationOptions" : @{
                                          NotificationPresentationAlertKey : @(alertBool),
                                          NotificationPresentationBadgeKey : @(badgeBool),
                                          NotificationPresentationSoundKey : @(soundBool)
                                  },
                                  @"authorizedNotificationSettings" : @{
                                          AuthorizedNotificationSettingsAlertKey : @(alertBool),
                                          AuthorizedNotificationSettingsBadgeKey : @(badgeBool),
                                          AuthorizedNotificationSettingsSoundKey : @(soundBool),
                                          AuthorizedNotificationSettingsCarPlayKey : @(carPlayBool),
                                          AuthorizedNotificationSettingsLockScreenKey : @(lockScreenBool),
                                          AuthorizedNotificationSettingsNotificationCenterKey : @(notificationCenterBool)
                                  }};

    UA_LINFO(@"Opt in status changed.");
    [self fireEvent:EventNotificationOptInStatus data:eventBody];
}

- (NSDictionary *)pushEventFromNotification:(UANotificationContent *)notificationContent {
    if (!notificationContent) {
        return @{ @"message": @"", @"extras": @{}};
    }

    NSMutableDictionary *info = [NSMutableDictionary dictionaryWithDictionary:notificationContent.notificationInfo];

    // remove the send ID
    if([[info allKeys] containsObject:@"_"]) {
        [info removeObjectForKey:@"_"];
    }

    NSMutableDictionary *result = [NSMutableDictionary dictionary];

    // If there is an aps dictionary in the extras, remove it and set it as a top level object
    if([[info allKeys] containsObject:@"aps"]) {
        result[@"aps"] = info[@"aps"];
        [info removeObjectForKey:@"aps"];
    }

    result[@"message"] = notificationContent.alertBody ?: @"";

    // Set the title and subtitle as top level objects, if present
    NSString *title = notificationContent.alertTitle;
    NSString *subtitle = notificationContent.notification.request.content.subtitle;
    [result setValue:title forKey:@"title"];
    [result setValue:subtitle forKey:@"subtitle"];

    // Set the remaining info as extras
    result[@"extras"] = info;

    return result;
}

- (UANotificationAction *)notificationActionForCategory:(NSString *)category actionIdentifier:(NSString *)identifier {
    NSSet *categories = [UAirship push].combinedCategories;

    UANotificationCategory *notificationCategory;
    UANotificationAction *notificationAction;

    for (UANotificationCategory *possibleCategory in categories) {
        if ([possibleCategory.identifier isEqualToString:category]) {
            notificationCategory = possibleCategory;
            break;
        }
    }

    if (!notificationCategory) {
        UA_LERR(@"Unknown notification category identifier %@", category);
        return nil;
    }

    NSMutableArray *possibleActions = [NSMutableArray arrayWithArray:notificationCategory.actions];

    for (UANotificationAction *possibleAction in possibleActions) {
        if ([possibleAction.identifier isEqualToString:identifier]) {
            notificationAction = possibleAction;
            break;
        }
    }

    if (!notificationAction) {
        UA_LERR(@"Unknown notification action identifier %@", identifier);
        return nil;
    }

    return notificationAction;
}


- (void)fireEvent:(NSString *)type data:(NSDictionary *)data {
    id strongDelegate = self.delegate;
    if (strongDelegate) {
        [strongDelegate notifyListener:type data:data];
    }
}

- (void)setDelegate:(id<UACordovaPluginManagerDelegate>)delegate {
    _delegate = delegate;

    if (delegate) {
        NSDictionary *events = [self.pendingEvents copy];
        [self.pendingEvents removeAllObjects];

        for (NSString *eventType in events) {
            [self fireEvent:eventType data:events[eventType]];
        }
    }
}

@end
