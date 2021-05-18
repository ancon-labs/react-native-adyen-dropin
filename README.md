# react-native-adyen-dropin

React Native bridge for Adyen drop-in

## Installation

```sh
yarn add react-native-adyen-dropin
```

## Usage

```js
import AdyenDropin from '@ancon/react-native-adyen-dropin';

// ...

return (
  <AdyenDropIn
    visible={visible}
    paymentMethods={paymentMethods}
    paymentMethodsConfiguration={paymentMethodsConfiguration}
    paymentResponse={paymentResponse}
    detailsResponse={detailsResponse}
    onSubmit={handleSubmit}
    onAdditionalDetails={handleAdditionalDetails}
    onError={handleError}
    onSuccess={handleSuccess}
    onClose={handleClose}
  />
);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
