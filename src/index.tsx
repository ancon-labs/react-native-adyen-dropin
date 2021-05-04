import React from 'react';
import { requireNativeComponent, ViewStyle } from 'react-native';

type AdyenDropinProps = {
  visible?: boolean;
  paymentMethods?: any;
  paymentMethodsConfiguration?: any;
  style?: ViewStyle;
};

export const AdyenDropinViewManager = requireNativeComponent<AdyenDropinProps>(
  'AdyenDropinView'
);

const AdyenDropIn = React.forwardRef(
  (
    {
      visible,
      paymentMethods,
      paymentMethodsConfiguration,
      style,
    }: AdyenDropinProps,
    ref
  ) => {
    const forwardedRef = ref as React.RefObject<any>;

    return (
      <AdyenDropinViewManager
        ref={forwardedRef}
        visible={visible}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={paymentMethodsConfiguration}
        style={style}
      />
    );
  }
);

export default AdyenDropIn;
