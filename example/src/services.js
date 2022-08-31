import { Platform } from 'react-native';

const { default: config } = __DEV__
  ? require('../config')
  : require('../config.example');

export const httpPost = async (url, data) => {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json',
      'Authorization': '',
      'x-channel': Platform.OS,
    },
    body: JSON.stringify(data) ?? '{}',
  });

  const responseData = await response.json();

  if (response.ok) {
    if (
      responseData.refusalReasonCode != null &&
      responseData.refusalReasonCode !== '0'
    ) {
      throw responseData;
    }

    return responseData;
  }

  throw responseData;
};

export const getPaymentMethods = async (configuration) =>
  httpPost(`${config.baseUrl}/paymentMethods`, configuration);
