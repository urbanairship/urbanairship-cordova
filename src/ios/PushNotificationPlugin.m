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

#import "PushNotificationPlugin.h"
#import "UAPush.h"
#import "UAirship.h"
#import "UAAnalytics.h"
#import "UALocationService.h"
#import "UAConfig.h"
#import "NSJSONSerialization+UAAdditions.h"

typedef id (^UACordovaCallbackBlock)(NSArray *args);
typedef void (^UACordovaVoidCallbackBlock)(NSArray *args);

@interface PushNotificationPlugin()
- (void)takeOff;
@property (nonatomic, copy) NSDictionary *incomingNotification;
@end

@implementation PushNotificationPlugin

- (void)pluginInitialize {
    UA_LINFO("Initializing PushNotificationPlugin");
    [self takeOff];
}

- (void)takeOff {
    //Init Airship launch options
    UAConfig *config = [UAConfig defaultConfig];

    NSDictionary *settings = self.commandDelegate.settings;

    config.productionAppKey = [settings valueForKey:@"com.urbanairship.production_app_key"] ?: config.productionAppKey;
    config.productionAppSecret = [settings valueForKey:@"com.urbanairship.production_app_secret"] ?: config.productionAppSecret;
    config.developmentAppKey = [settings valueForKey:@"com.urbanairship.development_app_key"] ?: config.developmentAppKey;
    config.developmentAppSecret = [settings valueForKey:@"com.urbanairship.development_app_secret"] ?: config.developmentAppSecret;
    if ([settings valueForKey:@"com.urbanairship.in_production"]) {
        config.inProduction = [[settings valueForKey:@"com.urbanairship.in_production"] boolValue];
    }

    BOOL enablePushOnLaunch = [[settings valueForKey:@"com.urbanairship.enable_push_onlaunch"] boolValue];
    [UAPush shared].userPushNotificationsEnabledByDefault = enablePushOnLaunch;

    // Create Airship singleton that's used to talk to Urban Airship servers.
    // Please populate AirshipConfig.plist with your info from http://go.urbanairship.com
    [UAirship takeOff:config];

    [[UAPush shared] resetBadge];//zero badge on startup
    [UAPush shared].pushNotificationDelegate = self;
    [UAPush shared].registrationDelegate = self;

    [[UAirship shared].locationService startReportingSignificantLocationChanges];
}

- (void)failWithCallbackID:(NSString *)callbackID {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    [self.commandDelegate sendPluginResult:result callbackId:callbackID];
}

- (void)succeedWithPluginResult:(CDVPluginResult *)result withCallbackID:(NSString *)callbackID {
    [self.commandDelegate sendPluginResult:result callbackId:callbackID];
}

- (BOOL)validateArguments:(NSArray *)args forExpectedTypes:(NSArray *)types {
    if (args.count == types.count) {
        for (int i = 0; i < args.count; i++) {
            if (![[args objectAtIndex:i] isKindOfClass:[types objectAtIndex:i]]) {
                //fail when when there is a type mismatch an expected and passed parameter
                UA_LERR(@"Type mismatch in cordova callback: expected %@ and received %@",
                        [types description], [args description]);
                return NO;
            }
        }
    } else {
        //fail when there is a number mismatch
        UA_LERR(@"Parameter number mismatch in cordova callback: expected %d and received %d", types.count, args.count);
        return NO;
    }

    return YES;
}

