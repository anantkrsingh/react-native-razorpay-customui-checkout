# react-native-razorpay-customui-checkout

React Native wrapper around Razorpay's native Custom UI Checkout SDK.

This package starts as a fork of [`react-native-customui`](https://github.com/razorpay/react-native-customui)
(same Gradle and CocoaPods native dependencies) and will be progressively
rewritten to fix issues in the upstream JS bridge, in particular:

- `Razorpay.js`'s global `removeSubscriptions()` call, which tears down
  **every** registered native event listener whenever any single SDK call
  resolves, corrupting concurrent/retried requests.
- `initRazorpay()`'s JS promise never settling on some SDK versions.
- No built-in cancellation/timeout/retry support for discovery calls
  (`getPaymentMethods`, `getAppsWhichSupportUPI`).

## Status

Native Android (`android/`) and iOS (`ios/`) sources are currently a direct
copy of the upstream module so the build config (Gradle + CocoaPods) keeps
working as-is. The JS/TS implementation is being rewritten incrementally.

## Installation

```sh
npm install react-native-razorpay-customui-checkout
cd ios && pod install
```

## License

MIT
