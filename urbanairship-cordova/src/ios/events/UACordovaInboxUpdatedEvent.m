/* Copyright Urban Airship and Contributors */

#import "UACordovaInboxUpdatedEvent.h"

NSString *const EventInboxUpdated = @"urbanairship.inbox_updated";

@implementation UACordovaInboxUpdatedEvent

+ (instancetype)event {
    return [[UACordovaInboxUpdatedEvent alloc] init];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.data = nil;
        self.type = EventInboxUpdated;
    }
    return self;
}

@end
