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
#import "UALocationService.h"
#import "UAConfig.h"
#import "NSJSONSerialization+UAAdditions.h"
#import "UAActionRunner.h"
#import "UADefaultMessageCenter.h"
#import "UAInbox.h"
#import "UAInboxMessageList.h"
#import "UAInboxMessage.h"
#import "UALandingPageOverlayController.h"
#import "UAMessageViewController.h"
#import "UAUtils.h"

typedef id (^UACordovaCallbackBlock)(NSArray *args);
typedef void (^UACordovaVoidCallbackBlock)(NSArray *args);

@interface UAirshipPlugin()
@property (nonatomic, copy) NSDictionary *launchNotification;
@property (nonatomic, copy) NSString *registrationCallbackID;
@property (nonatomic, copy) NSString *pushCallbackID;
@property (nonatomic, copy) NSString *inboxCallbackID;
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

- (void)pluginInitialize {
    UA_LINFO("Initializing UrbanAirship cordova plugin.");

    NSDictionary *settings = self.commandDelegate.settings;

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

    if ([UALocationService airshipLocationServiceEnabled]) {
        [[UAirship shared].locationService startReportingSignificantLocationChanges];
    }

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(inboxUpdated)
                                                 name:UAInboxMessageListUpdatedNotification
                                               object:nil];
}

- (void)dealloc {
    [UAirship push].pushNotificationDelegate = nil;
    [UAirship push].registrationDelegate = nil;
}

/**
 * Helper method to create a plugin result with the specified value.
 *
 * @param value The result's value.
 * @returns A CDVPluginResult with specified value.
 */
- (CDVPluginResult *)pluginResultForValue:(id)value {
    /*
     NSString -> String
     NSNumber --> (Integer | Double)
     NSArray --> Array
     NSDictionary --> Object
     NSNull --> no return value
     */

    if ([value isKindOfClass:[CDVPluginResult class]]) {
        return value;
    }

    // String
    if ([value isKindOfClass:[NSString class]]) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                 messageAsString:[value stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }

    // Number
    if ([value isKindOfClass:[NSNumber class]]) {
        CFNumberType numberType = CFNumberGetType((CFNumberRef)value);
        //note: underlyingly, BOOL values are typedefed as char
        if (numberType == kCFNumberIntType || numberType == kCFNumberCharType) {
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:[value intValue]];
        } else  {
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:[value doubleValue]];
        }
    }

    // Array
    if ([value isKindOfClass:[NSArray class]]) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:value];
    }

    // Object
    if ([value isKindOfClass:[NSDictionary class]]) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:value];
    }

    // Null
    if ([value isKindOfClass:[NSNull class]]) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }

    UA_LERR(@"Cordova callback block returned unrecognized type: %@", NSStringFromClass([value class]));
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
}

/**
 * Helper method to perform a cordova command with a return type. The command will
 * automatically be dispatched on the main queue.
 *
 * @param command The cordova command.
 * @param block The UACordovaCallbackBlock to execute.
 */
- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command withBlock:(UACordovaCallbackBlock)block {
    dispatch_async(dispatch_get_main_queue(), ^{
        //execute the block. the return value should be an obj-c object holding what we want to pass back to cordova.
        id returnValue = block ? block(command.arguments) : nil;

        CDVPluginResult *result = [self pluginResultForValue:returnValue];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    });
}

/**
 * Helper method to perform a cordova command without a return type. The command will
 * automatically be dispatched on the main queue.
 *
 * @param command The cordova command.
 * @param block The UACordovaCallbackBlock to execute.
 */
- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command withVoidBlock:(UACordovaVoidCallbackBlock)block {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args) {
        if (block) {
            block(args);
        }

        return [NSNull null];
    }];
}

/**
 * Helper method to parse the alert from a notification.
 *
 * @param userInfo The notification.
 * @return The notification's alert.
 */
- (NSString *)alertForUserInfo:(NSDictionary *)userInfo {
    NSString *alert = @"";

    if ([[userInfo allKeys] containsObject:@"aps"]) {
        NSDictionary *apsDict = [userInfo objectForKey:@"aps"];
        //TODO: what do we want to do in the case of a localized alert dictionary?
        if ([[apsDict valueForKey:@"alert"] isKindOfClass:[NSString class]]) {
            alert = [apsDict valueForKey:@"alert"];
        }
    }

    return alert;
}

/**
 * Helper method to parse the extras from a notification.
 *
 * @param userInfo The notification.
 * @return The notification's extras.
 */
- (NSMutableDictionary *)extrasForUserInfo:(NSDictionary *)userInfo {

    // remove extraneous key/value pairs
    NSMutableDictionary *extras = [NSMutableDictionary dictionaryWithDictionary:userInfo];

    if([[extras allKeys] containsObject:@"aps"]) {
        [extras removeObjectForKey:@"aps"];
    }
    if([[extras allKeys] containsObject:@"_"]) {
        [extras removeObjectForKey:@"_"];
    }

    return extras;
}

