#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(AdyenDropinViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(visible, BOOL?)
RCT_EXPORT_VIEW_PROPERTY(environment, NSString?)
RCT_EXPORT_VIEW_PROPERTY(paymentMethods, NSDictionary?)
RCT_EXPORT_VIEW_PROPERTY(paymentMethodsConfiguration, NSDictionary?)

@end
