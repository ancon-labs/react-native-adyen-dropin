#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(AdyenDropInModule, NSObject)

RCT_EXTERN_METHOD(setDropInConfig: (NSDictionary? *)config)
RCT_EXTERN_METHOD(setModuleConfig: (NSDictionary? *)config)
RCT_EXTERN_METHOD(setSubmitCallback: (RCTResponseSenderBlock *)onSubmit)
RCT_EXTERN_METHOD(setAdditionalDetailsCallback: (RCTResponseSenderBlock *)onAdditionalDetails)
RCT_EXTERN_METHOD(setPaymentResponse: (NSDictionary? *)paymentResponse)
RCT_EXTERN_METHOD(setDetailsResponse: (NSDictionary? *)detailsResponse)
RCT_EXTERN_METHOD(start: (NSDictionary *)paymentMethodsResponse resolveCallback:(RCTResponseSenderBlock *)resolveCallback rejectCallback:(RCTResponseSenderBlock *)rejectCallback)

@end
