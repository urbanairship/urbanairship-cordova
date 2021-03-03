/* Copyright Urban Airship and Contributors */

#import "UACordovaShowInboxEvent.h"

NSString *const EventShowInbox = @"urbanairship.show_inbox";

@implementation UACordovaShowInboxEvent

+ (instancetype)event {
    return [[UACordovaShowInboxEvent alloc] initWithMessageID:nil];
}

+ (instancetype)eventWithMessageID:(NSString *)identifier {
    return [[UACordovaShowInboxEvent alloc] initWithMessageID:identifier];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.type = EventShowInbox;
        self.data = nil;
    }
    return self;
}

- (instancetype)initWithMessageID:(nullable NSString *)identifier {
    self = [super init];
    if (self) {
        self.type = EventShowInbox;
        self.data = identifier ? @{@"messageId":identifier} : @{};
    }

    return self;
}

@end
