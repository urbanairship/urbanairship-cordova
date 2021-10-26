/* Copyright Urban Airship and Contributors */

#import "UACordovaPushEvent.h"

NSString *const EventPushReceived = @"urbanairship.push";

@implementation UACordovaPushEvent

+ (instancetype)eventWithNotificationContent:(NSDictionary *)userInfo {
    return [[self alloc] initWithNotificationContent:userInfo];
}

- (instancetype)initWithNotificationContent:(NSDictionary *)userInfo {
    self = [super init];

    if (self) {
        self.type = EventPushReceived;
        self.data = [[self class] pushEventDataFromNotificationContent:userInfo];
    }

    return self;
}

+ (NSDictionary *)pushEventDataFromNotificationContent:(NSDictionary *)userInfo {
    if (!userInfo) {
        return @{ @"message": @"", @"extras": @{}};
    }

    NSMutableDictionary *info = [NSMutableDictionary dictionaryWithDictionary:userInfo];

    // remove the send ID
    if([[info allKeys] containsObject:@"_"]) {
        [info removeObjectForKey:@"_"];
    }

    NSMutableDictionary *result = [NSMutableDictionary dictionary];

    // If there is an aps dictionary in the extras, remove it and set it as a top level object
    if([[info allKeys] containsObject:@"aps"]) {
        NSDictionary* aps = info[@"aps"];

        if ([[aps allKeys] containsObject:@"alert"]) {

            NSDictionary *alert = aps[@"alert"];
            if ([[alert allKeys] containsObject:@"body"]) {
                result[@"message"] = alert[@"body"];
            } else {
                result[@"message"] = @"";
            }
            if ([[alert allKeys] containsObject:@"title"]) {
                [result setValue:alert[@"title"] forKey:@"title"];
            }
            if ([[alert allKeys] containsObject:@"subtitle"]) {
                [result setValue:alert[@"subtitle"] forKey:@"subtitle"];
            }
        }
        result[@"aps"] = info[@"aps"];
        [info removeObjectForKey:@"aps"];
    }

    // Set the remaining info as extras
    result[@"extras"] = info;

    return result;
}

@end
