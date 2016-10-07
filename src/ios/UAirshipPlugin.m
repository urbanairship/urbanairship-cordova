/*
 Copyright 2009-2016 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#import "UAirshipPlugin.h"
#import "UAPush.h"
#import "UAirship.h"
#import "UAAnalytics.h"
#import "UALocation.h"
#import "UAConfig.h"
#import "NSJSONSerialization+UAAdditions.h"
#import "UAActionRunner.h"
#import "UADefaultMessageCenter.h"
#import "UAInAppMessaging.h"
#import "UAInbox.h"
#import "UAInboxMessageList.h"
#import "UAInboxMessage.h"
#import "UALandingPageOverlayController.h"
#import "UAMessageViewController.h"
#import "UAUtils.h"
#import "UADefaultMessageCenter.h"
#import "UAAssociatedIdentifiers.h"

typedef void (^UACordovaCompletionHandler)(CDVCommandStatus, id);
typedef void (^UACordovaExecutionBlock)(NSArray *args, UACordovaCompletionHandler completionHandler);

@interface UAirshipPlugin()
@property (nonatomic, strong) UANotificationResponse *launchNotificationResponse;
@property (nonatomic, copy) NSString *listenerCallbackID;
@property (nonatomic, copy) NSString *deepLink;
@property (nonatomic, assign) BOOL autoLaunchMessageCenter;
@property (nonatomic, strong) NSMutableDictionary *pendingEvents;
@end

@implementation UAirshipPlugin

// Config keys
NSString *const ProductionAppKeyConfigKey = @"com.urbanairship.production_app_key";
NSString *const ProductionAppSecretConfigKey = @"com.urbanairship.production_app_secret";
NSString *const DevelopmentAppKeyConfigKey = @"com.urbanairship.development_app_key";
NSString *const DevelopmentAppSecretConfigKey = @"com.urbanairship.development_app_secret";
NSString *const ProductionConfigKey = @"com.urbanairship.in_production";
NSString *const EnablePushOnLaunchConfigKey = @"com.urbanairship.enable_push_onlaunch";
NSString *const ClearBadgeOnLaunchConfigKey = @"com.urbanairship.clear_badge_onlaunch";
NSString *const EnableAnalyticsConfigKey = @"com.urbanairship.enable_analytics";
NSString *const AutoLaunchMessageCenterKey = @"com.urbanairship.auto_launch_message_center";
NSString *const NotificationPresentationAlertKey = @"com.urbanairship.ios_foreground_notification_presentation_alert";
NSString *const NotificationPresentationBadgeKey = @"com.urbanairship.ios_foreground_notification_presentation_badge";
NSString *const NotificationPresentationSoundKey = @"com.urbanairship.ios_foreground_notification_presentation_sound";

// Events
NSString *const EventPushReceived = @"urbanairship.push";
NSString *const EventNotificationOpened = @"urbanairship.notification_opened";
NSString *const EventInboxUpdated = @"urbanairship.inbox_updated";
NSString *const EventRegistration = @"urbanairship.registration";
NSString *const EventDeepLink = @"urbanairship.deep_link";



- (void)pluginInitialize {
    UA_LINFO("Initializing UrbanAirship cordova plugin.");

    self.pendingEvents = [NSMutableDictionary dictionary];

    NSDictionary *settings = self.commandDelegate.settings;

    if (settings[AutoLaunchMessageCenterKey]) {
        self.autoLaunchMessageCenter = [settings[AutoLaunchMessageCenterKey] boolValue];
    } else {
        self.autoLaunchMessageCenter = YES;
    }

    UAConfig *config = [UAConfig config];
    config.productionAppKey = settings[ProductionAppKeyConfigKey];
    config.productionAppSecret = settings[ProductionAppSecretConfigKey];
    config.developmentAppKey = settings[DevelopmentAppKeyConfigKey];
    config.developmentAppSecret = settings[DevelopmentAppSecretConfigKey];
    config.inProduction = [settings[ProductionConfigKey] boolValue];
    config.developmentLogLevel = UALogLevelTrace;
    config.productionLogLevel = UALogLevelTrace;

    // Analytics. Enabled by Default
    if (settings[EnableAnalyticsConfigKey] != nil) {
        config.analyticsEnabled = [settings[EnableAnalyticsConfigKey] boolValue];
    }

    // Create Airship singleton that's used to talk to Urban Airship servers.
    // Please populate AirshipConfig.plist with your info from http://go.urbanairship.com
    [UAirship takeOff:config];

    [UAirship push].userPushNotificationsEnabledByDefault = [settings[EnablePushOnLaunchConfigKey] boolValue];

    if (settings[ClearBadgeOnLaunchConfigKey] == nil || [settings[ClearBadgeOnLaunchConfigKey] boolValue]) {
        [[UAirship push] resetBadge];
    }

    [UAirship push].pushNotificationDelegate = self;
    [UAirship push].registrationDelegate = self;
    [UAirship inbox].delegate = self;

    // Set the iOS default foreground presentation options if specified in the config else default to None
    UNNotificationPresentationOptions options = UNNotificationPresentationOptionNone;

    if (settings[NotificationPresentationAlertKey] != nil) {
        if ([settings[NotificationPresentationAlertKey] boolValue]) {
            options = options | UNNotificationPresentationOptionAlert;
        }
    }

    if (settings[NotificationPresentationBadgeKey] != nil) {
        if ([settings[NotificationPresentationBadgeKey] boolValue]) {
            options = options | UNNotificationPresentationOptionBadge;
        }
    }

    if (settings[NotificationPresentationSoundKey] != nil) {
        if ([settings[NotificationPresentationSoundKey] boolValue]) {
            options = options | UNNotificationPresentationOptionSound;
        }
    }

    UA_LDEBUG(@"Foreground presentation options from the config: %lu", (unsigned long)options);
    [UAirship push].defaultPresentationOptions = options;

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(inboxUpdated)
                                                 name:UAInboxMessageListUpdatedNotification
                                               object:nil];

    __weak UAirshipPlugin *weakSelf = self;
    UAAction *customDLA = [UAAction actionWithBlock: ^(UAActionArguments *args, UAActionCompletionHandler handler)  {
        if ([args.value isKindOfClass:[NSURL class]]) {
            weakSelf.deepLink = [args.value absoluteString];
        } else {
            weakSelf.deepLink = args.value;
        }
        
        NSDictionary *data;
        data = @{ @"deepLink":weakSelf.deepLink};

        if (![weakSelf notifyListener:EventDeepLink data:data]) {
            [weakSelf.pendingEvents setValue:data forKey:EventDeepLink];
        }

        handler([UAActionResult resultWithValue:args.value]);
    } acceptingArguments:^BOOL(UAActionArguments *arg)  {
        if (arg.situation == UASituationBackgroundPush || arg.situation == UASituationBackgroundInteractiveButton) {
            return NO;
        }

        return [arg.value isKindOfClass:[NSURL class]] || [arg.value isKindOfClass:[NSString class]];
    }];

    [[UAirship shared].actionRegistry updateAction:customDLA forEntryWithName:kUADeepLinkActionDefaultRegistryName];
}

- (void)dealloc {
    [UAirship push].pushNotificationDelegate = nil;
    [UAirship push].registrationDelegate = nil;
    [UAirship inbox].delegate = nil;
}

/**
 * Helper method to create a plugin result with the specified value.
 *
 * @param value The result's value.
 * @param status The result's status.
 * @returns A CDVPluginResult with specified value.
 */
