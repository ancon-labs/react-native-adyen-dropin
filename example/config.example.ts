interface CardConfiguration {
  showsHolderNameField: boolean;
  showsStorePaymentMethodField: boolean;
  showsSecurityCodeField: boolean;
}

interface Amount {
  value: number;
  currencyCode: string;
}

interface ApplePayConfiguration {
  amount?: {
    value: number;
    currency: string;
  };
  countryCode?: string;
  configuration?: {
    merchantName?: string; // Name to be displayed on the form
    merchantId: string; // Your Apple merchant identifier
  };
}

interface ConfigInterface {
  baseUrl: string;
  clientKey: string;
  environment: 'test' | 'live';
  countryCode: string;
  payment?: Amount;
  card?: CardConfiguration;
  applePay?: ApplePayConfiguration;
  shopperReference?: string;
  showRemovePaymentMethodButton?: boolean;
}

const config = <ConfigInterface>{
  baseUrl: 'http://192.168.10.3:3000/api',
  clientKey: '<enter clientKey here>',
  environment: 'test',
  countryCode: '<enter countryCode here>',
  applePay: {
    label: '<enter company name here>',
    configuration: {
      merchantId: '<enter merchantId here>',
    },
  },
  shopperReference: '123456',
  showRemovePaymentMethodButton: false,
};

export default config;
