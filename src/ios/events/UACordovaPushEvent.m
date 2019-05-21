/* Copyright Urban Airship and Contributors */

#import "UACordovaPushEvent.h"

NSString *const EventPushReceived = @"urbanairship.push";

@implementation UACordovaPushEvent

+ (instancetype)eventWithData:(NSDictionary *)data  {
    return [[UACordovaPushEvent alloc] initWithData:data];
}

- (instancetype)initWithData:(NSDictionary *)data {
    self = [super init];
    if (self) {
        self.type = EventPushReceived;
        self.data = data;
    }
    return self;
}

@end