- (CDVPluginResult *)pluginResultForValue:(id)value status:(CDVCommandStatus)status{
    /*
     NSString -> String
     NSNumber --> (Integer | Double)
     NSArray --> Array
     NSDictionary --> Object
     NSNull --> no return value
     nil -> no return value
     */

    // String
    if ([value isKindOfClass:[NSString class]]) {
        return [CDVPluginResult resultWithStatus:status
                                 messageAsString:[value stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }

    // Number
    if ([value isKindOfClass:[NSNumber class]]) {
        CFNumberType numberType = CFNumberGetType((CFNumberRef)value);
        //note: underlyingly, BOOL values are typedefed as char
        if (numberType == kCFNumberIntType || numberType == kCFNumberCharType) {
            return [CDVPluginResult resultWithStatus:status messageAsInt:[value intValue]];
        } else  {
            return [CDVPluginResult resultWithStatus:status messageAsDouble:[value doubleValue]];
        }
    }

    // Array
    if ([value isKindOfClass:[NSArray class]]) {
        return [CDVPluginResult resultWithStatus:status messageAsArray:value];
    }

    // Object
    if ([value isKindOfClass:[NSDictionary class]]) {
        return [CDVPluginResult resultWithStatus:status messageAsDictionary:value];
    }

    // Null
    if ([value isKindOfClass:[NSNull class]]) {
        return [CDVPluginResult resultWithStatus:status];
    }

    // Nil
    if (!value) {
        return [CDVPluginResult resultWithStatus:status];
    }

    UA_LERR(@"Cordova callback block returned unrecognized type: %@", NSStringFromClass([value class]));
    return [CDVPluginResult resultWithStatus:status];
}

/**
 * Helper method to perform a cordova command asynchronously.
 *
 * @param command The cordova command.
 * @param block The UACordovaExecutionBlock to execute.
 */
- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command withBlock:(UACordovaExecutionBlock)block {
    [self.commandDelegate runInBackground:^{
        UACordovaCompletionHandler completionHandler = ^(CDVCommandStatus status, id value) {
            CDVPluginResult *result = [self pluginResultForValue:value status:status];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        };

        if (!block) {
            completionHandler(CDVCommandStatus_OK, nil);
        } else {
            block(command.arguments, completionHandler);
        }
    }];
}

#pragma mark Phonegap bridge

- (void)registerListener:(CDVInvokedUrlCommand *)command {
    self.listenerCallbackID = command.callbackId;

    for (NSString *event in self.pendingEvents) {
        [self notifyListener:event data:self.pendingEvents[event]];
    }

    [self.pendingEvents removeAllObjects];
}

- (void)setNotificationTypes:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        UANotificationOptions types = [[args objectAtIndex:0] intValue];

        UA_LDEBUG(@"Setting notification types: %ld", (long)types);
        [UAirship push].notificationOptions = types;
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship push].userPushNotificationsEnabled = enabled;

        //forces a reregistration
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setDisplayASAPEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship inAppMessaging].displayASAPEnabled = enabled;
        
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship location].locationUpdatesEnabled = enabled;

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setBackgroundLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship location].backgroundLocationUpdatesAllowed = enabled;

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setAnalyticsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship shared].analytics.enabled = enabled;

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setAssociatedIdentifier:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *key = [args objectAtIndex:0];
        NSString *identifier = [args objectAtIndex:1];

        UAAssociatedIdentifiers *identifiers = [[UAirship shared].analytics currentAssociatedDeviceIdentifiers];
        [identifiers setIdentifier:identifier forKey:key];
        [[UAirship shared].analytics associateDeviceIdentifiers:identifiers];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)isAnalyticsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship shared].analytics.enabled;

        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)isUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship push].userPushNotificationsEnabled;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship push].quietTimeEnabled;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)isInQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL inQuietTime;
        NSDictionary *quietTimeDictionary = [UAirship push].quietTime;
        if (quietTimeDictionary) {
            NSString *start = [quietTimeDictionary valueForKey:@"start"];
            NSString *end = [quietTimeDictionary valueForKey:@"end"];

            NSDateFormatter *df = [NSDateFormatter new];
            df.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
            df.dateFormat = @"HH:mm";

            NSDate *startDate = [df dateFromString:start];
            NSDate *endDate = [df dateFromString:end];

            NSDate *now = [NSDate date];

            inQuietTime = ([now earlierDate:startDate] == startDate && [now earlierDate:endDate] == now);
        } else {
            inQuietTime = NO;
        }

        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:inQuietTime]);
    }];
}

