# Migration from v2 to v3

Update your app's `/android/app/src/main/res/values/styles.xml`:

```diff
<resources>
  <!-- Base application theme. -->
-  <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
+  <style name="CustomAppTheme" parent="Theme.AppCompat.Light.NoActionBar">
      <!-- Customize your theme here. -->
      <item name="android:textColor">#000000</item>
  </style>
-  <style name="AdyenCheckout.TextInputLayout">
-    <item name="boxStrokeColor">@color/primaryColor</item>
-    <item name="hintTextColor">@color/primaryColor</item>
-    <item name="android:minHeight">@dimen/input_layout_height</item>
-  </style>
</resources>
```

And your `/android/app/src/main/AndroidManifest.xml`:

```diff
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.example.reactnativeadyendropin">

    <application
-      android:theme="@style/AppTheme">
+      android:theme="@style/CustomAppTheme">
```