#pragma mark Phonegap bridge

- (void)registerChannelListener:(CDVInvokedUrlCommand *)command {
    self.registrationCallbackID = command.callbackId;
}

- (void)registerPushListener:(CDVInvokedUrlCommand *)command {
    self.pushCallbackID = command.callbackId;
}

- (void)setNotificationTypes:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        UIUserNotificationType types = [[args objectAtIndex:0] intValue];

        UA_LDEBUG(@"Setting notification types: %ld", (long)types);
        [UAirship push].userNotificationTypes = types;
        [[UAirship push] updateRegistration];
    }];
}

- (void)setUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship push].userPushNotificationsEnabled = enabled;

        //forces a reregistration
        [[UAirship push] updateRegistration];
    }];
}

- (void)setLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UALocationService setAirshipLocationServiceEnabled:enabled];

        if (enabled) {
            [[UAirship shared].locationService startReportingSignificantLocationChanges];
        } else {
            [[UAirship shared].locationService stopReportingSignificantLocationChanges];
        }
    }];
}

- (void)setBackgroundLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship shared].locationService.backgroundLocationServiceEnabled = enabled;
    }];
}

- (void)setAnalyticsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship shared].analytics.enabled = enabled;
    }];
}

- (void)isAnalyticsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship shared].analytics.enabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship push].userPushNotificationsEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship push].quietTimeEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isInQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
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

        return [NSNumber numberWithBool:inQuietTime];
    }];
}

- (void)isLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UALocationService airshipLocationServiceEnabled];
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isBackgroundLocationEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship shared].locationService.backgroundLocationServiceEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)getLaunchNotification:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *incomingAlert = @"";
        NSMutableDictionary *incomingExtras = [NSMutableDictionary dictionary];

        if (self.launchNotification) {
            incomingAlert = [self alertForUserInfo:self.launchNotification];
            [incomingExtras setDictionary:[self extrasForUserInfo:self.launchNotification]];
        }

        NSMutableDictionary *returnDictionary = [NSMutableDictionary dictionary];

        [returnDictionary setObject:incomingAlert forKey:@"message"];
        [returnDictionary setObject:incomingExtras forKey:@"extras"];

        if ([args firstObject]) {
            self.launchNotification = nil;
        }

        return returnDictionary;
    }];
}

- (void)getChannelID:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return [UAirship push].channelID ?: @"";
    }];
}

- (void)getQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
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

                return @{ @"startHour": @(startComponents.hour),
                          @"startMinute": @(startComponents.minute),
                          @"endHour": @(endComponents.hour),
                          @"endMinute": @(endComponents.minute) };
            }
        }

        return @{ @"startHour": @(0),
                  @"startMinute": @(0),
                  @"endHour": @(0),
                  @"endMinute": @(0) };
    }];
}

- (void)getTags:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return [UAirship push].tags ?: [NSArray array];
    }];
}

- (void)getAlias:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *alias = [UAirship push].alias ?: @"";
        return alias;
    }];
}

- (void)getBadgeNumber:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return @([UIApplication sharedApplication].applicationIconBadgeNumber);
    }];
}

- (void)getNamedUser:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return [UAirship push].namedUser.identifier ?: @"";
    }];
}

- (void)setTags:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSMutableArray *tags = [NSMutableArray arrayWithArray:[args objectAtIndex:0]];
        [UAirship push].tags = tags;
        [[UAirship push] updateRegistration];
    }];
}

- (void)setAlias:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
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
    }];
}



- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship push].quietTimeEnabled = enabled;
        [[UAirship push] updateRegistration];
    }];
}

- (void)setQuietTime:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        id startHr = [args objectAtIndex:0];
        id startMin = [args objectAtIndex:1];
        id endHr = [args objectAtIndex:2];
        id endMin = [args objectAtIndex:3];

        [[UAirship push] setQuietTimeStartHour:[startHr integerValue] startMinute:[startMin integerValue] endHour:[endHr integerValue] endMinute:[endMin integerValue]];
        [[UAirship push] updateRegistration];
    }];
}

- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *number = [args objectAtIndex:0];
        BOOL enabled = [number boolValue];
        [UAirship push].autobadgeEnabled = enabled;
    }];
}

- (void)setBadgeNumber:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        id number = [args objectAtIndex:0];
        NSInteger badgeNumber = [number intValue];
        [[UAirship push] setBadgeNumber:badgeNumber];
    }];
}

- (void)setNamedUser:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSString *namedUserID = [args objectAtIndex:0];
        namedUserID = [namedUserID stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

        [UAirship push].namedUser.identifier = [namedUserID length] ? namedUserID : nil;
    }];
}

