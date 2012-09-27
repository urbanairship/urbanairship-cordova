//
//  UAPhoneGapSampleTest.m
//  UAPhoneGapSampleTest
//
//  Created by Matt Hooge on 9/27/12.
//
//

#import "UAAppDelegateSurrogate.h"
#import "AppDelegate.h"
#import "PushNotificationPlugin.h"

#import "NSObject+UASwizzle.h"

#import <objc/runtime.h>
#import <SenTestingKit/SenTestingKit.h>

/** AppDelegate Category
 * Using categories, associated objects, and method swizzling, this will expand the existing
 * class by one property, one method, and one ivar (effectivly). The additional method gets 
 * swizzled to map to an existing method on the AppDelegate, and calling the existing method will
 * call this method instead, thus resulting in a BOOL change throught the associated object.
 */

@interface AppDelegate (UATest)

/** New property that wraps/unwraps and associated object */
@property (nonatomic, assign) BOOL messageReceived;

/** Sets the associated object to YES */
- (void)setMessageReceivedToYes;

@end

@implementation AppDelegate (UATest)

/** Required void pointer that acts as a key for the associated object */
static char appDelegateMessageReceivedKey;

/** Assocaited object that wraps the BOOL value */
static NSNumber *appDelegateMessageReceivedBool = nil;

- (void)setMessageReceived:(BOOL)messageReceived {
    if (appDelegateMessageReceivedBool) {
        objc_setAssociatedObject(self, &appDelegateMessageReceivedKey, nil, OBJC_ASSOCIATION_RETAIN);
        appDelegateMessageReceivedBool = nil;
    }
    appDelegateMessageReceivedBool = [NSNumber numberWithBool:messageReceived];
    objc_setAssociatedObject(self,
                             &appDelegateMessageReceivedKey,
                             appDelegateMessageReceivedBool,
                             OBJC_ASSOCIATION_RETAIN);
}

- (BOOL)messageReceived {
    return [objc_getAssociatedObject(self, &appDelegateMessageReceivedKey) boolValue];
}

- (void)setMessageReceivedToYes {
    self.messageReceived = YES;
}
@end

/** This follows the exact same pattern as the above category */
@interface PushNotificationPlugin (UATest)

@property (nonatomic, assign) BOOL messageReceived;

- (void)setMessageReceivedToYes;
@end

@implementation PushNotificationPlugin (UATest)

static char pushNotficationPluginMessageReceivedKey = 0x00;
static NSNumber *pushNotificationPluginMessageReceivedBool = nil;

- (BOOL)messageReceived {
    return [objc_getAssociatedObject(self, &pushNotficationPluginMessageReceivedKey) boolValue];
}

- (void)setMessageReceived:(BOOL)messageReceived {
    if (pushNotificationPluginMessageReceivedBool) {
        objc_setAssociatedObject(self, &pushNotficationPluginMessageReceivedKey, nil, OBJC_ASSOCIATION_RETAIN);
        pushNotificationPluginMessageReceivedBool = nil;
    }
    pushNotificationPluginMessageReceivedBool = [NSNumber numberWithBool:messageReceived];
    objc_setAssociatedObject(self,
                             &pushNotficationPluginMessageReceivedKey,
                             pushNotificationPluginMessageReceivedBool,
                             OBJC_ASSOCIATION_RETAIN);
}

- (void)setMessageReceivedToYes {
    self.messageReceived = YES;
}

@end


@interface UAPhoneGapSampleTest : SenTestCase

@property (nonatomic, retain) NSMutableSet *nsObjectSelectors;
@property (nonatomic, retain) NSMutableSet *appDelegateSelectors;
@property (nonatomic, retain) NSMutableSet *pushPluginSelectors;

@property (nonatomic, assign) BOOL messageReceived;

@end

@implementation UAPhoneGapSampleTest

#pragma mark -
#pragma mark Setup

- (void)tearDown {
    [_nsObjectSelectors autorelease];
    _nsObjectSelectors = nil;
    [_appDelegateSelectors autorelease];
    _nsObjectSelectors = nil;
    [_pushPluginSelectors autorelease];
    _pushPluginSelectors = nil;
    _messageReceived = NO;
}

#pragma mark -
#pragma mark Test

