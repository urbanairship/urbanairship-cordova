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
        return @{@"extras": @{}};
    }

    NSMutableDictionary *info = [NSMutableDictionary dictionaryWithDictionary:userInfo];

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

    // Set the remaining info as extras
    result[@"extras"] = info;

    return result;
}

@end
