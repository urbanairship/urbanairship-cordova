//
// Copyright 2012 Urban Airship and Contributors

#import <Foundation/Foundation.h>
#import "UAGlobal.h"

/** Thrown if the default delegate is nil, which indicates that the 
 * delgate exchange at startup was unsuccessful.
 */
extern NSString * const UADefaultDelegateNilException;

@interface UAAppDelegateSurrogate : NSObject <UIApplicationDelegate> {
    NSObject <UIApplicationDelegate> *defaultAppDelegate;
    NSObject <UIApplicationDelegate> *surrogateDelegate;
    NSDictionary *launchOptions;
}

SINGLETON_INTERFACE(UAAppDelegateSurrogate);

@property(nonatomic, assign) NSObject<UIApplicationDelegate> *surrogateDelegate;
@property(nonatomic, assign) NSObject<UIApplicationDelegate> *defaultAppDelegate;
@property(nonatomic, readonly, copy) NSDictionary *launchOptions;

- (void)clearLaunchOptions;

@end
