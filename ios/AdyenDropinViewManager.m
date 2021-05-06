#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>

@interface RCT_EXTERN_REMAP_MODULE(AdyenDropIn, AdyenDropInViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(visible, BOOL?)
RCT_EXPORT_VIEW_PROPERTY(paymentMethods, NSDictionary?)
RCT_EXPORT_VIEW_PROPERTY(paymentMethodsConfiguration, NSDictionary?)
RCT_EXPORT_VIEW_PROPERTY(paymentResponse, NSDictionary?)
RCT_EXPORT_VIEW_PROPERTY(detailsResponse, NSDictionary?)
RCT_EXPORT_VIEW_PROPERTY(onSubmit, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdditionalDetails, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onSuccess, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onClose, RCTDirectEventBlock)

@end