- (CDVPluginResult *)pluginResultForValue:(id)value {
    CDVPluginResult *result;

    /*
     NSSString -> String
     NSNumber --> (Integer | Double)
     NSArray --> Array
     NSDictionary --> Object
     nil --> no return value
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
        return nil;
    }

    return result;
}

- (void)performCallbackWithCommand:(CDVInvokedUrlCommand*)command expecting:(NSArray *)expected withBlock:(UACordovaCallbackBlock)block {

    dispatch_async(dispatch_get_main_queue(), ^{
        //if we're expecting any arguments
        if (expected) {
            if (![self validateArguments:command.arguments forExpectedTypes:expected]) {
                [self failWithCallbackID:command.callbackId];
                return;
            }
        } else if(command.arguments.count) {
            UA_LERR(@"Parameter number mismatch: expected 0 and received %d", command.arguments.count);
            [self failWithCallbackID:command.callbackId];
            return;
        }

        //execute the block. the return value should be an obj-c object holding what we want to pass back to cordova.
        id returnValue = block(command.arguments);

        CDVPluginResult *result = [self pluginResultForValue:returnValue];
        if (result) {
            [self succeedWithPluginResult:result withCallbackID:command.callbackId];
        } else {
            [self failWithCallbackID:command.callbackId];
        }
    });
}

- (void)performCallbackWithCommand:(CDVInvokedUrlCommand*)command expecting:(NSArray *)expected withVoidBlock:(UACordovaVoidCallbackBlock)block {
    [self performCallbackWithCommand:command expecting:expected withBlock:^(NSArray *args) {
        block(args);
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

//events

- (void)raisePush:(NSString *)message withExtras:(NSDictionary *)extras {

    if (!message || !extras) {
        UA_LDEBUG(@"PushNotificationPlugin: attempted to raise push with nil message or extras");
        message = @"";
        extras = [NSMutableDictionary dictionary];
    }

    NSMutableDictionary *data = [NSMutableDictionary dictionary];

    [data setObject:message forKey:@"message"];
    [data setObject:extras forKey:@"extras"];

    NSString *json = [NSJSONSerialization stringWithObject:data];
    NSString *js = [NSString stringWithFormat:@"cordova.fireDocumentEvent('urbanairship.push', %@);", json];

    [self.commandDelegate evalJs:js scheduledOnRunLoop:NO];

    UA_LTRACE(@"js callback: %@", js);
}

- (void)raiseRegistration:(BOOL)valid withpushID:(NSString *)pushID {

    if (!pushID) {
        UA_LDEBUG(@"PushNotificationPlugin: attempted to raise registration with nil pushID");
        pushID = @"";
        valid = NO;
    }

    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    if (valid) {
        [data setObject:pushID forKey:@"pushID"];
    } else {
        [data setObject:@"Registration failed." forKey:@"error"];
    }

    NSString *json = [NSJSONSerialization stringWithObject:data];
    NSString *js = [NSString stringWithFormat:@"cordova.fireDocumentEvent('urbanairship.registration', %@);", json];

    [self.commandDelegate evalJs:js scheduledOnRunLoop:NO];

    UA_LTRACE(@"js callback: %@", js);
}

//registration

- (void)registerForNotificationTypes:(CDVInvokedUrlCommand*)command {
    UA_LDEBUG(@"PushNotificationPlugin: register for notification types");
    
    CDVPluginResult* pluginResult = nil;
    
    if (command.arguments.count >= 1) {
        id obj = [command.arguments objectAtIndex:0];
        
        if ([obj isKindOfClass:[NSNumber class]]) {
            UIUserNotificationType bitmask = [obj intValue];
            UALOG(@"bitmask value: %d", [obj intValue]);
            
            [UAPush shared].userNotificationTypes = bitmask;
            [[UAPush shared] updateRegistration];
            
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

- (void)enablePush:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UAPush shared].userPushNotificationsEnabled = YES;
        //forces a reregistration
        [[UAPush shared] updateRegistration];
    }];
}

- (void)disablePush:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UAPush shared].userPushNotificationsEnabled = NO;
        //forces a reregistration
        [[UAPush shared] updateRegistration];
    }];
}

- (void)enableLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UALocationService setAirshipLocationServiceEnabled:YES];
        [[UAirship shared].locationService startReportingSignificantLocationChanges];
    }];
}

- (void)disableLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UALocationService setAirshipLocationServiceEnabled:NO];
        [[UAirship shared].locationService stopReportingSignificantLocationChanges];
    }];
}

- (void)enableBackgroundLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UAirship shared].locationService.backgroundLocationServiceEnabled = YES;
    }];
}

- (void)disableBackgroundLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args){
        [UAirship shared].locationService.backgroundLocationServiceEnabled = NO;
    }];
}

//getters

- (void)isPushEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        BOOL enabled = [UAPush shared].userPushNotificationsEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        BOOL enabled = [UAPush shared].quietTimeEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isInQuietTime:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        BOOL inQuietTime;
        NSDictionary *quietTimeDictionary = [UAPush shared].quietTime;
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
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        BOOL enabled = [UALocationService airshipLocationServiceEnabled];
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)isBackgroundLocationEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        BOOL enabled = [UAirship shared].locationService.backgroundLocationServiceEnabled;
        return [NSNumber numberWithBool:enabled];
    }];
}

- (void)getIncoming:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
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

- (void)getPushID:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        NSString *pushID = [UAirship shared].deviceToken ?: @"";
        return pushID;
    }];
}

- (void)getQuietTime:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        NSDictionary *quietTimeDictionary = [UAPush shared].quietTime;
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
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        NSArray *tags = [UAPush shared].tags? : [NSArray array];
        NSDictionary *returnDictionary = [NSDictionary dictionaryWithObjectsAndKeys:tags, @"tags", nil];
        return returnDictionary;
    }];
}

- (void)getAlias:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withBlock:^(NSArray *args){
        NSString *alias = [UAPush shared].alias ?: @"";
        return alias;
    }];
}

//setters

- (void)setTags:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:[NSArray class],nil] withVoidBlock:^(NSArray *args) {
        NSMutableArray *tags = [NSMutableArray arrayWithArray:[args objectAtIndex:0]];
        [UAPush shared].tags = tags;
        [[UAPush shared] updateRegistration];
    }];
}

- (void)setAlias:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:[NSString class],nil] withVoidBlock:^(NSArray *args) {
        NSString *alias = [args objectAtIndex:0];
        // If the value passed in is nil or an empty string, set the alias to nil. Empty string will cause registration failures
        // from the Urban Airship API
        alias = [alias stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        if ([alias length] == 0) {
            [UAPush shared].alias = nil;
        }
        else{
            [UAPush shared].alias = alias;
        }
        [[UAPush shared] updateRegistration];
    }];
}

- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:[NSNumber class],nil] withVoidBlock:^(NSArray *args) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAPush shared].quietTimeEnabled = enabled;
        [[UAPush shared] updateRegistration];
    }];
}

- (void)setQuietTime:(CDVInvokedUrlCommand*)command {
    Class c = [NSNumber class];
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:c,c,c,c,nil] withVoidBlock:^(NSArray *args) {
        id startHr = [args objectAtIndex:0];
        id startMin = [args objectAtIndex:1];
        id endHr = [args objectAtIndex:2];
        id endMin = [args objectAtIndex:3];

        [[UAPush shared] setQuietTimeStartHour:[startHr integerValue] startMinute:[startMin integerValue] endHour:[endHr integerValue] endMinute:[endMin integerValue]];
        [[UAPush shared] updateRegistration];
    }];
}

- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:[NSNumber class],nil] withVoidBlock:^(NSArray *args) {
        NSNumber *number = [args objectAtIndex:0];
        BOOL enabled = [number boolValue];
        [UAPush shared].autobadgeEnabled = enabled;
    }];
}

- (void)setBadgeNumber:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:[NSArray arrayWithObjects:[NSNumber class],nil] withVoidBlock:^(NSArray *args) {
        id number = [args objectAtIndex:0];
        NSInteger badgeNumber = [number intValue];
        [[UAPush shared] setBadgeNumber:badgeNumber];
    }];
}

//reset badge

- (void)resetBadge:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args) {
        [[UAPush shared] resetBadge];
        [[UAPush shared] updateRegistration];
    }];
}

//location recording

- (void)recordCurrentLocation:(CDVInvokedUrlCommand*)command {
    [self performCallbackWithCommand:command expecting:nil withVoidBlock:^(NSArray *args) {
        [[UAirship shared].locationService reportCurrentLocation];
    }];
}


#pragma mark UARegistrationDelegate
- (void)registrationSucceededForChannelID:(NSString *)channelID deviceToken:(NSString *)deviceToken {
    UA_LINFO(@"PushNotificationPlugin: registered for remote notifications.");

    if (deviceToken) {
        [self raiseRegistration:YES withpushID:deviceToken];
    }
}

- (void)registrationFailed {
    UA_LINFO(@"PushNotificationPlugin: Failed to register for remote notifications.");

    [self raiseRegistration:NO withpushID:@""];
}

#pragma mark UAPushNotificationDelegate
- (void)launchedFromNotification:(NSDictionary *)notification {
    UA_LDEBUG(@"The application was launched or resumed from a notification %@", [notification description]);
    self.incomingNotification = notification;
}

- (void)receivedForegroundNotification:(NSDictionary *)notification {
    UA_LDEBUG(@"Received a notification while the app was already in the foreground %@", [notification description]);

    [[UAPush shared] setBadgeNumber:0]; // zero badge after push received

    NSString *alert = [self alertForUserInfo:notification];
    NSMutableDictionary *extras = [self extrasForUserInfo:notification];

    [self raisePush:alert withExtras:extras];
}


#pragma mark Other stuff

- (void)dealloc {
    [UAPush shared].pushNotificationDelegate = nil;
    [UAPush shared].registrationDelegate = nil;

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
