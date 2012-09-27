//
// NSObject+UASwizzle.m
// Reduced verison of the code used by JRSwizzle available here: https://github.com/rentzsch/jrswizzle


#import "NSObject+UASwizzle.h"

#import <objc/runtime.h>
#import <objc/message.h>

#define SetNSErrorFor(FUNC, ERROR_VAR, FORMAT,...)	\
if (ERROR_VAR) {	\
NSString *errStr = [NSString stringWithFormat:@"%s: " FORMAT,FUNC,##__VA_ARGS__]; \
*ERROR_VAR = [NSError errorWithDomain:@"NSCocoaErrorDomain" \
code:-1	\
userInfo:[NSDictionary dictionaryWithObject:errStr forKey:NSLocalizedDescriptionKey]]; \
}
#define SetNSError(ERROR_VAR, FORMAT,...) SetNSErrorFor(__func__, ERROR_VAR, FORMAT, ##__VA_ARGS__)


/**
 Definitions:
 Method opaque structure that represents a method in a class definition
 SEL Selector opaque structure that represents a method selector
 
 For efficiency, full ASCII names are not used as method selectors in compiled code. Instead, the compiler writes
 each method name into a table, then pairs the name with a unique identifier that represents the method at runtime.
 The runtime system makes sure each identifier is unique: No two selectors are the same, and all methods with the same
 name have the same selector.
 
 */

@implementation NSObject (UASwizzle)

+ (BOOL) ua_swizzleMethod:(SEL)aSelector withMethod:(SEL)anotherSelector error:(NSError**)error {
    // Get the original method,
    Method originalMethod = class_getInstanceMethod(self, aSelector);
    if (!originalMethod) {
        SetNSError(error, @"aMethod %@ not found for class %@", NSStringFromSelector(aSelector), [self class]);
        return NO;
    }
    Method newMethod = class_getInstanceMethod(self, anotherSelector);
    if (!newMethod) {
        SetNSError(error, @"anotherMethod %@ not found for class %@", NSStringFromSelector(anotherSelector), [self class]);
        return NO;
    }
    class_addMethod(self,
                    aSelector,
                    class_getMethodImplementation(self, aSelector),
                    method_getTypeEncoding(originalMethod));
    class_addMethod(self,
                    anotherSelector,
                    class_getMethodImplementation(self, anotherSelector),
                    method_getTypeEncoding(newMethod));
    
    method_exchangeImplementations(class_getInstanceMethod(self, aSelector), class_getInstanceMethod(self, anotherSelector));
    return YES;
}
@end