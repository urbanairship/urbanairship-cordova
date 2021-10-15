/* Copyright Urban Airship and Contributors */

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#else
@import AirshipKit;
#endif

#import "UACordovaNotificationOpenedEvent.h"

NSString *const EventNotificationOpened = @"urbanairship.notification_opened";

@implementation UACordovaNotificationOpenedEvent

- (instancetype)initWithNotificationResponse:(UNNotificationResponse *)response {
    self = [super init];

    if (self) {
        self.type = EventNotificationOpened;

        NSDictionary *pushEvent = [[self class] pushEventDataFromNotificationContent:response.notification.request.content.userInfo];
        NSMutableDictionary *data = [NSMutableDictionary dictionaryWithDictionary:pushEvent];

        if ([response.actionIdentifier isEqualToString:UNNotificationDefaultActionIdentifier]) {
            [data setValue:@(YES) forKey:@"isForeground"];
        } else {
            UNNotificationAction *notificationAction = [self notificationActionForCategory:response.notification.request.content.categoryIdentifier
                                                                          actionIdentifier:response.actionIdentifier];

            BOOL isForeground = notificationAction.options & UNNotificationActionOptionForeground;
            [data setValue:@(isForeground) forKey:@"isForeground"];
            [data setValue:response.actionIdentifier forKey:@"actionID"];
        }

        self.data = data;
    }

    return self;
}

+ (instancetype)eventWithNotificationResponse:(UNNotificationResponse *)response {
    return [[self alloc] initWithNotificationResponse:response];
}

- (UNNotificationAction *)notificationActionForCategory:(NSString *)category actionIdentifier:(NSString *)identifier {
    NSSet *categories = [UAirship push].combinedCategories;

    UNNotificationCategory *notificationCategory;
    UNNotificationAction *notificationAction;

    for (UNNotificationCategory *possibleCategory in categories) {
        if ([possibleCategory.identifier isEqualToString:category]) {
            notificationCategory = possibleCategory;
            break;
        }
    }

    if (!notificationCategory) {
        UA_LERR(@"Unknown notification category identifier %@", category);
        return nil;
    }

    NSMutableArray *possibleActions = [NSMutableArray arrayWithArray:notificationCategory.actions];

    for (UNNotificationAction *possibleAction in possibleActions) {
        if ([possibleAction.identifier isEqualToString:identifier]) {
            notificationAction = possibleAction;
            break;
        }
    }

    if (!notificationAction) {
        UA_LERR(@"Unknown notification action identifier %@", identifier);
        return nil;
    }

    return notificationAction;
}

@end