- (void)testMessageForwardingInUAAppDelegateSurrogate {

    // Get all the selectors from each of the two delegates, then remove the NSObject selectors
    // and the category methods that we added for the purposes of swizzling
    [self setupTestSelectorsForDelegates];
    NSMutableSet *categoryMethods = [self methodsForCategories];
    [_appDelegateSelectors minusSet:_nsObjectSelectors];
    [_pushPluginSelectors minusSet:_nsObjectSelectors];
    [_appDelegateSelectors minusSet:categoryMethods];
    [_pushPluginSelectors minusSet:categoryMethods];
    
    // Assign the two delegates to the surrogate delegate
    UAAppDelegateSurrogate *surrogate = [UAAppDelegateSurrogate shared];
    surrogate.defaultAppDelegate = [[[AppDelegate alloc] init] autorelease];
    surrogate.surrogateDelegate = [[[PushNotificationPlugin alloc] init] autorelease];
    
    // Setup test values
    [(AppDelegate*)surrogate.defaultAppDelegate setMessageReceived:NO];
    [(PushNotificationPlugin*)surrogate.surrogateDelegate setMessageReceived:NO];
    STAssertFalse([(AppDelegate*)surrogate.defaultAppDelegate messageReceived], @"AppDelegate messageReceived should be NO");
    STAssertFalse([(PushNotificationPlugin*)surrogate.surrogateDelegate messageReceived], @"PushNotificationPlugin messageReceived should be NO");
    
    // Test random selectors mapped to the individual objects to see if they respond appropriately
    SEL randomAppDelSelector = NSSelectorFromString([_appDelegateSelectors anyObject]);
    SEL randomPushPluginSelector = NSSelectorFromString([_pushPluginSelectors anyObject]);
    SEL messageReceivedToYes = @selector(setMessageReceivedToYes);
    STAssertTrue([surrogate respondsToSelector:randomAppDelSelector], @"UAAppDelegate should respond to selector %@", randomAppDelSelector);
    STAssertTrue([surrogate respondsToSelector:randomPushPluginSelector], @"UAAppDelegate should respond to selector %@", randomPushPluginSelector);
    STAssertFalse([surrogate respondsToSelector:@selector(crappySelector)], @"UAAppDelegate should not respond to selector");
    
    
    // Selector for the AppDelegate
    SEL appDelegateDidFinishLaunching = @selector(application:didFinishLaunchingWithOptions:);
    // Selector for the PushPlugin
    SEL pushPluginDidRegisterRemoteNotifications = @selector(application:didRegisterForRemoteNotificationsWithDeviceToken:);
    
    // Swizzle messages and make sure the make it through the surrogate delegate to their intented targets
    NSError *swizzleError = nil;
    [[surrogate.defaultAppDelegate class] ua_swizzleMethod:appDelegateDidFinishLaunching withMethod:messageReceivedToYes error:&swizzleError];
    STAssertNil(swizzleError, @"Swizzle error should be nil, was %@", swizzleError.localizedDescription);
    [surrogate performSelector:appDelegateDidFinishLaunching];
    STAssertTrue([(AppDelegate*)surrogate.defaultAppDelegate messageReceived], @"surrogate.defaultAppDelegate should have received message from selector %@", randomAppDelSelector);
    
    swizzleError = nil;
    [[surrogate.surrogateDelegate class] ua_swizzleMethod:pushPluginDidRegisterRemoteNotifications withMethod:messageReceivedToYes error:&swizzleError];
    STAssertNil(swizzleError, @"Swizzle error should be nil, was %@", swizzleError.localizedDescription);
    [surrogate performSelector:pushPluginDidRegisterRemoteNotifications];
    STAssertTrue([(PushNotificationPlugin*)surrogate.surrogateDelegate messageReceived], @"surrogate.surrogateDelegate should have received message from selector %@", randomPushPluginSelector);

}


#pragma mark -
#pragma mark Support Methods

/** 
 * Build a the set of category methods added to the objects under test, and remove them.
 * This makes the tests brittle in the sense that this needs to be maintained, but allows the 
 * classes themselves to by dynamic since we aren't hard coding specifiy methods to call for
 * testing. If you don't keep the category methods up to date, you can swizzle them by mistake
 * and then things get crazy. 
 */
- (NSMutableSet*)methodsForCategories {
    NSMutableSet *categoryMethods = [NSMutableSet setWithCapacity:3];
    // Set the @property methods
    [categoryMethods addObject:NSStringFromSelector(@selector(messageReceived))];
    [categoryMethods addObject:NSStringFromSelector(@selector(setMessageReceived:))];
    // Set the test method that's going to be swizzled.
    [categoryMethods addObject:NSStringFromSelector(@selector(setMessageReceivedToYes))];
    return categoryMethods;
}

/** 
 * Collect all the methods for each object using the objc runtime 
 */
- (void)setupTestSelectorsForDelegates {
    self.nsObjectSelectors = [self setOfSelectorsForClass:[NSObject class]];
    self.appDelegateSelectors = [self setOfSelectorsForClass:[AppDelegate class]];
    self.pushPluginSelectors = [self setOfSelectorsForClass:[PushNotificationPlugin class]];
}

/** 
 * Collect the methods of a class, turn them into NSStrings, and
 * put them into a set so they can be manipulated.
 */
- (NSMutableSet*)setOfSelectorsForClass:(Class)aClass {
    Method *methods = nil;
    unsigned int methodCount = 0;
    // Copy methods return objects that need to be freed
    methods = class_copyMethodList(aClass, &methodCount);
    NSMutableSet *setOfSelectors = [NSMutableSet setWithCapacity:methodCount];
    for (int i=0; i<methodCount; i++) {
        [setOfSelectors addObject:NSStringFromSelector(method_getName(methods[i]))];
    }
    free(methods);
    return setOfSelectors;
}

@end