- (void)isLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship location].locationUpdatesEnabled;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)isBackgroundLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship location].backgroundLocationUpdatesAllowed;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)getLaunchNotification:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        id event = [self pushEventFromNotification:self.launchNotificationResponse.notificationContent];

        if ([args firstObject]) {
            self.launchNotificationResponse = nil;
        }

        completionHandler(CDVCommandStatus_OK, event);
    }];
}

- (void)getDeepLink:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *deepLink = self.deepLink;

        if ([args firstObject]) {
            self.deepLink = nil;
        }

        completionHandler(CDVCommandStatus_OK, deepLink);
    }];
}

- (void)getChannelID:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [UAirship push].channelID ?: @"");
    }];
}

- (void)getQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSDictionary *quietTimeDictionary = [UAirship push].quietTime;

        if (quietTimeDictionary) {

            NSString *start = [quietTimeDictionary objectForKey:@"start"];
            NSString *end = [quietTimeDictionary objectForKey:@"end"];

            NSDateFormatter *df = [NSDateFormatter new];
            df.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
            df.dateFormat = @"HH:mm";

            NSDate *startDate = [df dateFromString:start];
            NSDate *endDate = [df dateFromString:end];

            // these will be nil if the dateformatter can't make sense of either string
            if (startDate && endDate) {
                NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
                NSDateComponents *startComponents = [gregorian components:NSHourCalendarUnit|NSMinuteCalendarUnit fromDate:startDate];
                NSDateComponents *endComponents = [gregorian components:NSHourCalendarUnit|NSMinuteCalendarUnit fromDate:endDate];

                completionHandler(CDVCommandStatus_OK, @{ @"startHour": @(startComponents.hour),
                                                          @"startMinute": @(startComponents.minute),
                                                          @"endHour": @(endComponents.hour),
                                                          @"endMinute": @(endComponents.minute) });

                return;
            }
        }

        completionHandler(CDVCommandStatus_OK, @{ @"startHour": @(0),
                                                  @"startMinute": @(0),
                                                  @"endHour": @(0),
                                                  @"endMinute": @(0) });
    }];
}

