/* Copyright Urban Airship and Contributors */

#import "UACordovaPluginManager.h"

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#import "AirshipMessageCenterLib.h"
#else
@import AirshipKit;
#endif

#import "UACordovaEvent.h"
#import "UACordovaDeepLinkEvent.h"
#import "UACordovaInboxUpdatedEvent.h"
#import "UACordovaNotificationOpenedEvent.h"
#import "UACordovaNotificationOptInEvent.h"
#import "UACordovaPushEvent.h"
#import "UACordovaRegistrationEvent.h"
#import "UACordovaShowInboxEvent.h"
#import "UACordovaPreferenceCenterEvent.h"

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
NSString *const CloudSiteConfigKey = @"com.urbanairship.site";
NSString *const CloudSiteEUString = @"EU";

NSString *const UACordovaPluginVersionKey = @"UACordovaPluginVersion";

// Events
NSString *const CategoriesPlistPath = @"UACustomNotificationCategories";


@interface UACordovaPluginManager() <UARegistrationDelegate, UAPushNotificationDelegate, UAMessageCenterDisplayDelegate, UADeepLinkDelegate, UAPreferenceCenterOpenDelegate>
@property (nonatomic, strong) NSDictionary *defaultConfig;
@property (nonatomic, strong) NSMutableArray<NSObject<UACordovaEvent> *> *pendingEvents;
@property (nonatomic, assign) BOOL isAirshipReady;

@end
@implementation UACordovaPluginManager

- (void)load {
    [[NSNotificationCenter defaultCenter] addObserverForName:UIApplicationDidFinishLaunchingNotification
                                                      object:nil
                                                       queue:nil usingBlock:^(NSNotification * _Nonnull note) {

        [self attemptTakeOffWithLaunchOptions:note.userInfo];
    }];
}

- (void)dealloc {
    [UAirship push].pushNotificationDelegate = nil;
    [UAirship push].registrationDelegate = nil;
    [UAMessageCenter shared].displayDelegate = nil;
    [UAPreferenceCenter shared].openDelegate = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (instancetype)initWithDefaultConfig:(NSDictionary *)defaultConfig {
    self = [super init];

    if (self) {
        self.defaultConfig = defaultConfig;
        self.pendingEvents = [NSMutableArray array];
    }

    return self;
}

+ (instancetype)pluginManagerWithDefaultConfig:(NSDictionary *)defaultConfig {
    return [[UACordovaPluginManager alloc] initWithDefaultConfig:defaultConfig];
}

- (void)attemptTakeOff {
    [self attemptTakeOffWithLaunchOptions:nil];
}

- (void)attemptTakeOffWithLaunchOptions:(NSDictionary *)launchOptions {
    if (self.isAirshipReady) {
        return;
    }

    UAConfig *config = [self createAirshipConfig];
    if (![config validate]) {
        return;
    }

    [UAirship takeOff:config launchOptions:launchOptions];
    [self registerCordovaPluginVersion];

    if ([[self configValueForKey:EnablePushOnLaunchConfigKey] boolValue]) {
        [UAirship push].userPushNotificationsEnabled = true;
    }

    if ([[self configValueForKey:ClearBadgeOnLaunchConfigKey] boolValue]) {
        [[UAirship push] resetBadge];
    }

    [self loadCustomNotificationCategories];

    [UAirship push].pushNotificationDelegate = self;
    [UAirship push].registrationDelegate = self;
    [UAMessageCenter shared].displayDelegate = self;
    [UAirship shared].deepLinkDelegate = self;
    [UAPreferenceCenter shared].openDelegate = self;

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(inboxUpdated)
                                                 name:UAInboxMessageListUpdatedNotification
                                               object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(channelRegistrationSucceeded:)
                                                 name:UAChannel.channelUpdatedEvent
                                               object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(channelRegistrationFailed)
                                                 name:UAChannel.channelRegistrationFailedEvent
                                               object:nil];



    self.isAirshipReady = YES;
}

- (void)loadCustomNotificationCategories {
    NSString *categoriesPath = [[NSBundle mainBundle] pathForResource:CategoriesPlistPath ofType:@"plist"];
    NSSet *customNotificationCategories = [UANotificationCategories createCategoriesFromFile:categoriesPath];

    if (customNotificationCategories.count) {
        UA_LDEBUG(@"Registering custom notification categories: %@", customNotificationCategories);
        [UAirship push].customCategories = customNotificationCategories;
        [[UAirship push] updateRegistration];
    }
}

