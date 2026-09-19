//
//  RazorpayEventEmitter.h
//  RazorpayCheckout
//
//  Created by Abhinav Arora on 11/10/17.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "RCTEventEmitter.h"

@interface RazorpayEventEmitterCustom : RCTEventEmitter

+ (void)onPaymentSuccess:(NSString *)payment_id
                 andData:(NSDictionary *)response;
+ (void)onPaymentError:(int)code
           description:(NSString *)str
               andData:(NSDictionary *)response;
+ (void)upiApps:(NSArray *)upiApps;
+ (void)paymentMethods:(NSDictionary *) methods;
+ (void)paymentMethodsError:(NSDictionary *) error;
+ (void)recommendedInstruments:(NSDictionary *)response;
+ (void)recommendedInstrumentsError:(NSDictionary *)response;
+ (void)cardNetwork:(NSDictionary *)response;
+ (void)cardNetworkLength:(NSDictionary *)response;
+ (void)credAppAvailable:(NSDictionary *)response;
+ (void)walletLogoUrl:(NSDictionary *)response;
+ (void)subscriptionAmount:(NSDictionary *)response;
+ (void)cardNumberValidity:(NSDictionary *)response;
+ (void)validVpa:(NSDictionary *)response;
+ (void)bankLogoUrl:(NSDictionary *)response;
+ (void)sqWalletLogoUrl:(NSDictionary *)response;
@end