- (void)editNamedUserTagGroups:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {

        UANamedUser *namedUser = [UAirship push].namedUser;
        for (NSDictionary *operation in [args objectAtIndex:0]) {
            NSString *group = operation[@"group"];
            if ([operation[@"operation"] isEqualToString:@"add"]) {
                [namedUser addTags:operation[@"tags"] group:group];
            } else if ([operation[@"operation"] isEqualToString:@"remove"]) {
                [namedUser removeTags:operation[@"tags"] group:group];
            }
        }

        [namedUser updateTags];
    }];
}

- (void)editChannelTagGroups:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {

        for (NSDictionary *operation in [args objectAtIndex:0]) {
            NSString *group = operation[@"group"];
            if ([operation[@"operation"] isEqualToString:@"add"]) {
                [[UAirship push] addTags:operation[@"tags"] group:group];
            } else if ([operation[@"operation"] isEqualToString:@"remove"]) {
                [[UAirship push] removeTags:operation[@"tags"] group:group];
            }
        }

        [[UAirship push] updateRegistration];
    }];
}

- (void)resetBadge:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        [[UAirship push] resetBadge];
        [[UAirship push] updateRegistration];
    }];
}

- (void)recordCurrentLocation:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        [[UAirship shared].locationService reportCurrentLocation];
    }];
}

- (void)runAction:(CDVInvokedUrlCommand *)command {
    NSString *actionName = [command.arguments firstObject];
    id actionValue = command.arguments.count >= 2 ? [command.arguments objectAtIndex:1] : nil;

    [UAActionRunner runActionWithName:actionName
                                value:actionValue
                            situation:UASituationManualInvocation
                    completionHandler:^(UAActionResult *actionResult) {
                        NSDictionary *cordovaResult = [NSMutableDictionary dictionary];

                        if (actionResult.status == UAActionStatusCompleted) {
                            [cordovaResult setValue:actionResult.value forKey:@"value"];
                        } else {
                            NSString *error = [self errorMessageForAction:actionName result:actionResult];
                            [cordovaResult setValue:error forKey:@"error"];
                        }

                        CDVPluginResult *result = [self pluginResultForValue:cordovaResult];
                        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
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

    if (self.registrationCallbackID) {
        NSDictionary *data;
        if (deviceToken) {
            data = @{ @"channelID":channelID, @"deviceToken":deviceToken };
        } else {
            data = @{ @"channelID":channelID };
        }
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.registrationCallbackID];
    }
}

- (void)registrationFailed {
    UA_LINFO(@"Channel registration failed.");

    if (self.registrationCallbackID) {
        NSDictionary *data = @{ @"error": @"Registration failed." };
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.registrationCallbackID];
    }
}

#pragma mark UAPushNotificationDelegate

- (void)launchedFromNotification:(NSDictionary *)notification {
    UA_LDEBUG(@"The application was launched or resumed from a notification %@", [notification description]);
    self.launchNotification = notification;
}

- (void)receivedForegroundNotification:(NSDictionary *)notification {
    UA_LDEBUG(@"Received a notification while the app was already in the foreground %@", [notification description]);

    [[UAirship push] setBadgeNumber:0]; // zero badge after push received

    if (self.pushCallbackID) {
        NSMutableDictionary *data = [NSMutableDictionary dictionary];
        [data setValue:[self alertForUserInfo:notification] forKey:@"message"];
        [data setValue:[self extrasForUserInfo:notification] forKey:@"extras"];

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.pushCallbackID];
    }
}

#pragma mark Message Center

- (void)displayMessageCenter:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        [[UAirship defaultMessageCenter] display];
    }];
}

#pragma mark Inbox

- (void)registerInboxListener:(CDVInvokedUrlCommand *)command {
    self.inboxCallbackID = command.callbackId;
}

- (void)inboxUpdated {
    UA_LDEBUG(@"Inbox updated");

    if (self.inboxCallbackID) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.inboxCallbackID];
    }
}

- (void)getInboxMessages:(CDVInvokedUrlCommand *)command {
    UA_LDEBUG(@"Getting messages");

    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
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

        return messages;
    }];
}

- (void)markInboxMessageRead:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        }

        [[UAirship inbox].messageList markMessagesRead:@[message] completionHandler:nil];
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }];
}

- (void)deleteInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        }

        [[UAirship inbox].messageList markMessagesDeleted:@[message] completionHandler:nil];
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }];
}

- (void)displayInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        }

        UAMessageViewController *mvc = [[UAMessageViewController alloc] initWithNibName:@"UADefaultMessageCenterMessageViewController"
                                                                                 bundle:[UAirship resources]];
        mvc.message = message;

        UINavigationController *navController =  [[UINavigationController alloc] initWithRootViewController:mvc];

        [[UAUtils topController] presentViewController:navController animated:YES completion:nil];
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }];
}

- (void)overlayInboxMessage:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *messageID = [command.arguments firstObject];
        UAInboxMessage *message = [[UAirship inbox].messageList messageForID:messageID];

        if (!message) {
            NSString *error = [NSString stringWithFormat:@"Message not found: %@", messageID];
            return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        }

        [UALandingPageOverlayController showMessage:message];
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }];
}

@end
