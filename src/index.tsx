import React from 'react';
import { requireNativeComponent, ViewStyle } from 'react-native';

type AdyenDropInProps = {
  visible?: boolean;
  paymentMethods?: any;
  paymentMethodsConfiguration?: any;
  paymentResponse?: any;
  detailsResponse?: any;
  onSubmit?: Function;
  onAdditionalDetails?: Function;
  onError?: Function;
  onSuccess?: Function;
  onClose?: Function;
  style?: ViewStyle;
};

export const AdyenDropInModule = requireNativeComponent<AdyenDropInProps>(
  'AdyenDropIn'
);

function cleanEvent(event: any): any {
  if (event && typeof event === 'object' && event.target !== undefined) {
    delete event.target;
  }

  return event;
}

const AdyenDropIn = React.forwardRef(
  (
    {
      visible = false,
      paymentMethods = {},
      paymentMethodsConfiguration = {},
      paymentResponse,
      detailsResponse,
      onSubmit,
      onAdditionalDetails,
      onError,
      onSuccess,
      onClose,
      style,
    }: AdyenDropInProps,
    ref
  ) => {
    const forwardedRef = ref as React.RefObject<any>;

    function handleSubmit(event: any) {
      onSubmit?.(cleanEvent(event.nativeEvent));
    }

    function handleError(event: any) {
      onError?.(cleanEvent(event.nativeEvent));
    }

    function handleAdditionalDetails(event: any) {
      onAdditionalDetails?.(cleanEvent(event.nativeEvent));
    }

    function handleSuccess(event: any) {
      onSuccess?.(cleanEvent(event.nativeEvent));
    }

    function handleClose(event: any) {
      onClose?.(cleanEvent(event.nativeEvent));
    }

    return (
      <AdyenDropInModule
        ref={forwardedRef}
        visible={visible}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={paymentMethodsConfiguration}
        paymentResponse={paymentResponse}
        detailsResponse={detailsResponse}
        onSubmit={handleSubmit}
        onAdditionalDetails={handleAdditionalDetails}
        onError={handleError}
        onSuccess={handleSuccess}
        onClose={handleClose}
        style={style}
      />
    );
  }
);

export default AdyenDropIn;
