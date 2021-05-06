import React, { useState, useEffect } from 'react';
import { Alert, Button } from 'react-native';

import { StyleSheet, View } from 'react-native';
import AdyenDropIn from 'react-native-adyen-dropin';

const config = __DEV__ ? require('../config') : require('../config.example');

// @ts-ignore
import * as services from './services';

export default function App() {
  const [visible, setVisible] = useState(false);
  const [paymentMethods, setPaymentMethods] = useState(null);
  const [paymentResponse, setPaymentResponse] = useState(null);
  const [detailsResponse, setDetailsResponse] = useState(null);

  useEffect(() => {
    async function init() {
      const response = await services.getPaymentMethods();
      setPaymentMethods(response);
    }

    init();
  }, []);

  async function handleSubmit(data: any) {
    console.log('running handleSubmit');
    console.log(data);
    const response = await services.makePayment(data);
    console.log('handleSubmit response');
    console.log(response);
    setPaymentResponse(response);
  }

  async function handleAdditionalDetails(data: any) {
    console.log('running handleAdditionalDetails');
    console.log(data);
    const response = await services.makeDetailsCall(data);
    console.log('handleAdditionalDetails response');
    console.log(response);
    setDetailsResponse(response);
  }

  function handleError(err: any) {
    Alert.alert('Payment failure', JSON.stringify(err ?? {}, undefined, 2));
  }

  // function handleSuccess() {
  //   Alert.alert('Payment success');
  // }

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
        // onSuccess={handleSuccess}
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
