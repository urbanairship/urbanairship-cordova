/* Copyright Urban Airship and Contributors */


#if __has_include(<urbanairship-cordova/cordova_airship-Swift.h>)
#import <urbanairship-cordova/cordova_airship-Swift.h>
#else
#import "cordova_airship-Swift.h"
#endif



#import "UAirshipPlugin.h"
#import "UACordovaPluginManager.h"
#import "UACordovaPushEvent.h"

NSString *const PreferenceCenterIdKey = @"id";
NSString *const PreferenceCenterSectionsKey = @"sections";
NSString *const PreferenceCenterDisplayKey = @"display";
NSString *const PreferenceCenterDisplayNameKey = @"name";
NSString *const PreferenceCenterDisplayDescriptionKey = @"description";
NSString *const PreferenceCenterItemsKey = @"items";
NSString *const PreferenceCenterSubscriptionIdKey = @"subscriptionId";
NSString *const PreferenceCenterComponentsKey = @"components";
NSString *const PreferenceCenterScopesKey = @"scopes";
NSString *const PreferenceCenterScopeWebKey = @"web";
NSString *const PreferenceCenterScopeEmailKey = @"email";
NSString *const PreferenceCenterScopeAppKey = @"app";
NSString *const PreferenceCenterScopeSmsKey = @"sms";

typedef void (^UACordovaCompletionHandler)(CDVCommandStatus, id);
typedef void (^UACordovaExecutionBlock)(NSArray *args, UACordovaCompletionHandler completionHandler);

@interface UAirshipPlugin() <UACordovaPluginManagerDelegate>
@property (nonatomic, copy) NSString *listenerCallbackID;
@property (nonatomic, strong) UACordovaPluginManager *pluginManager;
@property (nonatomic, weak) UAInAppMessageHTMLAdapter *htmlAdapter;
@property (nonatomic, assign) BOOL factoryBlockAssigned;
@end

@implementation UAirshipPlugin

- (void)pluginInitialize {
    UA_LINFO(@"Initializing UrbanAirship cordova plugin.");

    if (!self.pluginManager) {
        self.pluginManager = [UACordovaPluginManager pluginManagerWithDefaultConfig:self.commandDelegate.settings];
    }

    UA_LDEBUG(@"pluginIntialize called:plugin initializing and attempting takeOff with pluginManager:%@", self.pluginManager);
    [self.pluginManager attemptTakeOff];
}

- (void)dealloc {
    self.pluginManager.delegate = nil;
    self.listenerCallbackID = nil;
}

/**
 * Helper method to create a plugin result with the specified value.
 *
 * @param value The result's value.
 * @param status The result's status.
 * @returns A CDVPluginResult with specified value.
 */
- (CDVPluginResult *)pluginResultForValue:(id)value status:(CDVCommandStatus)status {
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
        NSCharacterSet *characters = [NSCharacterSet URLHostAllowedCharacterSet];
        return [CDVPluginResult resultWithStatus:status
                                 messageAsString:[value stringByAddingPercentEncodingWithAllowedCharacters:characters]];
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
 * Helper method to perform a cordova command.
 *
 * @param command The cordova command.
 * @param block The UACordovaExecutionBlock to execute.
 */
- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command withBlock:(UACordovaExecutionBlock)block {
    [self performCallbackWithCommand:command airshipRequired:YES withBlock:block];
}

/**
 * Helper method to perform a cordova command.
 *
 * @param command The cordova command.
 * @param block The UACordovaExecutionBlock to execute.
 */
- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command
                   airshipRequired:(BOOL)airshipRequired
                         withBlock:(UACordovaExecutionBlock)block {

    if (airshipRequired && !self.pluginManager.isAirshipReady) {
        UA_LERR(@"Unable to run Urban Airship command. Takeoff not called.");
        id result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"TakeOff not called."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    UACordovaCompletionHandler completionHandler = ^(CDVCommandStatus status, id value) {
        CDVPluginResult *result = [self pluginResultForValue:value status:status];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };

    if (!block) {
        completionHandler(CDVCommandStatus_OK, nil);
    } else {
        block(command.arguments, completionHandler);
    }
}

#pragma mark Cordova bridge

- (void)registerListener:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"registerListener called with command: %@ and callback ID:%@", command, command.callbackId);

    self.listenerCallbackID = command.callbackId;

    if (self.listenerCallbackID) {
        self.pluginManager.delegate = self;
    }
}

