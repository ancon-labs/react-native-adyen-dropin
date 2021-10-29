<a href="https://app.circleci.com/pipelines/github/ancon-labs/react-native-adyen-dropin">
  <img src="https://img.shields.io/circleci/build/github/ancon-labs/react-native-adyen-dropin">
</a>
<a href="https://www.npmjs.com/package/@ancon/react-native-adyen-dropin">
  <img src="https://img.shields.io/npm/v/@ancon/react-native-adyen-dropin">
</a>

# react-native-adyen-dropin

React Native bridge for Adyen drop-in

🍏 Adyen iOS SDK v4.3.0
🤖 Adyen Android SDK v4.2.0

## Installation

```sh
yarn add @ancon/react-native-adyen-dropin
```

### Android

Add Kotlin v1.5 as a dependency in `android/build.gradle`:

```diff
buildscript {
  ext {
    buildToolsVersion           = "30.0.0"
    minSdkVersion               = 21
    compileSdkVersion           = 30
    targetSdkVersion            = 30
+    kotlinVersion               = "1.5.31"
  }
```

Add Kotlin as a dependency in `android/app/build.gradle`

```diff
dependencies {
  implementation fileTree(dir: "libs", include: ["*.jar"])
  //noinspection GradleDynamicVersion
  implementation("com.facebook.react:react-native:+")

+  implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
```

Add the following into `/android/app/src/main/res/values/styles.xml`:

```diff
<resources>
  <!-- Base application theme. -->
  <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
      <!-- Customize your theme here. -->
      <item name="android:textColor">#000000</item>
  </style>
+  <style name="AdyenCheckout.TextInputLayout">
+    <item name="boxStrokeColor">@color/primaryColor</item>
+    <item name="hintTextColor">@color/primaryColor</item>
+    <item name="android:minHeight">@dimen/input_layout_height</item>
+  </style>
</resources>
```

Add the following into `onCreate()` in `MainApplication.java`:

```diff
+import static com.reactnativeadyendropin.AdyenDropIn.setup;
// ...
@Override
public void onCreate() {
  super.onCreate();
  SoLoader.init(this, /* native exopackage */ false);
  initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
+  setup(this);
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
  isSuccessResult,
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
        if (isSuccessResult(res)) {
          Alert.alert('Success', `Payment success: ${res.resultCode}`);
        } else {
          Alert.alert('Refused', `Payment refused: ${res.refusalReason}`);
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