- (UAConfig *)createAirshipConfig {
    UAConfig *airshipConfig = [UAConfig config];
    airshipConfig.productionAppKey = [self configValueForKey:ProductionAppKeyConfigKey];
    airshipConfig.productionAppSecret = [self configValueForKey:ProductionAppSecretConfigKey];
    airshipConfig.developmentAppKey = [self configValueForKey:DevelopmentAppKeyConfigKey];
    airshipConfig.developmentAppSecret = [self configValueForKey:DevelopmentAppSecretConfigKey];
    airshipConfig.URLAllowListScopeOpenURL = @[@"*"];

    NSString *cloudSite = [self configValueForKey:CloudSiteConfigKey];
    airshipConfig.site = [UACordovaPluginManager parseCloudSiteString:cloudSite];

    if ([self configValueForKey:ProductionConfigKey] != nil) {
        airshipConfig.inProduction = [[self configValueForKey:ProductionConfigKey] boolValue];
    }

    airshipConfig.developmentLogLevel = [self parseLogLevel:[self configValueForKey:DevelopmentLogLevelKey]
                                            defaultLogLevel:UALogLevelDebug];

    airshipConfig.productionLogLevel = [self parseLogLevel:[self configValueForKey:ProductionLogLevelKey]
                                           defaultLogLevel:UALogLevelError];

    if ([self configValueForKey:EnableAnalyticsConfigKey] != nil) {
        airshipConfig.isAnalyticsEnabled = [[self configValueForKey:EnableAnalyticsConfigKey] boolValue];
    }

    airshipConfig.enabledFeatures = UAFeaturesAll;

    return airshipConfig;
}

