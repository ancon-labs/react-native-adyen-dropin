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

Add the following into your app's `MainApplication.java`:

```java
import static com.reactnativeadyendropin.AdyenDropIn.setup;

@Override
public void onCreate() {
  super.onCreate();
  SoLoader.init(this, /* native exopackage */ false);
  initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  // ADD setup(this); in onCreate()
  setup(this);
}
```

### iOS

Install pods:

```sh
cd ios && pod install
```

Add this to your project's swift file to be able to handle redirect actions (or create one and generate a bridging header file):

```swift
import Foundation
import Adyen

@objc class AdyenObjectiveCBridge: NSObject {

  @objc(applicationDidOpenURL:)
  static func applicationDidOpen(_ url: URL) -> Bool {
     let adyenHandled = RedirectComponent.applicationDidOpen(from : url)
     return adyenHandled
  }
}
```

## Usage

See example at `example/src/App.tsx`

```tsx
import AdyenDropIn, {
  isCancelledError,
  isRefusedResult,
  PaymentResult,
} from '@ancon/react-native-adyen-dropin';

// ...

async function handlePress() {
  if (!loading) {
    setLoading(true);

    const adyenDropIn = AdyenDropIn.setModuleConfig({
      // Required API base URL
      baseUrl: 'http://192.168.1.74:3000/api/',
      // Optional to view more native logs
      debug: true,
      // Optional custom headers
      headers: {
        Authorization: 'Bearer 123',
      },
      // Optional custom endpoints
      endpoints: {
        makePayment: '/payments',
        makeDetailsCall: '/details',
      },
    }).setDropInConfig({
      // Required
      clientKey: config.clientKey,
      // Required
      environment: config.environment,
      // Required
      countryCode: config.countryCode,
      // Required
      amount: { value: 100, currencyCode: 'SEK' },
      // Optional
      applePay: {
        label: 'Example Company',
        amount: { value: 1, currencyCode: 'SEK' },
        configuration: {
          merchantId: config.applePay?.configuration?.merchantId,
        },
      },
    });

    // Fetch payment methods
    const response = await services.getPaymentMethods();

    // Start the drop-in flow
    adyenDropIn
      .start(response)
      .then((res: PaymentResult) => {
        if (isRefusedResult(res)) {
          Alert.alert('Refused', `Payment refused: ${res.refusalReason}`);
        } else {
          Alert.alert('Success', `Payment success: ${res.resultCode}`);
        }
      })
      .catch((err: Error) => {
        if (isCancelledError(err)) {
          console.log('Cancelled');
        } else {
          Alert.alert('Error', `Payment error: ${err.message}`);
        }
      })
      .finally(() => {
        setLoading(false);
      });
  }
}

// ...
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