- (void)takeOff:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"takeOff called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command
                     airshipRequired:NO
                           withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        UA_LDEBUG(@"Performing takeOff with args: %@", args);

        NSDictionary *config = [args objectAtIndex:0];
        
        if (!config[@"production"] || !config[@"development"]) {
            completionHandler(CDVCommandStatus_ERROR, @"Invalid config");
            return;
        }
         

        if (self.pluginManager.isAirshipReady) {
            UA_LINFO(@"TakeOff already called. Config will be applied next app start.");
            completionHandler(CDVCommandStatus_OK, nil);
        }

        if (!self.pluginManager.isAirshipReady) {
            NSError *error;
            id result = [AirshipReactNative.shared takeOffWithJson:config
                                                     launchOptions:nil
                                                             error:&error];
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, nil);
            }
           
            if (!self.pluginManager.isAirshipReady) {
                completionHandler(CDVCommandStatus_ERROR, @"Invalid config. Airship unable to takeOff.");
            }
        }

    }];
}

- (void)setAutoLaunchDefaultMessageCenter:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setAutoLaunchDefaultMessageCenter called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];
        self.pluginManager.autoLaunchMessageCenter = enabled;
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setNotificationTypes:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setNotificationTypes called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        UANotificationOptions types = [[args objectAtIndex:0]];

        [AirshipCordova.shared pushSetNotificationOptions:types];
        
        UA_LDEBUG(@"Setting notification types: %ld", (long)types);
    
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setPresentationOptions:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setPresentationOptions called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        UNNotificationPresentationOptions options = [[args objectAtIndex:0]];

        UA_LDEBUG(@"Setting presentation options types: %ld", (long)options);
        
        [AirshipCordova.shared pushSetForegroundPresentationOptions:options];
    
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setUserNotificationsEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [[args objectAtIndex:0] boolValue];

        UA_LTRACE(@"setUserNotificationsEnabled set to:%@", enabled ? @"true" : @"false");

        [AirshipCordova.shared pushSetUserNotificationsEnabled:enabled];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)enableUserNotifications:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"enableUserNotifications called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
         [AirshipCordova.shared pushEnableUserNotifications:^(BOOL success) {
            completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:success]);
         }];
    }];
}


- (void)setAssociatedIdentifier:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setAssociatedIdentifier called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *key = [args objectAtIndex:0];
        NSString *identifier = [args objectAtIndex:1];

        [AirshipCordova.shared analyticsAssociateIdentifier:identifier key:key];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)isUserNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"isUserNotificationsEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *enabled = [AirshipCordova.shared pushIsUserNotificationsEnabled];
        completionHandler(CDVCommandStatus_OK, enabled);
    }];
}
/*
- (void)isQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"isQuietTimeEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL enabled = [UAirship push].quietTimeEnabled;
        completionHandler(CDVCommandStatus_OK, [NSNumber numberWithBool:enabled]);
    }];
}

- (void)isInQuietTime:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"isInQuietTime called with command arguments: %@", command.arguments);

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
*/
- (void)getLaunchNotification:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getLaunchNotification called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        id event = self.pluginManager.lastReceivedNotificationResponse;

        if ([args firstObject]) {
            self.pluginManager.lastReceivedNotificationResponse = nil;
        }

        completionHandler(CDVCommandStatus_OK, event);
    }];
}

- (void)getDeepLink:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getDeepLink called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *deepLink = self.pluginManager.lastReceivedDeepLink;

        if ([args firstObject]) {
            self.pluginManager.lastReceivedDeepLink = nil;
        }

        completionHandler(CDVCommandStatus_OK, deepLink);
    }];
}

