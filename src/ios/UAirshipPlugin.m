/*
 Copyright 2009-2013 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binaryform must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided withthe distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC``AS IS'' AND ANY EXPRESS OR
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

typedef id (^UACordovaCallbackBlock)(NSArray *args);
typedef void (^UACordovaVoidCallbackBlock)(NSArray *args);

@interface UAirshipPlugin()
@property (nonatomic, copy) NSDictionary *incomingNotification;
@property (nonatomic, copy) NSString *registrationCallbackID;
@property (nonatomic, copy) NSString *pushCallbackID;
@end

@implementation UAirshipPlugin

NSString *const ProductionAppKeyConfigKey = @"com.urbanairship.production_app_key";
NSString *const ProductionAppSecretConfigKey = @"com.urbanairship.production_app_secret";
NSString *const DevelopmentAppKeyConfigKey = @"com.urbanairship.development_app_key";
NSString *const DevelopmentAppSecretConfigKey = @"com.urbanairship.development_app_secret";
NSString *const ProductionConfigKey = @"com.urbanairship.in_production";
NSString *const EnablePushOnLaunchConfigKey = @"com.urbanairship.enable_push_onlaunch";
NSString *const ClearBadgeOnLaunchConfigKey = @"com.urbanairship.clear_badge_onlaunch";

- (void)pluginInitialize {
    UA_LINFO("Initializing UrbanAirship cordova plugin.");
    [self takeOff];
}

- (void)takeOff {
    NSDictionary *settings = self.commandDelegate.settings;

    UAConfig *config = [UAConfig config];
    config.productionAppKey = settings[ProductionAppKeyConfigKey];
    config.productionAppSecret = settings[ProductionAppSecretConfigKey];
    config.developmentAppKey = settings[DevelopmentAppKeyConfigKey];
    config.developmentAppSecret = settings[DevelopmentAppSecretConfigKey];
    config.inProduction = [settings[ProductionConfigKey] boolValue];

    // Create Airship singleton that's used to talk to Urban Airship servers.
    // Please populate AirshipConfig.plist with your info from http://go.urbanairship.com
    [UAirship takeOff:config];

    [UAirship push].userPushNotificationsEnabledByDefault = [settings[EnablePushOnLaunchConfigKey] boolValue];

    if (settings[ClearBadgeOnLaunchConfigKey] == nil || [settings[ClearBadgeOnLaunchConfigKey] boolValue]) {
        [[UAirship push] resetBadge];
    }

    [UAirship push].pushNotificationDelegate = self;
    [UAirship push].registrationDelegate = self;

    [[UAirship shared].locationService startReportingSignificantLocationChanges];
}

- (CDVPluginResult *)pluginResultForValue:(id)value {
    CDVPluginResult *result;

    /*
     NSSString -> String
     NSNumber --> (Integer | Double)
     NSArray --> Array
     NSDictionary --> Object
     NSNull --> no return value
     */

    if ([value isKindOfClass:[NSString class]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:[value stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    } else if ([value isKindOfClass:[NSNumber class]]) {
        CFNumberType numberType = CFNumberGetType((CFNumberRef)value);
        //note: underlyingly, BOOL values are typedefed as char
        if (numberType == kCFNumberIntType || numberType == kCFNumberCharType) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:[value intValue]];
        } else  {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:[value doubleValue]];
        }
    } else if ([value isKindOfClass:[NSArray class]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:value];
    } else if ([value isKindOfClass:[NSDictionary class]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:value];
    } else if ([value isKindOfClass:[NSNull class]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        UA_LERR(@"Cordova callback block returned unrecognized type: %@", NSStringFromClass([value class]));
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    return result;
}

- (void)performCallbackWithCommand:(CDVInvokedUrlCommand*)command withBlock:(UACordovaCallbackBlock)block {
    dispatch_async(dispatch_get_main_queue(), ^{
        //execute the block. the return value should be an obj-c object holding what we want to pass back to cordova.
        id returnValue = block ? block(command.arguments) : nil;

        CDVPluginResult *result = [self pluginResultForValue:returnValue];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    });
}

- (void)performCallbackWithCommand:(CDVInvokedUrlCommand*)command withVoidBlock:(UACordovaVoidCallbackBlock)block {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args) {
        if (block) {
            block(args);
        }
        
        return [NSNull null];
    }];
}

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

