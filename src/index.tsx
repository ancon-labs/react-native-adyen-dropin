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
  style?: ViewStyle;
};

export const AdyenDropInModule = requireNativeComponent<AdyenDropInProps>(
  'AdyenDropIn'
);

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
      style,
    }: AdyenDropInProps,
    ref
  ) => {
    const forwardedRef = ref as React.RefObject<any>;

    function handleSubmit(event: any) {
      onSubmit?.(event.nativeEvent);
    }

    function handleError(event: any) {
      onError?.(event.nativeEvent);
    }

    function handleAdditionalDetails(event: any) {
      onAdditionalDetails?.(event.nativeEvent);
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
        style={style}
      />
    );
  }
);

export default AdyenDropIn;
