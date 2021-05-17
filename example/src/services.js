export const httpPost = async (url, data) => {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json',
      'Authorization': '',
    },
    body: JSON.stringify(data) ?? '{}',
  });

  const responseData = await response.json();

  if (response.ok) {
    if (responseData.refusalReasonCode != null) {
      throw responseData;
    }

    return responseData;
  }

  throw responseData;
};

const BASE_URL = 'http://192.168.0.12:3000/api';

export const getPaymentMethods = async (configuration) =>
  httpPost(`${BASE_URL}/paymentMethods`, configuration);

export const makePayment = async (data) =>
  httpPost(`${BASE_URL}/payments`, data);

export const makeDetailsCall = async (data) =>
  httpPost(`${BASE_URL}/details`, data);