- (NSMutableDictionary *)extrasForUserInfo:(NSDictionary *)userInfo {

    // remove extraneous key/value pairs
    NSMutableDictionary *extras = [NSMutableDictionary dictionaryWithDictionary:userInfo];

    if([[extras allKeys] containsObject:@"aps"]) {
        [extras removeObjectForKey:@"aps"];
    }
    if([[extras allKeys] containsObject:@"_uamid"]) {
        [extras removeObjectForKey:@"_uamid"];
    }
    if([[extras allKeys] containsObject:@"_"]) {
        [extras removeObjectForKey:@"_"];
    }

    return extras;
}

#pragma mark Phonegap bridge

//registration

- (void)registerChannelListener:(CDVInvokedUrlCommand*)command {
    self.registrationCallbackID = command.callbackId;
}

- (void)registerPushListener:(CDVInvokedUrlCommand*)command {
    self.pushCallbackID = command.callbackId;
}

- (void)registerForNotificationTypes:(CDVInvokedUrlCommand*)command {
    UA_LDEBUG(@"Register for notification types");
    
    CDVPluginResult* pluginResult = nil;
    
    if (command.arguments.count >= 1) {
        id obj = [command.arguments objectAtIndex:0];
        
        if ([obj isKindOfClass:[NSNumber class]]) {
            UIUserNotificationType bitmask = [obj intValue];
            UALOG(@"bitmask value: %d", [obj intValue]);
            
            [UAirship push].userNotificationTypes = bitmask;
            [[UAirship push] updateRegistration];
            
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
        
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

//general enablement

- (void)setUserNotificationsEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship push].userPushNotificationsEnabled = enabled;

        //forces a reregistration
        [[UAirship push] updateRegistration];
    }];
}

- (void)setLocationEnabled:(CDVInvokedUrlCommand*)command {
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

- (void)setBackgroundLocationEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args){
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        [UAirship shared].locationService.backgroundLocationServiceEnabled = enabled;
    }];
}

- (void)setAnalyticsEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship shared].analytics.enabled = enabled;
    }];
}

- (void)isAnalyticsEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship shared].analytics.enabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

//getters

- (void)isUserNotificationsEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship push].userPushNotificationsEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship push].quietTimeEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isInQuietTime:(CDVInvokedUrlCommand*)command {
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

- (void)isLocationEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UALocationService airshipLocationServiceEnabled];
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isBackgroundLocationEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        BOOL enabled = [UAirship shared].locationService.backgroundLocationServiceEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)getIncoming:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *incomingAlert = @"";
        NSMutableDictionary *incomingExtras = [NSMutableDictionary dictionary];

        if (self.incomingNotification) {
            incomingAlert = [self alertForUserInfo:self.incomingNotification];
            [incomingExtras setDictionary:[self extrasForUserInfo:self.incomingNotification]];
        }

        NSMutableDictionary *returnDictionary = [NSMutableDictionary dictionary];

        [returnDictionary setObject:incomingAlert forKey:@"message"];
        [returnDictionary setObject:incomingExtras forKey:@"extras"];

        //reset incoming push data until the next background push comes in
        self.incomingNotification = nil;

        return returnDictionary;
    }];
}

- (void)getChannelID:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return [UAirship push].channelID ?: @"";
    }];
}