- (void)getTags:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [UAirship push].tags ?: [NSArray array]);
    }];
}

- (void)getAlias:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [UAirship push].alias ?: @"");
    }];
}

- (void)getBadgeNumber:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, @([UIApplication sharedApplication].applicationIconBadgeNumber));
    }];
}

- (void)getNamedUser:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [UAirship namedUser].identifier ?: @"");
    }];
}

- (void)setTags:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSMutableArray *tags = [NSMutableArray arrayWithArray:[args objectAtIndex:0]];
        [UAirship push].tags = tags;
        [[UAirship push] updateRegistration];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setAlias:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *alias = [args objectAtIndex:0];
        // If the value passed in is nil or an empty string, set the alias to nil. Empty string will cause registration failures
        // from the Urban Airship API
        alias = [alias stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        if ([alias length] == 0) {
            [UAirship push].alias = nil;
        }
        else{
            [UAirship push].alias = alias;
        }
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}



- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship push].quietTimeEnabled = enabled;
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        id startHr = [args objectAtIndex:0];
        id startMin = [args objectAtIndex:1];
        id endHr = [args objectAtIndex:2];
        id endMin = [args objectAtIndex:3];

        [[UAirship push] setQuietTimeStartHour:[startHr integerValue] startMinute:[startMin integerValue] endHour:[endHr integerValue] endMinute:[endMin integerValue]];
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *number = [args objectAtIndex:0];
        BOOL enabled = [number boolValue];
        [UAirship push].autobadgeEnabled = enabled;

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setBadgeNumber:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        id number = [args objectAtIndex:0];
        NSInteger badgeNumber = [number intValue];
        [[UAirship push] setBadgeNumber:badgeNumber];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setNamedUser:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *namedUserID = [args objectAtIndex:0];
        namedUserID = [namedUserID stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

        [UAirship namedUser].identifier = [namedUserID length] ? namedUserID : nil;

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editNamedUserTagGroups:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        UANamedUser *namedUser = [UAirship namedUser];
        for (NSDictionary *operation in [args objectAtIndex:0]) {
            NSString *group = operation[@"group"];
            if ([operation[@"operation"] isEqualToString:@"add"]) {
                [namedUser addTags:operation[@"tags"] group:group];
            } else if ([operation[@"operation"] isEqualToString:@"remove"]) {
                [namedUser removeTags:operation[@"tags"] group:group];
            }
        }

        [namedUser updateTags];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editChannelTagGroups:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        for (NSDictionary *operation in [args objectAtIndex:0]) {
            NSString *group = operation[@"group"];
            if ([operation[@"operation"] isEqualToString:@"add"]) {
                [[UAirship push] addTags:operation[@"tags"] group:group];
            } else if ([operation[@"operation"] isEqualToString:@"remove"]) {
                [[UAirship push] removeTags:operation[@"tags"] group:group];
            }
        }

        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)resetBadge:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [[UAirship push] resetBadge];
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)runAction:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *actionName = [args firstObject];
        id actionValue = args.count >= 2 ? [args objectAtIndex:1] : nil;

        [UAActionRunner runActionWithName:actionName
                                    value:actionValue
                                situation:UASituationManualInvocation
                        completionHandler:^(UAActionResult *actionResult) {

                            if (actionResult.status == UAActionStatusCompleted) {

                                /*
                                 * We are wrapping the value in an object to be consistent
                                 * with the Android implementation.
                                 */

                                NSMutableDictionary *result = [NSMutableDictionary dictionary];
                                [result setValue:actionResult.value forKey:@"value"];
                                completionHandler(CDVCommandStatus_OK, result);
                            } else {
                                NSString *error = [self errorMessageForAction:actionName result:actionResult];
                                completionHandler(CDVCommandStatus_ERROR, error);
                            }
                        }];

    }];
}

