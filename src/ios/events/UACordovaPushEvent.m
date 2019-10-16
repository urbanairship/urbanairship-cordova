/* Copyright Urban Airship and Contributors */

#import "UACordovaPushEvent.h"

NSString *const EventPushReceived = @"urbanairship.push";

@implementation UACordovaPushEvent

+ (instancetype)eventWithNotificationContent:(UANotificationContent *)content  {
    return [[self alloc] initWithNotificationContent:content];
}

- (instancetype)initWithNotificationContent:(UANotificationContent *)content {
    self = [super init];

    if (self) {
        self.type = EventPushReceived;
        self.data = [[self class] pushEventDataFromNotificationContent:content];
    }

    return self;
}

+ (NSDictionary *)pushEventDataFromNotificationContent:(UANotificationContent *)notificationContent {
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

@end
