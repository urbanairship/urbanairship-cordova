/* Copyright Urban Airship and Contributors */

#import "UACordovaNotificationOpenedEvent.h"

NSString *const EventNotificationOpened = @"urbanairship.notification_opened";

@implementation UACordovaNotificationOpenedEvent

- (instancetype)init {
    self = [super init];
    if (self) {
        self.data = nil;
        self.type = EventNotificationOpened;
    }
    return self;
}

+ (instancetype)eventWithData:(NSDictionary *)data  {
    return [[UACordovaNotificationOpenedEvent alloc] initWithData:data];
}

- (instancetype)initWithData:(NSDictionary *)data {
    self = [super init];
    if (self) {
        self.type = EventNotificationOpened;
        self.data = data;
    }
    return self;
}

@end
