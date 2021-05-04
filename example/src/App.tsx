import React, { useState, useEffect } from 'react';
import { Platform } from 'react-native';

import { StyleSheet, View } from 'react-native';
import AdyenDropinViewManager from 'react-native-adyen-dropin';
import config from '../config';

export default function App() {
  const [visible, setVisible] = useState(false);
  const [paymentMethods, setPaymentMethods] = useState(null);
  const [paymentMethodsConfiguration] = useState(config);

  useEffect(() => {
    async function init() {
      const response = await fetch(
        'http://192.168.0.12:3000/api/paymentMethods',
        {
          method: 'POST',
          body: {
            channel: Platform.OS,
          },
        }
      ).then((r) => r.json());

      console.log(JSON.stringify(response, undefined, 2));

      setPaymentMethods(response);
    }

    init();
  }, []);

  useEffect(() => {
    if (paymentMethods) {
      setVisible(true);
    }
  }, [paymentMethods]);

  return (
    <View style={styles.container}>
      <AdyenDropinViewManager
        visible={visible}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={paymentMethodsConfiguration}
      />
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
