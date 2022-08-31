import React, { useState } from 'react';
import AdyenDropIn, {
  isCancelledError,
  isSuccessResult,
  PaymentResult,
} from '@ancon/react-native-adyen-dropin';
import { StyleSheet, View, Button, Alert } from 'react-native';

const { default: config } = __DEV__
  ? require('../config')
  : require('../config.example');

// @ts-ignore
import * as services from './services';

export default function App() {
  const [loading, setLoading] = useState(false);

  async function handlePress() {
    if (!loading) {
      setLoading(true);

      const adyenDropIn = AdyenDropIn.setModuleConfig({
        // Required
        baseUrl: config.baseUrl,
        // Optional
        debug: true,
        // Optional
        headers: {
          Authorization: 'Bearer 123',
        },
        // Optional
        endpoints: {
          makePayment: '/payments',
          makeDetailsCall: '/details',
          disableStoredPaymentMethod: '/disable',
        },
      }).setDropInConfig({
        // Required
        clientKey: config.clientKey,
        // Required
        environment: config.environment,
        // Required
        countryCode: config.countryCode,
        // Required
        amount: { value: 100, currencyCode: 'SEK' },
        // Optional
        applePay: {
          label: 'Example Company',
          amount: { value: 1, currencyCode: 'SEK' },
          configuration: {
            merchantId: config.applePay?.configuration?.merchantId,
          },
        },
        // Optional
        showRemovePaymentMethodButton: config.showRemovePaymentMethodButton,
        // Optional, required for recurring payments/saved payment method
        shopperReference: config.shopperReference,
      });

      const response = await services.getPaymentMethods();

      adyenDropIn
        .start(response)
        .then((res: PaymentResult) => {
          console.log('result:');
          console.log(res);
          if (isSuccessResult(res)) {
            Alert.alert('Success', `Payment success: ${res.resultCode}`);
          } else {
            Alert.alert('Refused', `Payment refused: ${res.refusalReason}`);
          }
        })
        .catch((err: Error) => {
          if (isCancelledError(err)) {
            console.log('Cancelled');
          } else {
            Alert.alert('Error', `Payment error: ${err.message}`);
            console.error(err);
          }
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }

  return (
    <View style={styles.container}>
      <Button disabled={loading} title="Start payment" onPress={handlePress} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