- (void)registerCordovaPluginVersion {
    NSString *version = [NSBundle mainBundle].infoDictionary[UACordovaPluginVersionKey] ?: @"0.0.0";
    [[UAirship analytics] registerSDKExtension:UASDKExtensionCordova version:version];
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

- (void)setCloudSite:(NSString *)site {
    [[NSUserDefaults standardUserDefaults] setValue:site forKey:CloudSiteConfigKey];
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

+ (UACloudSite)parseCloudSiteString:(NSString *)site {
    if ([CloudSiteEUString caseInsensitiveCompare:site] == NSOrderedSame) {
        return UACloudSiteEU;
    } else {
        return UACloudSiteUS;
    }
}

#pragma mark -
#pragma mark UAInboxDelegate

- (void)displayMessageCenterForMessageID:(NSString *)messageID animated:(BOOL)animated {
    if (self.autoLaunchMessageCenter) {
        [[UAMessageCenter shared].defaultUI displayMessageCenterForMessageID:messageID animated:true];
    } else {
        [self fireEvent:[UACordovaShowInboxEvent eventWithMessageID:messageID]];
    }
}

- (void)displayMessageCenterAnimated:(BOOL)animated {
    if (self.autoLaunchMessageCenter) {
        [[UAMessageCenter shared].defaultUI displayMessageCenterAnimated:animated];
    } else {
        [self fireEvent:[UACordovaShowInboxEvent event]];
    }
}

- (void)dismissMessageCenterAnimated:(BOOL)animated {
    if (self.autoLaunchMessageCenter) {
        [[UAMessageCenter shared].defaultUI dismissMessageCenterAnimated:animated];
    }
}

- (void)inboxUpdated {
    UA_LDEBUG(@"Inbox updated");
    [self fireEvent:[UACordovaInboxUpdatedEvent event]];
}

#pragma mark -
#pragma mark UAPushNotificationDelegate

-(void)receivedForegroundNotification:(NSDictionary *)userInfo completionHandler:(void (^)(void))completionHandler {
    UA_LDEBUG(@"Received a notification while the app was already in the foreground %@", userInfo);

    [self fireEvent:[UACordovaPushEvent eventWithNotificationContent:userInfo]];

    completionHandler();
}

- (void)receivedBackgroundNotification:(NSDictionary *)userInfo
                     completionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {

    UA_LDEBUG(@"Received a background notification %@", userInfo);

    [self fireEvent:[UACordovaPushEvent eventWithNotificationContent:userInfo]];

    completionHandler(UIBackgroundFetchResultNoData);
}

-(void)receivedNotificationResponse:(UNNotificationResponse *)notificationResponse completionHandler:(void (^)(void))completionHandler {
    UA_LDEBUG(@"The application was launched or resumed from a notification %@", notificationResponse);

    UACordovaNotificationOpenedEvent *event = [UACordovaNotificationOpenedEvent eventWithNotificationResponse:notificationResponse];
    self.lastReceivedNotificationResponse = event.data;
    [self fireEvent:event];

    completionHandler();
}

- (UNNotificationPresentationOptions)extendPresentationOptions:(UNNotificationPresentationOptions)options notification:(UNNotification *)notification {
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

#pragma mark -
#pragma mark UADeepLinkDelegate

-(void)receivedDeepLink:(NSURL *_Nonnull)url completionHandler:(void (^_Nonnull)(void))completionHandler {
    self.lastReceivedDeepLink = [url absoluteString];
    [self fireEvent:[UACordovaDeepLinkEvent eventWithDeepLink:url]];
    completionHandler();
}

#pragma mark -
#pragma mark Channel Registration Events

- (void)channelRegistrationSucceeded:(NSNotification *)notification {
    NSString *channelID = notification.userInfo[UAChannel.channelIdentifierKey];
    NSString *deviceToken = [UAirship push].deviceToken;

    UA_LINFO(@"Channel registration successful %@.", channelID);

    [self fireEvent:[UACordovaRegistrationEvent registrationSucceededEventWithChannelID:channelID deviceToken:deviceToken]];
}

- (void)channelRegistrationFailed {
    UA_LINFO(@"Channel registration failed.");
    [self fireEvent:[UACordovaRegistrationEvent registrationFailedEvent]];
}

#pragma mark -
#pragma mark UARegistrationDelegate

- (void)notificationAuthorizedSettingsDidChange:(UAAuthorizedNotificationSettings)authorizedSettings {
    UACordovaNotificationOptInEvent *event = [UACordovaNotificationOptInEvent eventWithAuthorizedSettings:authorizedSettings];
    [self fireEvent:event];
}

- (void)fireEvent:(NSObject<UACordovaEvent> *)event {
    id strongDelegate = self.delegate;

    if (strongDelegate && [strongDelegate notifyListener:event.type data:event.data]) {
        UA_LTRACE(@"Cordova plugin manager delegate notified with event of type:%@ with data:%@", event.type, event.data);

        return;
    }

    UA_LTRACE(@"No cordova plugin manager delegate available, storing pending event of type:%@ with data:%@", event.type, event.data);

    // Add pending event
    [self.pendingEvents addObject:event];
}

- (void)setDelegate:(id<UACordovaPluginManagerDelegate>)delegate {
    _delegate = delegate;

    if (delegate) {
        @synchronized(self.pendingEvents) {
            UA_LTRACE(@"Cordova plugin manager delegate set:%@", delegate);

            NSDictionary *events = [self.pendingEvents copy];
            [self.pendingEvents removeAllObjects];

            for (NSObject<UACordovaEvent> *event in events) {
                [self fireEvent:event];
            }
        }
    }
}

#pragma mark -
#pragma mark UAPreferenceCenterOpenDelegate

- (BOOL)openPreferenceCenter:(NSString * _Nonnull)preferenceCenterID {
    BOOL useCustomUi = [[NSUserDefaults standardUserDefaults] boolForKey:[self preferenceCenterUIKey:preferenceCenterID]];
    if (useCustomUi) {
        [self fireEvent:[UACordovaPreferenceCenterEvent eventWithPreferenceCenterId:preferenceCenterID]];
    }
    return useCustomUi;
}

- (void)setPreferenceCenter:(NSString *)preferenceCenterID useCustomUI:(BOOL)useCustomUI {
    [[NSUserDefaults standardUserDefaults] setBool:useCustomUI forKey:[self preferenceCenterUIKey:preferenceCenterID]];
}

- (NSString *)preferenceCenterUIKey:(NSString *)preferenceCenterID {
    return [NSString stringWithFormat:@"com.urbanairship.preference_%@_custom_ui", preferenceCenterID];
}

@end
