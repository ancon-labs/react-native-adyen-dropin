export const httpPost = async (url, data) =>
  fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then((response) => {
    if (response.ok) {
      return response.json();
    }

    throw new Error(response.json());
  });

export const getPaymentMethods = (configuration) =>
  httpPost('http://192.168.0.12:3000/api/paymentMethods', configuration);

export const makePayment = (data) =>
  httpPost('http://192.168.0.12:3000/api/payments', data);

export const makeDetailsCall = (data) =>
  httpPost('http://192.168.0.12:3000/api/payments/details', data);
