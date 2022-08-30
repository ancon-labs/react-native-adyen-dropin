import Reactotron from 'reactotron-react-native';

Reactotron.configure() // controls connection & communication settings
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
