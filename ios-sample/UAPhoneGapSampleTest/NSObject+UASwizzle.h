//
//  NSObject+UASwizzle.h
//
// Copyright 2012 Urban Airship and Contributors

#import <Foundation/Foundation.h>

@interface NSObject (UASwizzle)

+ (BOOL) ua_swizzleMethod:(SEL)aSelector withMethod:(SEL)anotherSelector error:(NSError**)error;
@end