- (void)isAppNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL optedIn = [UAirship push].authorizedNotificationOptions != 0;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:optedIn]);
    }];
}

/**
 * Helper method to create an error message from an action result.
 *
 * @param actionName The name of the action.
 * @param actionResult The action result.
 * @return An error message, or nil if no error was found.
 */
- (NSString *)errorMessageForAction:(NSString *)actionName result:(UAActionResult *)actionResult {
    switch (actionResult.status) {
        case UAActionStatusActionNotFound:
            return [NSString stringWithFormat:@"Action %@ not found.", actionName];
        case UAActionStatusArgumentsRejected:
            return [NSString stringWithFormat:@"Action %@ rejected its arguments.", actionName];
        case UAActionStatusError:
            if (actionResult.error.localizedDescription) {
                return actionResult.error.localizedDescription;
            }
        case UAActionStatusCompleted:
            return nil;
    }

    return [NSString stringWithFormat:@"Action %@ failed with unspecified error", actionName];
}


#pragma mark UARegistrationDelegate

- (void)registrationSucceededForChannelID:(NSString *)channelID deviceToken:(NSString *)deviceToken {
    UA_LINFO(@"Channel registration successful %@.", channelID);

    NSDictionary *data;
    if (deviceToken) {
        data = @{ @"channelID":channelID, @"deviceToken":deviceToken };
    } else {
        data = @{ @"channelID":channelID };
    }

    [self notifyListener:EventRegistration data:data];
}

- (void)registrationFailed {
    UA_LINFO(@"Channel registration failed.");
    [self notifyListener:EventRegistration data:@{ @"error": @"Registration failed." }];
}

#pragma mark UAPushNotificationDelegate

- (void)receivedNotificationResponse:(UANotificationResponse *)notificationResponse completionHandler:(void(^)())completionHandler {
    if ([notificationResponse.actionIdentifier isEqualToString:UANotificationDefaultActionIdentifier]) {
        UA_LDEBUG(@"The application was launched or resumed from a notification %@", notificationResponse);

        self.launchNotificationResponse = notificationResponse;

        id event = [self pushEventFromNotification:notificationResponse.notificationContent];
        if (![self notifyListener:EventNotificationOpened data:event]) {
            [self.pendingEvents setValue:event forKey:EventNotificationOpened];
        }
    }

    completionHandler();
}

- (void)receivedForegroundNotification:(UANotificationContent *)notificationContent completionHandler:(void(^)())completionHandler {
    UA_LDEBUG(@"Received a notification while the app was already in the foreground %@", notificationContent);

    [[UAirship push] setBadgeNumber:0]; // zero badge after push received

    id event = [self pushEventFromNotification:notificationContent];
    [self notifyListener:EventPushReceived data:event];
    completionHandler();
}

#pragma mark UAInboxDelegate

- (void)showInboxMessage:(UAInboxMessage *)message {
    if (self.autoLaunchMessageCenter) {
        [[UAirship defaultMessageCenter] displayMessage:message];
    }
}

- (void)showInbox {
    if (self.autoLaunchMessageCenter) {
        [[UAirship defaultMessageCenter] display];
    }
}

