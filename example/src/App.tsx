import React, { useState, useEffect } from 'react';
import { Alert, Button } from 'react-native';

import { StyleSheet, View } from 'react-native';
import AdyenDropIn from '@ancon/react-native-adyen-dropin';

const { default: config } = __DEV__
  ? require('../config')
  : require('../config.example');

// @ts-ignore
import * as services from './services';

export default function App() {
  const [visible, setVisible] = useState(false);
  const [paymentMethods, setPaymentMethods] = useState(null);
  const [paymentResponse, setPaymentResponse] = useState(null);
  const [detailsResponse, setDetailsResponse] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function init() {
      try {
        const response = await services.getPaymentMethods();
        setPaymentMethods(response);
      } catch (err) {
        setError(err);
      }
    }

    init();
  }, []);

  useEffect(() => {
    if (error) {
      console.log('Show error', error);
      Alert.alert('Payment failure', JSON.stringify(error ?? {}, undefined, 2));
    }
  }, [error]);

  async function handleSubmit(data: any) {
    try {
      console.log('running handleSubmit');
      console.log(data);
      const response = await services.makePayment(data);
      console.log('handleSubmit response');
      console.log(response);
      setPaymentResponse(response);
    } catch (err) {
      setVisible(false);
      setError(err);
    }
  }

  async function handleAdditionalDetails(data: any) {
    try {
      console.log('running handleAdditionalDetails');
      console.log(data);
      const response = await services.makeDetailsCall(data);
      console.log('handleAdditionalDetails response');
      console.log(response);
      setDetailsResponse(response);
    } catch (err) {
      setDetailsResponse(err);
      setVisible(false);
      setError(err);
    }
  }

  function handleError(data: any) {
    console.log('handleError', data);
    setError(data);
  }

  function handleSuccess(data: any) {
    console.log('handleSuccess', data);
    Alert.alert('Payment success', JSON.stringify(data ?? {}, undefined, 2));
  }

  function handleClose() {
    setVisible(false);
  }

  return (
    <View style={styles.container}>
      <AdyenDropIn
        visible={visible}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={{
          clientKey: config.clientKey,
          environment: config.environment,
          countryCode: config.countryCode,
          applePay: {
            amount: { value: 1, currencyCode: 'SEK' },
            configuration: {
              merchantId: config.applePay?.configuration?.merchantId,
            },
          },
          amount: { value: 100, currencyCode: 'SEK' },
        }}
        paymentResponse={paymentResponse}
        detailsResponse={detailsResponse}
        onSubmit={handleSubmit}
        onAdditionalDetails={handleAdditionalDetails}
        onError={handleError}
        onSuccess={handleSuccess}
        onClose={handleClose}
      />
      <Button title="Start payment" onPress={() => setVisible(true)} />
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
