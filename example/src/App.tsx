import React, { useState, useEffect } from 'react';
import { Platform, Button } from 'react-native';

import { StyleSheet, View } from 'react-native';
import AdyenDropinViewManager from 'react-native-adyen-dropin';
import config from '../config';

export default function App() {
  const [visible, setVisible] = useState(false);
  const [paymentMethods, setPaymentMethods] = useState(null);

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

  return (
    <View style={styles.container}>
      <AdyenDropinViewManager
        visible={visible}
        environment={config.environment}
        paymentMethods={paymentMethods}
        paymentMethodsConfiguration={{ clientKey: config.clientKey }}
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
