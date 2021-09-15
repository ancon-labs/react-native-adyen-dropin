<a href="https://app.circleci.com/pipelines/github/ancon-labs/react-native-adyen-dropin">
  <img src="https://img.shields.io/circleci/build/github/ancon-labs/react-native-adyen-dropin">
</a>
<a href="https://www.npmjs.com/package/@ancon/react-native-adyen-dropin">
  <img src="https://img.shields.io/npm/v/@ancon/react-native-adyen-dropin">
</a>

# react-native-adyen-dropin

React Native bridge for Adyen drop-in

## Installation

```sh
yarn add @ancon/react-native-adyen-dropin
```

### Android

Add the following into your app's `/android/app/src/main/res/values/styles.xml`:

```xml
<style name="AdyenCheckout.TextInputLayout">
  <item name="boxStrokeColor">@color/primaryColor</item>
  <item name="hintTextColor">@color/primaryColor</item>
  <item name="android:minHeight">@dimen/input_layout_height</item>
</style>
```

### iOS

```sh
cd ios && pod install
```

## Usage

See example at `example/src/App.tsx`

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

## Props

| Props                       | Type     | Description                                                    | Default |
| --------------------------- | -------- | -------------------------------------------------------------- | ------- |
| debug                       | boolean  | Set to `true` to enable native debugging logs                  | false   |
| visible                     | boolean  | Whether or not the drop-in should show                         | false   |
| paymentMethods              | object   | Payment methods response object                                | {}      |
| paymentMethodsConfiguration | object   | Payment methods configuration object                           | {}      |
| paymentResponse             | object   | Payment response object (onSubmit request)                     |         |
| detailsResponse             | object   | Details response object (onAdditionalDetails request)          |         |
| onSubmit                    | function | Callback with data when making a new payment                   |         |
| onAdditionalDetails         | function | Callback with data when an action is required                  |         |
| onError                     | function | Callback with error (if available) on error or payment failure |         |
| onSuccess                   | function | Callback with resultCode on payment success                    |         |
| onClose                     | function | Callback when the drop-in was closed                           |         |

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
