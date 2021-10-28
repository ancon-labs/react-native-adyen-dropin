#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(AdyenDropInModule, NSObject)

RCT_EXTERN_METHOD(setDropInConfig: (NSDictionary? *)config)
RCT_EXTERN_METHOD(setModuleConfig: (NSDictionary? *)config)
RCT_EXTERN_METHOD(start: (NSDictionary *)paymentMethodsResponse resolveCallback:(RCTResponseSenderBlock *)resolveCallback rejectCallback:(RCTResponseSenderBlock *)rejectCallback)

@end
