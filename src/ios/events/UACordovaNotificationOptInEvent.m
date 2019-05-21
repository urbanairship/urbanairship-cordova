/* Copyright Urban Airship and Contributors */

#import "UACordovaNotificationOptInEvent.h"

NSString *const EventNotificationOptInStatus = @"urbanairship.notification_opt_in_status";

@implementation UACordovaNotificationOptInEvent

+ (instancetype)eventWithData:(NSDictionary *)data  {
    return [[UACordovaNotificationOptInEvent alloc] initWithData:data];
}

- (instancetype)initWithData:(NSDictionary *)data {
    self = [super init];
    if (self) {
        self.type = EventNotificationOptInStatus;
        self.data = data;
    }
    return self;
}

@end
