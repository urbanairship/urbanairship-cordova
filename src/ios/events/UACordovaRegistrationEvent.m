/* Copyright Urban Airship and Contributors */

#import "UACordovaRegistrationEvent.h"

NSString *const EventRegistration = @"urbanairship.registration";

@implementation UACordovaRegistrationEvent

+ (instancetype)eventWithData:(NSDictionary *)data  {
    return [[UACordovaRegistrationEvent alloc] initWithData:data];
}

- (instancetype)initWithData:(NSDictionary *)data {
    self = [super init];
    if (self) {
        self.type = EventRegistration;
        self.data = data;
    }
    return self;
}

@end