- (void)getChannelID:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getChannelID called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [AirshipCordova.shared channelGetChannelIdOrEmpty] ?: @"");
    }];
}
/*
- (void)getQuietTime:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getQuietTime called with command arguments: %@", command.arguments);

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
                NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSCalendarIdentifierGregorian];
                NSDateComponents *startComponents = [gregorian components:NSCalendarUnitHour|NSCalendarUnitMinute fromDate:startDate];
                NSDateComponents *endComponents = [gregorian components:NSCalendarUnitHour|NSCalendarUnitMinute fromDate:endDate];

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
*/
- (void)getTags:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getTags called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [AirshipCordova shared channelGetTags] ?: [NSArray array]);
    }];
}

- (void)getBadgeNumber:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getBadgeNumber called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [AirshipCordova.shared pushGetBadgeNumber)];
    }];
}

- (void)getNamedUser:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getNamedUser called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        completionHandler(CDVCommandStatus_OK, [AirshipCordova.shared contactGetNamedUserIdOrEmtpy] ?: @"");
    }];
}

- (void)setTags:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setTags called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSMutableArray *tags = [NSMutableArray arrayWithArray:[args objectAtIndex:0]];
        
        [AirshipCordova channelAddTags:tags];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

/*
- (void)setQuietTimeEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setQuietTimeEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *value = [args objectAtIndex:0];
        BOOL enabled = [value boolValue];
        [UAirship push].quietTimeEnabled = enabled;
        [[UAirship push] updateRegistration];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setQuietTime:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setQuietTime called with command arguments: %@", command.arguments);

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
*/
- (void)setAutobadgeEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setAutobadgeEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSNumber *number = [args objectAtIndex:0];
        BOOL enabled = [number boolValue];
        [AirshipCordova.shared pushSetAutobadgeEnabled:enabled];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setBadgeNumber:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setBadgeNumber called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        id number = [args objectAtIndex:0];
        NSInteger badgeNumber = [number intValue];
        [AirshipCordova.shared pushSetBadgeNumber:badgeNumber];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)setNamedUser:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setNamedUser called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *namedUserID = nil;
        if ([[args objectAtIndex:0] isKindOfClass:[NSString class]]) {
            namedUserID = [args objectAtIndex:0];
            namedUserID = [namedUserID stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        }

        if (namedUserID.length) {
            [AirshipCordova.shared contactIdentify:namedUserID];
        } else {
            [AirshipCordova.shared contactReset];
        }
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editNamedUserTagGroups:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editNamedUserTagGroups called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        [AirshipCordova.shared contactEditTagGroups:[args objectAtIndex:0]];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editChannelTagGroups:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editChannelTagGroups called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        [AirshipCordova.shared channelEditTagGroups:[args objectAtIndex:0]];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editChannelSubscriptionLists:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editChannelSubscriptionLists called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared channelEditSubscriptionLists: [args objectAtIndex:0]];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editContactSubscriptionLists:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editContactSubscriptionLists called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared contactEditSubscriptionLists: [args objectAtIndex:0]];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)getChannelSubscriptionLists:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getChannelSubscriptionLists called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        [AirshipCordova.shared channelGetSubscriptionListsWithCompletionHandler:^(NSArray<NSString *> * _Nullable channelSubscriptionLists, NSError * _Nullable error) {
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            }
            if (!channelSubscriptionLists) {
                completionHandler(CDVCommandStatus_ERROR, @"channel subscription list null");
            }
            completionHandler(CDVCommandStatus_OK, channelSubscriptionLists);
        }];

    }];
}