- (void)getQuietTime:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSDictionary *quietTimeDictionary = [UAirship push].quietTime;
        //initialize the returned dictionary with zero values
        NSNumber *zero = [NSNumber numberWithInt:0];
        NSDictionary *returnDictionary = [NSDictionary dictionaryWithObjectsAndKeys:zero,@"startHour",
                                          zero,@"startMinute",
                                          zero,@"endHour",
                                          zero,@"endMinute",nil];

        //this can be nil if quiet time is not set
        if (quietTimeDictionary) {

            NSString *start = [quietTimeDictionary objectForKey:@"start"];
            NSString *end = [quietTimeDictionary objectForKey:@"end"];

            NSDateFormatter *df = [NSDateFormatter new];
            df.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
            df.dateFormat = @"HH:mm";

            NSDate *startDate = [df dateFromString:start];
            NSDate *endDate = [df dateFromString:end];

            //these will be nil if the dateformatter can't make sense of either string
            if (startDate && endDate) {

                NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];

                NSDateComponents *startComponents = [gregorian components:NSHourCalendarUnit|NSMinuteCalendarUnit fromDate:startDate];
                NSDateComponents *endComponents = [gregorian components:NSHourCalendarUnit|NSMinuteCalendarUnit fromDate:endDate];

                NSNumber *startHr = [NSNumber numberWithInt:startComponents.hour];
                NSNumber *startMin = [NSNumber numberWithInt:startComponents.minute];
                NSNumber *endHr = [NSNumber numberWithInt:endComponents.hour];
                NSNumber *endMin = [NSNumber numberWithInt:endComponents.minute];

                returnDictionary = [NSDictionary dictionaryWithObjectsAndKeys:startHr,@"startHour",startMin,@"startMinute",
                                    endHr,@"endHour",endMin,@"endMinute",nil];
            }
        }
        return returnDictionary;
    }];
}

- (void)getTags:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSArray *tags = [UAirship push].tags? : [NSArray array];
        NSDictionary *returnDictionary = [NSDictionary dictionaryWithObjectsAndKeys:tags, @"tags", nil];
        return returnDictionary;
    }];
}

- (void)getAlias:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        NSString *alias = [UAirship push].alias ?: @"";
        return alias;
    }];
}

- (void)getBadgeNumber:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return @([UIApplication sharedApplication].applicationIconBadgeNumber);
    }];
}

- (void)getNamedUser:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args){
        return [UAirship push].namedUser.identifier ?: @"";
    }];
}

//setters

- (void)setTags:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSMutableArray *tags = [NSMutableArray arrayWithArray:[args objectAtIndex:0]];
        [UAirship push].tags = tags;
        [[UAirship push] updateRegistration];
    }];
}

- (void)setAlias:(CDVInvokedUrlCommand*)command {
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



- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship push].quietTimeEnabled = enabled;
        [[UAirship push] updateRegistration];
    }];
}

- (void)setQuietTime:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        id startHr = [args objectAtIndex:0];
        id startMin = [args objectAtIndex:1];
        id endHr = [args objectAtIndex:2];
        id endMin = [args objectAtIndex:3];

        [[UAirship push] setQuietTimeStartHour:[startHr integerValue] startMinute:[startMin integerValue] endHour:[endHr integerValue] endMinute:[endMin integerValue]];
        [[UAirship push] updateRegistration];
    }];
}

- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSNumber *number = [args objectAtIndex:0];
        BOOL enabled = [number boolValue];
        [UAirship push].autobadgeEnabled = enabled;
    }];
}

- (void)setBadgeNumber:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        id number = [args objectAtIndex:0];
        NSInteger badgeNumber = [number intValue];
        [[UAirship push] setBadgeNumber:badgeNumber];
    }];
}

- (void)setNamedUser:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        NSString *namedUserID = [args objectAtIndex:0];
        namedUserID = [namedUserID stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

        [UAirship push].namedUser.identifier = [namedUserID length] ? namedUserID : nil;
    }];
}

//reset badge

- (void)resetBadge:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        [[UAirship push] resetBadge];
        [[UAirship push] updateRegistration];
    }];
}

//location recording

- (void)recordCurrentLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command withVoidBlock:^(NSArray *args) {
        [[UAirship shared].locationService reportCurrentLocation];
    }];
}

- (void)runAction:(CDVInvokedUrlCommand*)command {
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
        NSDictionary *data = @{ @"channelID":channelID };
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
    self.incomingNotification = notification;
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

#pragma mark Other stuff

- (void)dealloc {
    [UAirship push].pushNotificationDelegate = nil;
    [UAirship push].registrationDelegate = nil;
}

- (void)failIfSimulator {
    if ([[[UIDevice currentDevice] model] compare:@"iPhone Simulator"] == NSOrderedSame) {
        UIAlertView *someError = [[UIAlertView alloc] initWithTitle:@"Notice"
                                                            message:@"You will not be able to recieve push notifications in the simulator."
                                                           delegate:self
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
        
        [someError show];
    }
}

@end
