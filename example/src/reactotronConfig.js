import Reactotron from 'reactotron-react-native';
import { AsyncStorage } from 'react-native';

Reactotron.setAsyncStorageHandler(AsyncStorage) // AsyncStorage would either come from `react-native` or `@react-native-community/async-storage` depending on where you get it from
  .configure() // controls connection & communication settings
  .useReactNative({
    asyncStorage: false, // there are more options to the async storage.
    // optionally, you can turn it off with false.
    networking: {
      ignoreUrls: /symbolicate/,
    },
  }) // add all built-in react native plugins
  .connect(); // let's connect!

const originalConsoleLog = console.log;

console.log = (...args) => {
  originalConsoleLog(...args);
  Reactotron.log(...args);
};