- (void)getContactSubscriptionLists:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getContactSubscriptionLists called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {

        [AirshipCordova.shared contactGetSubscriptionListsWithCompletionHandler:^(NSDictionary<NSString *,UAChannelScopes *> * _Nullable contactSubscriptionLists, NSError * _Nullable error) {
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            }
            if (!contactSubscriptionLists) {
                completionHandler(CDVCommandStatus_ERROR, @"contact subscription list null");
            }

            NSMutableDictionary *contactSubscriptionListDict = [NSMutableDictionary dictionary];
            for (NSString* identifier in contactSubscriptionLists.allKeys) {
                UAChannelScopes *scopes = contactSubscriptionLists[identifier];
                NSMutableArray *scopesArray = [NSMutableArray array];
                for (id scope in scopes.values) {
                    UAChannelScope channelScope = (UAChannelScope)[scope intValue];
                    [scopesArray addObject:[self getScopeString:channelScope]];
                }
                [contactSubscriptionListDict setValue:scopesArray forKey:identifier];
            }
            completionHandler(CDVCommandStatus_OK, contactSubscriptionListDict);
        }];

    }];
}

- (void)trackScreen:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"trackScreen called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *screen = [args objectAtIndex:0];

        UA_LTRACE(@"trackScreen set to:%@", screen);

        [AirshipCordova.shared analyticsTrackScreen:screen];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}
- (void)runAction:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"runAction called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *actionName = [args firstObject];
        id actionValue = args.count >= 2 ? [args objectAtIndex:1] : nil;

        [AirshipCordova.shared actionsRunWithActionName:name
                                                actionValue:value
                                          completionHandler:^(id result , NSError *error) {

            /*
            if (result.status == UAActionStatusCompleted) {

                NSMutableDictionary *result = [NSMutableDictionary dictionary];
                [result setValue:result.value forKey:@"value"];
                completionHandler(CDVCommandStatus_OK, result);
            } else {
                NSString *error = [self errorMessageForAction:actionName result:result];
                completionHandler(CDVCommandStatus_ERROR, error);
            }
             */
            completionHandler(CDVCommandStatus_OK, nil);
        }];

    }];
 
}

- (void)isAppNotificationsEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"isAppNotificationsEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        BOOL optedIn = [AirshipCordova.shared pushGetAuthorizedNotificationSettings] != 0;
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
/*
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
*/

- (void)dismissMessageCenter:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"dismissMessageCenter called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared messageCenterDismiss];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)getInboxMessages:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getInboxMessages called with command arguments: %@", command.arguments);
    UA_LDEBUG(@"Getting messages");

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSMutableArray *messages = [AirshipCordova.shared messageCenterGetMessagesWithCompletionHandler:^(NSArray *result, NSError *error) {
            completionHandler(CDVCommandStatus_OK, result);
        }
    }];
 
}

- (void)markInboxMessageRead:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"markInboxMessageRead called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        [AirshipCordova.shared messageCenterMarkMessageReadWithMessageId:messageId
                                                       completionHandler:^(NSError * error) {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}

- (void)deleteInboxMessage:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"deleteInboxMessage called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *messageID = [command.arguments firstObject];
        [AirshipCordova.shared messageCenterDeleteMessageWithMessageId:messageId
                                                     completionHandler:^(NSError * error) {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
 
}

- (void)displayInboxMessage:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"displayInboxMessage called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
       
        
        [AirshipCordova.shared messageCenterDisplayWithMessageId:[command.arguments firstObject]
                                                           error:&error];
        
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}

- (void)refreshInbox:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"refreshInbox called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared messageCenterRefreshWithCompletionHandler:^(NSError *error) {
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, nil);
            }
        }];
    }];
}

- (void)getActiveNotifications:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getActiveNotifications called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared pushGetActiveNotificationsWithCompletionHandler:^(NSArray<NSDictionary<NSString *,id> *> *result) {
            completionHandler(CDVCommandStatus_OK, result);
        }];
    }];
}

- (void)clearNotification:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"clearNotification called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        
        NSString *identifier = command.arguments.firstObject;
        [AirshipCordova.shared pushClearNotification:identifier];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)clearNotifications:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"clearNotifications called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [AirshipCordova.shared pushClearNotifications];

        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)editChannelAttributes:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editChannelAttributes called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSError *error;
        [AirshipCordova.shared channelEditAttributesWithJson:[args objectAtIndex:0]
                                                           error:&error];
        
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}

