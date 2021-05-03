import { requireNativeComponent, ViewStyle } from 'react-native';

type AdyenDropinProps = {
  color: string;
  style: ViewStyle;
};

export const AdyenDropinViewManager = requireNativeComponent<AdyenDropinProps>(
  'AdyenDropinView'
);

export default AdyenDropinViewManager;
