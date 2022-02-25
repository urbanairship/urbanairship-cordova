   /* Copyright Urban Airship and Contributors */

   #import "UACordovaEvent.h"

   NS_ASSUME_NONNULL_BEGIN

   extern NSString *const PreferenceCenterLink;

   /**
    * Preference Center event when the open preference center listener is called.
    */
   @interface UACordovaPreferenceCenterEvent : NSObject<UACordovaEvent>

   /**
    * The event type.
    *
    * @return The event type.
    */
   @property (nonatomic, strong, nullable) NSString *type;

   /**
    * The event data.
    *
    * @return The event data.
    */
   @property (nonatomic, strong, nullable) NSDictionary *data;

   /**
    * Preference Center event when the open preference center listener is called.
    *
    * @param preferenceCenterId The preference center Id.
    */
   + (instancetype)eventWithPreferenceCenterId:(NSString *)preferenceCenterId;

   @end

   NS_ASSUME_NONNULL_END

