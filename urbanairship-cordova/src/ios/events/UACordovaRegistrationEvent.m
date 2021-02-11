/* Copyright Urban Airship and Contributors */

#import "UACordovaRegistrationEvent.h"

NSString *const EventRegistration = @"urbanairship.registration";

@implementation UACordovaRegistrationEvent

+ (instancetype)registrationSucceededEventWithChannelID:channelID deviceToken:(NSString *)deviceToken {
    return [[self alloc] initWithData:[self registrationSucceededData:channelID deviceToken:deviceToken]];
}

+ (instancetype)registrationFailedEvent {
    return [[self alloc] initWithData:[self registrationFailedData]];
}

- (instancetype)initWithData:(NSDictionary *)data {
    self = [super init];
    if (self) {
        self.type = EventRegistration;
        self.data = data;
    }
    return self;
}

+ (NSDictionary *)registrationSucceededData:(NSString *)channelID deviceToken:(NSString *)deviceToken {
    NSDictionary *data;

    if (deviceToken) {
        data = @{ @"channelID":channelID, @"deviceToken":deviceToken, @"registrationToken":deviceToken };
    } else {
        data = @{ @"channelID":channelID };
    }

    return data;
}

+ (NSDictionary *)registrationFailedData {
    return @{ @"error": @"Registration failed." };
}

@end
