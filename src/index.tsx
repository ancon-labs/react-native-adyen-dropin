import React from 'react';
import { requireNativeComponent, ViewStyle } from 'react-native';

type AdyenDropinProps = {
  visible?: boolean;
  environment?: 'test' | 'live';
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
      visible = false,
      environment = 'test',
      paymentMethods = {},
      paymentMethodsConfiguration = {},
      style,
    }: AdyenDropinProps,
    ref
  ) => {
    const forwardedRef = ref as React.RefObject<any>;

    return (
      <AdyenDropinViewManager
        ref={forwardedRef}
        visible={visible}
        environment={environment}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={paymentMethodsConfiguration}
        style={style}
      />
    );
  }
);

export default AdyenDropIn;