- (void)editNamedUserAttributes:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"editNamedUserAttributes called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSError *error;
        [AirshipCordova.shared contactEditAttributesWithJson:[args objectAtIndex:0]
                                                           error:&error];
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}


- (BOOL)notifyListener:(NSString *)eventType data:(NSDictionary *)data {
    UA_LTRACE(@"notifyListener called with event type:%@ and data:%@", eventType, data);

    if (!self.listenerCallbackID) {
        UA_LTRACE(@"Listener callback unavailable, event %@", eventType);
        return false;
    }

    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setValue:eventType forKey:@"eventType"];
    [message setValue:data forKey:@"eventData"];

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [result setKeepCallbackAsBool:YES];

    [self.commandDelegate sendPluginResult:result callbackId:self.listenerCallbackID];

    return true;
}

- (void)enableFeature:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"enableFeature called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSArray *features = [args firstObject];
        if ([self isValidFeature:features]) {
            NSError *error;
            [AirshipCordova.shared privacyManagerEnableFeatureWithFeatures:features error:&error];
            
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, nil);
            }
        } else {
            completionHandler(CDVCommandStatus_ERROR, @"Invalid feature, cancelling the action.");
        }
    }];
}

- (void)disableFeature:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"disableFeature called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSArray *features = [args firstObject];
        if ([self isValidFeature:features]) {
            NSError *error;
            [AirshipCordova.shared privacyManagerEnableFeatureWithFeatures:[self stringToFeature:features] error:&error];
            
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, nil);
            }
        } else {
            completionHandler(CDVCommandStatus_ERROR, @"Invalid feature, cancelling the action.");
        }
    }];
}

- (void)setEnabledFeatures:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setEnabledFeatures called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSArray *features = [args firstObject];
        if ([self isValidFeature:features]) {
            NSError *error;
            [AirshipCordova.shared privacyManagerSetEnabledFeaturesWithFeatures:[self stringToFeature:features] error:&error];
                        
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, nil);
            }
            
        } else {
            completionHandler(CDVCommandStatus_ERROR, @"Invalid feature, cancelling the action.");
        }
    }];
}

- (void)getEnabledFeatures:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getEnabledFeatures called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSError *error;
        id result = [AirshipCordova.shared privacyManagerGetEnabledFeaturesAndReturnError:&error];

        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, [self featureToString:result]);
        }
    }];
}

- (void)isFeatureEnabled:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"isFeatureEnabled called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSArray *features = [args firstObject];
        if ([self isValidFeature:features]) {
            NSError *error;
            id result = [AirshipCordova.shared privacyManagerIsFeatureEnabledWithFeatures:[self stringToFeature:features]
                                                                                        error:&error];
            if (error) {
                completionHandler(CDVCommandStatus_ERROR, error);
            } else {
                completionHandler(CDVCommandStatus_OK, result);
            }
        } else {
            completionHandler(CDVCommandStatus_ERROR, @"Invalid feature, cancelling the action.");
        }
    }];
}

- (void)openPreferenceCenter:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"openPreferenceCenter called with command arguments: %@", command.arguments);

    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *preferenceCenterId = [args firstObject];
        NSError *error;
        [AirshipCordova.shared preferenceCenterDisplayWithPreferenceCenterId:preferenceCenterId
                                                                           error:&error];
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}

- (void)getPreferenceCenterConfig:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getPreferenceCenterConfig called with command arguments: %@", command.arguments);
    NSString *preferenceCenterId = [args firstObject];
    [AirshipCordova.shared preferenceCenterGetConfigWithPreferenceCenterId:preferenceCenterId
                                                             completionHandler:^(id result, NSError *error) {
        
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, result);
        }
        
    }
}

- (void)setUseCustomPreferenceCenterUi:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setUseCustomPreferenceCenterUi called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *preferenceCenterID = [args firstObject];
        BOOL useCustomUI = [[args objectAtIndex:1] boolValue];
        [self.pluginManager setPreferenceCenter:preferenceCenterID useCustomUI:useCustomUI];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)getCurrentLocale:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"getCurrentLocale called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSError *error;
        NSLocale *airshipLocale = [AirshipCordova.shared localeGetLocaleAndReturnError:&error];
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK,  completionHandler(CDVCommandStatus_OK, airshipLocale.localeIdentifier);
        }

    }];
}

