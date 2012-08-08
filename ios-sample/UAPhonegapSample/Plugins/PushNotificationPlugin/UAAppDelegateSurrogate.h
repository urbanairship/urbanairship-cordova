
#import <Foundation/Foundation.h>
#import "UAGlobal.h"

@interface UAAppDelegateSurrogate : NSObject <UIApplicationDelegate> {
    NSObject <UIApplicationDelegate> *defaultAppDelegate;
    NSObject <UIApplicationDelegate> *surrogateDelegate;
    NSDictionary *launchOptions;
}

SINGLETON_INTERFACE(UAAppDelegateSurrogate);

@property(nonatomic, assign) NSObject<UIApplicationDelegate> *surrogateDelegate;
@property(nonatomic, readonly, copy) NSDictionary *launchOptions;

@end