#pragma mark Message Center

- (void)displayMessageCenter:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[UAirship defaultMessageCenter] display];
        });
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

#pragma mark Inbox

- (void)inboxUpdated {
    UA_LDEBUG(@"Inbox updated");
    [self notifyListener:EventInboxUpdated data:nil];
}

- (void)getInboxMessages:(CDVInvokedUrlCommand *)command {
    UA_LDEBUG(@"Getting messages");

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSMutableArray *messages = [NSMutableArray array];

        for (UAInboxMessage *message in [UAirship inbox].messageList.messages) {

            NSDictionary *icons = [message.rawMessageObject objectForKey:@"icons"];
            NSString *iconUrl = [icons objectForKey:@"list_icon"];
            NSNumber *sentDate = @([message.messageSent timeIntervalSince1970] * 1000);

            NSMutableDictionary *messageInfo = [NSMutableDictionary dictionary];
            [messageInfo setValue:message.title forKey:@"title"];
            [messageInfo setValue:message.messageID forKey:@"id"];
            [messageInfo setValue:sentDate forKey:@"sentDate"];
            [messageInfo setValue:iconUrl forKey:@"listIconUrl"];
            [messageInfo setValue:message.unread ? @NO : @YES  forKey:@"isRead"];
            [messageInfo setValue:message.extra forKey:@"extras"];

            [messages addObject:messageInfo];
        }

        completionHandler(CDVCommandStatus_OK, messages);
    }];
}

- (void)markInboxMessageRead:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            completionHandler(CDVCommandStatus_ERROR, error);
            return;
        }

        [[UAirship inbox].messageList markMessagesRead:@[message] completionHandler:nil];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)deleteInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            completionHandler(CDVCommandStatus_ERROR, error);
            return;
        }

        [[UAirship inbox].messageList markMessagesDeleted:@[message] completionHandler:nil];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)displayInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            completionHandler(CDVCommandStatus_ERROR, error);
            return;
        }

        UAMessageViewController *mvc = [[UAMessageViewController alloc] initWithNibName:@"UADefaultMessageCenterMessageViewController"
                                                                                 bundle:[UAirship resources]];
        mvc.message = message;

        UINavigationController *navController =  [[UINavigationController alloc] initWithRootViewController:mvc];

        dispatch_async(dispatch_get_main_queue(), ^{
            [[UAUtils topController] presentViewController:navController animated:YES completion:nil];
        });

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)overlayInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            completionHandler(CDVCommandStatus_ERROR, error);
            return;
        }

        dispatch_async(dispatch_get_main_queue(), ^{
            [UALandingPageOverlayController showMessage:message];
        });

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)refreshInbox:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [[UAirship inbox].messageList retrieveMessageListWithSuccessBlock:^{
            completionHandler(CDVCommandStatus_OK, nil);
        } withFailureBlock:^{
            completionHandler(CDVCommandStatus_ERROR, @"Inbox failed to refresh");
        }];
    }];
}

- (BOOL)notifyListener:(NSString *)eventType data:(NSDictionary *)data {
    if (!self.listenerCallbackID) {
        UA_LTRACE(@"Listener callback unavailable.  event %@", eventType);
        return NO;
    }

    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setValue:eventType forKey:@"eventType"];
    [message setValue:data forKey:@"eventData"];

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [result setKeepCallbackAsBool:YES];

    [self.commandDelegate sendPluginResult:result callbackId:self.listenerCallbackID];
    return YES;
}


- (id)pushEventFromNotification:(UANotificationContent *)notificationContent {
    if (!notificationContent) {
        return @{ @"message": @"", @"extras": @{}};
    }

    // remove extraneous key/value pairs
    NSMutableDictionary *extras = [NSMutableDictionary dictionaryWithDictionary:notificationContent.notificationInfo];

    if([[extras allKeys] containsObject:@"aps"]) {
        [extras removeObjectForKey:@"aps"];
    }

    if([[extras allKeys] containsObject:@"_"]) {
        [extras removeObjectForKey:@"_"];
    }

    NSMutableDictionary *result = [NSMutableDictionary dictionary];
    result[@"message"] = notificationContent.alertBody ?: @"";
    result[@"extras"] = extras;
    return result;
}


@end