- (void)setCurrentLocale:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"setCurrentLocale called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSString *localeIdentifier = [args firstObject];
        NSError *error;
        [AirshipCordova.shared localeSetLocaleOverrideWithLocaleIdentifier:localeIdentifier
                                                                         error:&error];
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK,  completionHandler(CDVCommandStatus_OK, nil);
        }

    }];
}

- (void)clearLocale:(CDVInvokedUrlCommand *)command {
    UA_LTRACE(@"clearLocale called with command arguments: %@", command.arguments);
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSError *error;
        [AirshipCordova.shared localeClearLocaleOverrideAndReturnError:&error];
        if (error) {
            completionHandler(CDVCommandStatus_ERROR, error);
        } else {
            completionHandler(CDVCommandStatus_OK, nil);
        }
    }];
}

- (BOOL)isValidFeature:(NSArray *)features {
    if (!features || [features count] == 0) {
        return NO;
    }
    NSDictionary *authorizedFeatures = [self authorizedFeatures];

    for (NSString *feature in features) {
        if (![authorizedFeatures objectForKey:feature]) {
            return NO;
        }
    }
    return YES;
}

- (UAFeatures)stringToFeature:(NSArray *)features {
    NSDictionary *authorizedFeatures = [self authorizedFeatures];

    NSNumber *objectFeature = authorizedFeatures[[features objectAtIndex:0]];
    UAFeatures convertedFeatures = [objectFeature longValue];

    if ([features count] > 1) {
        int i;
        for (i = 1; i < [features count]; i++) {
            NSNumber *objectFeature = authorizedFeatures[[features objectAtIndex:i]];
            convertedFeatures |= [objectFeature longValue];
        }
    }
    return convertedFeatures;
}

- (NSArray *)featureToString:(UAFeatures)features {
    NSMutableArray *convertedFeatures = [[NSMutableArray alloc] init];

    NSDictionary *authorizedFeatures = [self authorizedFeatures];

    if (features == UAFeaturesAll) {
        [convertedFeatures addObject:@"FEATURE_ALL"];
    } else if (features == UAFeaturesNone) {
        [convertedFeatures addObject:@"FEATURE_NONE"];
    } else {
        for (NSString *feature in authorizedFeatures) {
            NSNumber *objectFeature = authorizedFeatures[feature];
            long longFeature = [objectFeature longValue];
            if ((longFeature & features) && (longFeature != UAFeaturesAll)) {
                [convertedFeatures addObject:feature];
            }
        }
    }
    return convertedFeatures;
}

- (NSDictionary *)authorizedFeatures {
    NSMutableDictionary *authorizedFeatures = [[NSMutableDictionary alloc] init];
    [authorizedFeatures setValue:@(UAFeaturesNone) forKey:@"FEATURE_NONE"];
    [authorizedFeatures setValue:@(UAFeaturesInAppAutomation) forKey:@"FEATURE_IN_APP_AUTOMATION"];
    [authorizedFeatures setValue:@(UAFeaturesMessageCenter) forKey:@"FEATURE_MESSAGE_CENTER"];
    [authorizedFeatures setValue:@(UAFeaturesPush) forKey:@"FEATURE_PUSH"];
    [authorizedFeatures setValue:@(UAFeaturesAnalytics) forKey:@"FEATURE_ANALYTICS"];
    [authorizedFeatures setValue:@(UAFeaturesTagsAndAttributes) forKey:@"FEATURE_TAGS_AND_ATTRIBUTES"];
    [authorizedFeatures setValue:@(UAFeaturesContacts) forKey:@"FEATURE_CONTACTS"];
    [authorizedFeatures setValue:@(UAFeaturesAll) forKey:@"FEATURE_ALL"];
    return authorizedFeatures;
}

@end

