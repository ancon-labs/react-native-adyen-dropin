export const httpPost = (url, data) =>
  fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then((response) => response.json());

export const getPaymentMethods = (configuration) =>
  httpPost('http://192.168.0.12:3000/api/paymentMethods', configuration)
    .then((response) => {
      if (response.error) throw 'No paymentMethods available';
      return response;
    })
    .catch(console.error);

export const makePayment = (data, config = {}) => {
  // NOTE: Merging data object. DO NOT do this in production.
  const paymentRequest = { ...config, ...data };
  if (paymentRequest.order) {
    delete paymentRequest.amount;
  }
  return httpPost('http://192.168.0.12:3000/api/payments', paymentRequest)
    .then((response) => {
      if (response.error) throw 'Payment initiation failed';
      return response;
    })
    .catch(console.error);
};

export const makeDetailsCall = (data) =>
  httpPost('http://192.168.0.12:3000/api/payments/details', data)
    .then((response) => {
      if (response.error) throw 'Details call failed';
      return response;
    })
    .catch((err) => console.error(err));
